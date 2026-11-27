# tests/test_schemas.py
import pytest
from pydantic import ValidationError

from app.schemas.analysis import MAX_TEXT_LENGTH, AnalyzeTextRequest, Page, PreviewRequest


def test_defaults_to_english():
    assert AnalyzeTextRequest(raw_text="你好").translation_language == "en"


@pytest.mark.parametrize("text", ["", "   \n"])
def test_blank_text_is_rejected(text):
    with pytest.raises(ValidationError):
        AnalyzeTextRequest(raw_text=text)
    with pytest.raises(ValidationError):
        PreviewRequest(text=text)


def test_overlong_text_is_rejected():
    with pytest.raises(ValidationError):
        AnalyzeTextRequest(raw_text="好" * (MAX_TEXT_LENGTH + 1))


def test_unsupported_language_is_rejected():
    with pytest.raises(ValidationError):
        AnalyzeTextRequest(raw_text="你好", translation_language="xx")


def test_page_keeps_the_json_field_names_the_frontend_expects():
    page = Page[int](items=[1], total=1, page=1, size=20, total_pages=1)
    assert set(page.model_dump()) == {"items", "total", "page", "size", "total_pages"}