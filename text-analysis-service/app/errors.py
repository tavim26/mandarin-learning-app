# app/errors.py

class AppError(Exception):
    status_code = 500
    title = "Internal Server Error"

    def __init__(self, detail: str):
        super().__init__(detail)
        self.detail = detail


class NotFoundError(AppError):
    status_code = 404
    title = "Not Found"


class UnauthorizedError(AppError):
    status_code = 401
    title = "Unauthorized"


class ForbiddenError(AppError):
    status_code = 403
    title = "Forbidden"


class PayloadTooLargeError(AppError):
    status_code = 413
    title = "Payload Too Large"


class InvalidImageError(AppError):
    status_code = 422
    title = "Unprocessable Image"


class NoTextDetectedError(AppError):
    status_code = 422
    title = "No Text Detected"


class RateLimitExceededError(AppError):
    status_code = 429
    title = "Too Many Requests"

    def __init__(self, retry_after_seconds: int):
        super().__init__("Too many requests. Please wait before trying again")
        self.retry_after_seconds = retry_after_seconds


class TranslationUnavailableError(AppError):
    status_code = 503
    title = "Translation Unavailable"


class ServiceBusyError(AppError):
    status_code = 503
    title = "Service Busy"