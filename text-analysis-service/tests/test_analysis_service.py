# tests/test_analysis_service.py
from datetime import datetime

import pytest

from app.errors import ForbiddenError, NoTextDetectedError, NotFoundError
from app.models.analysis import TextAnalysis
from app.security import AnalysisAccessPolicy, CurrentUser, Role
from app.services.analysis_service import AnalysisService
from app.services.hsk import HskVocabulary
from app.services.nlp import NlpProcessor

NOW = datetime(2026, 1, 1, 12, 0)


class FakeSession:
    def __init__(self):
        self.commits = 0

    def commit(self):
        self.commits += 1


class FakeRepository:
    def __init__(self, stored=None):
        self.stored = stored or {}
        self.added = []
        self.deleted = []

    def add(self, analysis):
        analysis.id = 99
        self.added.append(analysis)
        return analysis

    def find_by_id(self, analysis_id):
        return self.stored.get(analysis_id)

    def delete(self, analysis):
        self.deleted.append(analysis)


class FakeSegmenter:
    def __init__(self, words):
        self.words = words

    def segment(self, text):
        return self.words


class FakeTranslator:
    def __init__(self):
        self.calls = []

    def translate(self, texts, target_language):
        self.calls.append((list(texts), target_language))
        return [f"{text}!" for text in texts]


def make_service(words=(("你好", "INTJ"), ("，", "PUNCT"), ("你好", "INTJ")), stored=None):
    session, repository, translator = FakeSession(), FakeRepository(stored), FakeTranslator()
    nlp = NlpProcessor(HskVocabulary({"你好": 1}), FakeSegmenter(list(words)), romanizer=lambda w: w)
    service = AnalysisService(
        session, repository, nlp, translator, AnalysisAccessPolicy(), clock=lambda: NOW
    )
    return service, session, repository, translator


def test_analysis_is_translated_saved_and_committed_once():
    service, session, repository, translator = make_service()

    analysis = service.analyze(7, "你好，你好", "MANUAL", "ro")

    assert session.commits == 1 and repository.added == [analysis]
    assert analysis.student_id == 7 and analysis.created_at == NOW
    assert analysis.overall_hsk_level == 1
    assert analysis.translated_text == "你好，你好!"
    assert [t.position_index for t in analysis.tokens] == [0, 1, 2]


def test_punctuation_is_not_translated_and_repeated_words_are_sent_once():
    service, _, _, translator = make_service()

    analysis = service.analyze(7, "你好，你好", "MANUAL", "ro")

    assert translator.calls == [(["你好，你好", "你好"], "ro")]
    assert [t.translation for t in analysis.tokens] == ["你好!", None, "你好!"]


def test_nothing_is_saved_when_no_words_are_found():
    service, session, repository, translator = make_service(words=[(" ", "SPACE")])

    with pytest.raises(NoTextDetectedError):
        service.analyze(7, "   ", "OCR", "en")

    assert repository.added == [] and session.commits == 0 and translator.calls == []


def stored_analysis(student_id=7):
    return TextAnalysis(student_id=student_id, raw_text="x", source_type="MANUAL")


def test_student_reads_own_analysis():
    analysis = stored_analysis()
    service, *_ = make_service(stored={1: analysis})
    assert service.get(CurrentUser(7, Role.STUDENT), 1) is analysis


def test_another_students_analysis_looks_like_a_missing_one():
    service, *_ = make_service(stored={1: stored_analysis(student_id=7)})
    with pytest.raises(NotFoundError):
        service.get(CurrentUser(8, Role.STUDENT), 1)
    with pytest.raises(NotFoundError):
        service.get(CurrentUser(8, Role.STUDENT), 404)


def test_admin_reads_any_analysis_and_teacher_none():
    service, *_ = make_service(stored={1: stored_analysis()})
    assert service.get(CurrentUser(1, Role.ADMIN), 1)
    with pytest.raises(ForbiddenError):
        service.get(CurrentUser(2, Role.TEACHER), 1)


def test_delete_removes_and_commits_only_for_the_owner():
    analysis = stored_analysis()
    service, session, repository, _ = make_service(stored={1: analysis})

    with pytest.raises(NotFoundError):
        service.delete(CurrentUser(8, Role.STUDENT), 1)
    assert repository.deleted == [] and session.commits == 0

    service.delete(CurrentUser(7, Role.STUDENT), 1)
    assert repository.deleted == [analysis] and session.commits == 1