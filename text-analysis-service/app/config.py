# app/config.py
"""Application settings, read once from environment variables."""
import os
from dataclasses import dataclass
from functools import lru_cache
from typing import Mapping

from dotenv import load_dotenv


@dataclass(frozen=True)
class Settings:

    database_url: str
    google_translate_api_key: str
    log_level: str = "INFO"

    max_upload_bytes: int = 5 * 1024 * 1024
    max_image_pixels: int = 25_000_000
    ocr_max_side: int = 2000
    ocr_model_dir: str | None = None
    ocr_max_concurrency: int = 1
    ocr_queue_timeout_seconds: float = 30.0

    translation_timeout_seconds: float = 10.0
    translation_base_url: str = "https://translation.googleapis.com/language/translate/v2"

    analysis_requests_per_minute: int = 10
    preview_requests_per_minute: int = 60

    @classmethod
    def from_env(cls, env: Mapping[str, str]) -> "Settings":
        return cls(
            database_url=_required(env, "DATABASE_URL"),
            google_translate_api_key=_required(env, "GOOGLE_TRANSLATE_API_KEY"),
            log_level=env.get("LOG_LEVEL", "INFO").upper(),
            max_upload_bytes=int(env.get("MAX_UPLOAD_BYTES", cls.max_upload_bytes)),
            max_image_pixels=int(env.get("MAX_IMAGE_PIXELS", cls.max_image_pixels)),
            ocr_max_side=int(env.get("OCR_MAX_SIDE", cls.ocr_max_side)),
            ocr_model_dir=env.get("OCR_MODEL_DIR") or None,
            ocr_max_concurrency=int(env.get("OCR_MAX_CONCURRENCY", cls.ocr_max_concurrency)),
            ocr_queue_timeout_seconds=float(
                env.get("OCR_QUEUE_TIMEOUT_SECONDS", cls.ocr_queue_timeout_seconds)
            ),
            analysis_requests_per_minute=int(
                env.get("ANALYSIS_REQUESTS_PER_MINUTE", cls.analysis_requests_per_minute)
            ),
            preview_requests_per_minute=int(
                env.get("PREVIEW_REQUESTS_PER_MINUTE", cls.preview_requests_per_minute)
            ),
        )


def _required(env: Mapping[str, str], name: str) -> str:
    value = env.get(name, "").strip()
    if not value:
        raise RuntimeError(f"Environment variable {name} is required but not set")
    return value


@lru_cache
def get_settings() -> Settings:
    load_dotenv()
    return Settings.from_env(os.environ)