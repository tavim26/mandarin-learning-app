# app/main.py
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.config import Settings, get_settings
from app.database import Base, create_db_engine, create_session_factory
from app.dependencies import Container
from app.handlers import register_exception_handlers
from app.logging_config import configure_logging
from app.middleware import ContentLengthLimitMiddleware
from app.models import analysis as _models  # noqa: F401  (registers the tables on Base.metadata)
from app.routers import analysis
from app.security import AnalysisAccessPolicy
from app.services.hsk import HskVocabulary
from app.services.nlp import NlpProcessor, SpacySegmenter
from app.services.ocr import OcrReader
from app.services.rate_limiter import SlidingWindowRateLimiter
from app.services.translation import GoogleTranslateClient

logger = logging.getLogger(__name__)

MULTIPART_OVERHEAD_BYTES = 64 * 1024


def build_container(settings: Settings) -> Container:
    engine = create_db_engine(settings.database_url)
    Base.metadata.create_all(engine)

    vocabulary = HskVocabulary.from_file()
    return Container(
        settings=settings,
        session_factory=create_session_factory(engine),
        vocabulary=vocabulary,
        nlp=NlpProcessor(vocabulary, SpacySegmenter()),
        ocr=OcrReader(
            max_image_pixels=settings.max_image_pixels,
            max_side=settings.ocr_max_side,
            max_concurrency=settings.ocr_max_concurrency,
            queue_timeout_seconds=settings.ocr_queue_timeout_seconds,
            model_dir=settings.ocr_model_dir,
        ),
        translator=GoogleTranslateClient(
            api_key=settings.google_translate_api_key,
            base_url=settings.translation_base_url,
            timeout_seconds=settings.translation_timeout_seconds,
        ),
        policy=AnalysisAccessPolicy(),
        analysis_limiter=SlidingWindowRateLimiter(settings.analysis_requests_per_minute, 60),
        preview_limiter=SlidingWindowRateLimiter(settings.preview_requests_per_minute, 60),
    )


def create_app() -> FastAPI:
    settings = get_settings()
    configure_logging(settings.log_level)

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        app.state.container = build_container(settings)
        logger.info("Text analysis service started")
        yield

    app = FastAPI(title="Text Analysis Service", lifespan=lifespan)
    app.add_middleware(
        ContentLengthLimitMiddleware,
        max_body_bytes=settings.max_upload_bytes + MULTIPART_OVERHEAD_BYTES,
    )
    register_exception_handlers(app)
    app.include_router(analysis.router)

    @app.get("/health", include_in_schema=False)
    def health() -> dict[str, str]:
        return {"status": "UP"}

    return app


app = create_app()