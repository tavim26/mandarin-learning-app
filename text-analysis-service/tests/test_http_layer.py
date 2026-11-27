# tests/test_http_layer.py
import asyncio
import json

from app.errors import ForbiddenError, RateLimitExceededError
from app.middleware import ContentLengthLimitMiddleware
from app.problems import handle_app_error, handle_unexpected_error


def call(app, headers=None):
    """Runs one HTTP request through an ASGI app and returns (status, headers, body)."""
    sent = []
    scope = {
        "type": "http", "method": "POST", "path": "/", "query_string": b"",
        "headers": [(k.encode(), v.encode()) for k, v in (headers or {}).items()],
    }

    async def receive():
        return {"type": "http.request", "body": b"", "more_body": False}

    async def send(message):
        sent.append(message)

    asyncio.run(app(scope, receive, send))
    start = next(m for m in sent if m["type"] == "http.response.start")
    body = b"".join(m.get("body", b"") for m in sent if m["type"] == "http.response.body")
    return start["status"], dict(start["headers"]), body


async def ok_app(scope, receive, send):
    await send({"type": "http.response.start", "status": 200, "headers": []})
    await send({"type": "http.response.body", "body": b"ok"})


def test_oversized_request_is_rejected_before_reaching_the_app():
    app = ContentLengthLimitMiddleware(ok_app, max_body_bytes=100)
    status, headers, body = call(app, {"content-length": "101"})
    assert status == 413
    assert headers[b"content-type"] == b"application/problem+json"
    assert json.loads(body)["status"] == 413


def test_request_within_the_limit_passes_through():
    app = ContentLengthLimitMiddleware(ok_app, max_body_bytes=100)
    assert call(app, {"content-length": "100"})[0] == 200
    assert call(app)[0] == 200


def test_malformed_content_length_does_not_crash():
    app = ContentLengthLimitMiddleware(ok_app, max_body_bytes=100)
    assert call(app, {"content-length": "abc"})[0] == 200


def test_app_error_becomes_problem_json():
    response = asyncio.run(handle_app_error(None, ForbiddenError("nope")))
    body = json.loads(response.body)
    assert response.status_code == 403
    assert body["detail"] == "nope" and body["title"] == "Forbidden"


def test_rate_limit_error_carries_retry_after():
    response = asyncio.run(handle_app_error(None, RateLimitExceededError(12)))
    assert response.status_code == 429
    assert response.headers["retry-after"] == "12"


def test_unexpected_error_hides_the_details():
    class Req:
        method = "GET"

        class url:
            path = "/x"

    response = asyncio.run(handle_unexpected_error(Req(), RuntimeError("db password is hunter2")))
    assert response.status_code == 500
    assert "hunter2" not in response.body.decode()