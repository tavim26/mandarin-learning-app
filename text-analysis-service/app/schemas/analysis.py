# app/schemas/analysis.py
from datetime import datetime
from typing import Generic, Literal, TypeVar

from pydantic import BaseModel, ConfigDict, Field, field_validator

TranslationLanguage = Literal["ro", "en", "de", "es", "fr"]
SourceType = Literal["MANUAL", "OCR"]
SortOrder = Literal["newest", "oldest"]

MAX_TEXT_LENGTH = 5000

T = TypeVar("T")


def _require_non_blank(value: str) -> str:
    if not value.strip():
        raise ValueError("Text must not be blank")
    return value


class AnalyzeTextRequest(BaseModel):
    raw_text: str = Field(min_length=1, max_length=MAX_TEXT_LENGTH)
    translation_language: TranslationLanguage = "en"

    _not_blank = field_validator("raw_text")(_require_non_blank)


class PreviewRequest(BaseModel):
    text: str = Field(min_length=1, max_length=MAX_TEXT_LENGTH)

    _not_blank = field_validator("text")(_require_non_blank)


class AnalysisTokenResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    analysis_id: int
    hanzi: str
    pinyin: str | None
    translation: str | None
    hsk_level: int | None
    position_index: int
    pos: str | None


class AnalysisSummaryResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    student_id: int
    raw_text: str
    source_type: str
    overall_hsk_level: int | None
    created_at: datetime
    translated_text: str | None
    translation_language: str | None


class AnalysisResponse(AnalysisSummaryResponse):
    tokens: list[AnalysisTokenResponse]


class Page(BaseModel, Generic[T]):
    items: list[T]
    total: int
    page: int
    size: int
    total_pages: int


class PreviewTokenResponse(BaseModel):
    hanzi: str
    pinyin: str
    hsk_level: int | None
    position_index: int
    pos: str | None


class PreviewResponse(BaseModel):
    tokens: list[PreviewTokenResponse]


class HskTokenDistribution(BaseModel):
    hsk_level: int | None
    token_count: int


class HskUniqueWords(BaseModel):
    hsk_level: int
    unique_count: int
    total_in_level: int
    percentage: float


class StudentStatsResponse(BaseModel):
    token_distribution: list[HskTokenDistribution]
    source_type_split: dict[str, int]
    unique_chars_per_hsk_level: list[HskUniqueWords]