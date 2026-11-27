# app/repositories/analysis_repository.py
from typing import Literal

from sqlalchemy import func, select
from sqlalchemy.orm import Session, selectinload

from app.models.analysis import AnalysisToken, TextAnalysis


class AnalysisRepository:
    """Persistence for text analyses. It never commits: the service owns the transaction."""

    def __init__(self, session: Session):
        self._session = session

    def add(self, analysis: TextAnalysis) -> TextAnalysis:
        self._session.add(analysis)
        self._session.flush()
        return analysis

    def find_by_id(self, analysis_id: int) -> TextAnalysis | None:
        statement = (
            select(TextAnalysis)
            .where(TextAnalysis.id == analysis_id)
            .options(selectinload(TextAnalysis.tokens))
        )
        return self._session.scalars(statement).one_or_none()


    def delete(self, analysis: TextAnalysis) -> None:
        self._session.delete(analysis)
        self._session.flush()

    def find_page(
        self,
        student_id: int,
        *,
        source_type: str | None,
        hsk_level: int | None,
        sort_order: Literal["newest", "oldest"],
        offset: int,
        limit: int,
    ) -> tuple[list[TextAnalysis], int]:
        conditions = [TextAnalysis.student_id == student_id]
        if source_type is not None:
            conditions.append(TextAnalysis.source_type == source_type)
        if hsk_level is not None:
            conditions.append(TextAnalysis.overall_hsk_level == hsk_level)

        total = self._session.scalar(select(func.count(TextAnalysis.id)).where(*conditions)) or 0

        ordering = (
            (TextAnalysis.created_at.asc(), TextAnalysis.id.asc())
            if sort_order == "oldest"
            else (TextAnalysis.created_at.desc(), TextAnalysis.id.desc())
        )
        statement = select(TextAnalysis).where(*conditions).order_by(*ordering).offset(offset).limit(limit)
        return list(self._session.scalars(statement)), total


    def count_tokens_by_hsk_level(self, student_id: int) -> list[tuple[int | None, int]]:
        statement = (
            select(AnalysisToken.hsk_level, func.count(AnalysisToken.id))
            .join(TextAnalysis, AnalysisToken.analysis_id == TextAnalysis.id)
            .where(TextAnalysis.student_id == student_id)
            .group_by(AnalysisToken.hsk_level)
        )
        return [(level, count) for level, count in self._session.execute(statement)]


    def count_distinct_words_by_hsk_level(self, student_id: int) -> list[tuple[int, int]]:
        statement = (
            select(AnalysisToken.hsk_level, func.count(AnalysisToken.hanzi.distinct()))
            .join(TextAnalysis, AnalysisToken.analysis_id == TextAnalysis.id)
            .where(TextAnalysis.student_id == student_id, AnalysisToken.hsk_level.is_not(None))
            .group_by(AnalysisToken.hsk_level)
        )
        return [(level, count) for level, count in self._session.execute(statement)]


    def count_by_source_type(self, student_id: int) -> dict[str, int]:
        statement = (
            select(TextAnalysis.source_type, func.count(TextAnalysis.id))
            .where(TextAnalysis.student_id == student_id)
            .group_by(TextAnalysis.source_type)
        )
        return {source_type: count for source_type, count in self._session.execute(statement)}