# app/problems.py
import logging
from typing import Any

from starlette.requests import Request
from starlette.responses import JSONResponse

from app.errors import AppError, RateLimitExceededError

logger = logging.getLogger(__name__)

PROBLEM_JSON = "application/problem+json"


def problem_response(
    status: int,
    title: str,
    detail: str,
    headers: dict[str, str] | None = None,
    **extra: Any,
) -> JSONResponse:
    body = {"type": "about:blank", "title": title, "status": status, "detail": detail, **extra}
    return JSONResponse(body, status_code=status, headers=headers, media_type=PROBLEM_JSON)


async def handle_app_error(_: Request, exc: AppError) -> JSONResponse:
    headers = None
    if isinstance(exc, RateLimitExceededError):
        headers = {"Retry-After": str(exc.retry_after_seconds)}
    return problem_response(exc.status_code, exc.title, exc.detail, headers)


async def handle_unexpected_error(request: Request, exc: Exception) -> JSONResponse:
    logger.error("Unhandled error on %s %s", request.method, request.url.path, exc_info=exc)
    return problem_response(500, "Internal Server Error", "An unexpected error occurred")