# app/services/analysis_service.py
import logging
from datetime import datetime
from typing import Callable

from sqlalchemy.orm import Session

from app.errors import NoTextDetectedError, NotFoundError
from app.models.analysis import AnalysisToken, TextAnalysis
from app.repositories.analysis_repository import AnalysisRepository
from app.security import AnalysisAccessPolicy, CurrentUser, Role
from app.services.hsk import dominant_level
from app.services.nlp import NlpProcessor, WordToken
from app.services.translation import GoogleTranslateClient, translate_text_and_words

logger = logging.getLogger(__name__)


class AnalysisService:
    """Analyses Chinese text, stores the result and serves it back to its owner."""

    def __init__(
        self,
        session: Session,
        repository: AnalysisRepository,
        nlp: NlpProcessor,
        translator: GoogleTranslateClient,
        policy: AnalysisAccessPolicy,
        clock: Callable[[], datetime] = datetime.now,
    ):
        self._session = session
        self._repository = repository
        self._nlp = nlp
        self._translator = translator
        self._policy = policy
        self._clock = clock

    def analyze(
        self,
        student_id: int,
        raw_text: str,
        source_type: str,
        translation_language: str,
    ) -> TextAnalysis:
        words = self._nlp.process(raw_text)
        if not words:
            raise NoTextDetectedError("No text could be found to analyse")

    
        translated_text, word_translations = translate_text_and_words(
            self._translator,
            raw_text,
            [word.hanzi for word in words if word.is_translatable],
            translation_language,
        )

        analysis = TextAnalysis(
            student_id=student_id,
            raw_text=raw_text,
            source_type=source_type,
            overall_hsk_level=dominant_level(word.hsk_level for word in words),
            translated_text=translated_text,
            translation_language=translation_language,
            created_at=self._clock(),
            tokens=[self._build_token(word, word_translations) for word in words],
        )
        self._repository.add(analysis)
        self._session.commit()
        return analysis

    def get(self, user: CurrentUser, analysis_id: int) -> TextAnalysis:
        return self._find_owned(user, analysis_id)

    def delete(self, user: CurrentUser, analysis_id: int) -> None:
        analysis = self._find_owned(user, analysis_id)
        self._repository.delete(analysis)
        self._session.commit()

    def list_for_student(
        self,
        student_id: int,
        *,
        page: int,
        size: int,
        source_type: str | None,
        hsk_level: int | None,
        sort_order: str,
    ) -> tuple[list[TextAnalysis], int]:
        return self._repository.find_page(
            student_id,
            source_type=source_type,
            hsk_level=hsk_level,
            sort_order=sort_order,
            offset=(page - 1) * size,
            limit=size,
        )

    def preview(self, text: str) -> list[WordToken]:
        return self._nlp.process(text)

    def _find_owned(self, user: CurrentUser, analysis_id: int) -> TextAnalysis:
        self._policy.assert_can_use_module(user)
        analysis = self._repository.find_by_id(analysis_id)

        if analysis is None or (user.role is Role.STUDENT and analysis.student_id != user.id):
            raise NotFoundError("Analysis not found")
        return analysis

    @staticmethod
    def _build_token(word: WordToken, translations: dict[str, str]) -> AnalysisToken:
        return AnalysisToken(
            hanzi=word.hanzi,
            pinyin=word.pinyin,
            translation=translations.get(word.hanzi),
            hsk_level=word.hsk_level,
            position_index=word.position_index,
            pos=word.pos,
        )