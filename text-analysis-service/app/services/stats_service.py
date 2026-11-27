# app/services/stats_service.py
from app.repositories.analysis_repository import AnalysisRepository
from app.schemas.analysis import HskTokenDistribution, HskUniqueWords, StudentStatsResponse
from app.services.hsk import HskVocabulary


class StatsService:
    def __init__(self, repository: AnalysisRepository, vocabulary: HskVocabulary):
        self._repository = repository
        self._vocabulary = vocabulary

    def for_student(self, student_id: int) -> StudentStatsResponse:
        distribution = sorted(
            self._repository.count_tokens_by_hsk_level(student_id),
            key=lambda row: (row[0] is None, row[0] or 0),  # words without a level come last
        )
        unique_words = sorted(self._repository.count_distinct_words_by_hsk_level(student_id))
        totals = self._vocabulary.total_words_per_level()

        return StudentStatsResponse(
            token_distribution=[
                HskTokenDistribution(hsk_level=level, token_count=count) for level, count in distribution
            ],
            source_type_split=self._repository.count_by_source_type(student_id),
            unique_chars_per_hsk_level=[
                HskUniqueWords(
                    hsk_level=level,
                    unique_count=count,
                    total_in_level=totals.get(level, 0),
                    percentage=_percentage(count, totals.get(level, 0)),
                )
                for level, count in unique_words
            ],
        )


def _percentage(part: int, whole: int) -> float:
    return round(part / whole * 100, 2) if whole else 0.0