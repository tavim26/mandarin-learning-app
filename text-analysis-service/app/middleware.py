# app/middleware.py
from starlette.types import ASGIApp, Receive, Scope, Send

from app.problems import problem_response


class ContentLengthLimitMiddleware:
    """Rejects requests that announce a body larger than the limit, before the body is read.

    FastAPI parses multipart uploads before dependencies run, so an oversized upload must be
    stopped here. Requests without a Content-Length are still capped where the body is read.
    """

    def __init__(self, app: ASGIApp, max_body_bytes: int):
        self._app = app
        self._max_body_bytes = max_body_bytes

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] == "http" and self._announced_size(scope) > self._max_body_bytes:
            response = problem_response(413, "Payload Too Large", "The request body is too large")
            await response(scope, receive, send)
            return
        await self._app(scope, receive, send)

    @staticmethod
    def _announced_size(scope: Scope) -> int:
        for name, value in scope["headers"]:
            if name == b"content-length":
                try:
                    return int(value)
                except ValueError:
                    return 0
        return 0