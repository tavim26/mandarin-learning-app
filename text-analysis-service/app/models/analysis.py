# app/models/analysis.py
from datetime import datetime

from sqlalchemy import BigInteger, DateTime, ForeignKey, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class TextAnalysis(Base):
    __tablename__ = "text_analyses"
    __table_args__ = (Index("ix_text_analyses_student_created", "student_id", "created_at"),)

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    student_id: Mapped[int] = mapped_column(BigInteger, nullable=False)
    raw_text: Mapped[str] = mapped_column(Text, nullable=False)
    source_type: Mapped[str] = mapped_column(String(20), nullable=False)
    overall_hsk_level: Mapped[int | None] = mapped_column(Integer)
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    translated_text: Mapped[str | None] = mapped_column(Text)
    translation_language: Mapped[str | None] = mapped_column(String(10))

    tokens: Mapped[list["AnalysisToken"]] = relationship(
        back_populates="analysis",
        cascade="all, delete-orphan",
        passive_deletes=True,
        order_by="AnalysisToken.position_index",
    )


class AnalysisToken(Base):
    __tablename__ = "analysis_tokens"
    __table_args__ = (Index("ix_analysis_tokens_analysis_id", "analysis_id"),)

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    analysis_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("text_analyses.id", ondelete="CASCADE"), nullable=False
    )
    hanzi: Mapped[str] = mapped_column(String(100), nullable=False)
    pinyin: Mapped[str | None] = mapped_column(String(255))
    translation: Mapped[str | None] = mapped_column(Text)
    hsk_level: Mapped[int | None] = mapped_column(Integer)
    position_index: Mapped[int] = mapped_column(Integer, nullable=False)
    pos: Mapped[str | None] = mapped_column(String(50))

    analysis: Mapped[TextAnalysis] = relationship(back_populates="tokens")