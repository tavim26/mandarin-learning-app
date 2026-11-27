# tests/test_stats_service.py
from app.services.hsk import HskVocabulary
from app.services.stats_service import StatsService


class FakeRepository:
    def count_tokens_by_hsk_level(self, student_id):
        return [(None, 5), (3, 2), (1, 10)]

    def count_distinct_words_by_hsk_level(self, student_id):
        return [(3, 1), (1, 3)]

    def count_by_source_type(self, student_id):
        return {"MANUAL": 2, "OCR": 1}


def test_stats_are_sorted_and_percentages_computed():
    vocabulary = HskVocabulary({"a": 1, "b": 1, "c": 1, "d": 1, "e": 3})

    stats = StatsService(FakeRepository(), vocabulary).for_student(7)

    assert [(d.hsk_level, d.token_count) for d in stats.token_distribution] == [(1, 10), (3, 2), (None, 5)]
    assert [(u.hsk_level, u.unique_count, u.total_in_level, u.percentage) for u in stats.unique_chars_per_hsk_level] == [
        (1, 3, 4, 75.0),
        (3, 1, 1, 100.0),
    ]
    assert stats.source_type_split == {"MANUAL": 2, "OCR": 1}


def test_level_missing_from_the_vocabulary_gives_zero_percent():
    stats = StatsService(FakeRepository(), HskVocabulary({})).for_student(7)
    assert all(u.percentage == 0.0 for u in stats.unique_chars_per_hsk_level)