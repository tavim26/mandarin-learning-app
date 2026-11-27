# app/handlers.py
from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from starlette.requests import Request
from starlette.responses import JSONResponse

from app.errors import AppError
from app.problems import handle_app_error, handle_unexpected_error, problem_response


async def handle_validation_error(_: Request, exc: RequestValidationError) -> JSONResponse:
    errors = [
        {"field": ".".join(str(part) for part in error["loc"]), "message": error["msg"]}
        for error in exc.errors()
    ]
    return problem_response(422, "Validation Failed", "The request is not valid", errors=errors)


async def handle_http_exception(_: Request, exc: StarletteHTTPException) -> JSONResponse:
    return problem_response(exc.status_code, "Request Failed", str(exc.detail), dict(exc.headers or {}))


def register_exception_handlers(app: FastAPI) -> None:
    app.add_exception_handler(AppError, handle_app_error)
    app.add_exception_handler(RequestValidationError, handle_validation_error)
    app.add_exception_handler(StarletteHTTPException, handle_http_exception)
    app.add_exception_handler(Exception, handle_unexpected_error)