# tests/test_rate_limiter.py
import pytest

from app.errors import RateLimitExceededError
from app.services.rate_limiter import SlidingWindowRateLimiter


class FakeClock:
    def __init__(self) -> None:
        self.now = 0.0

    def __call__(self) -> float:
        return self.now


def test_blocks_the_request_over_the_limit_and_reports_when_to_retry():
    clock = FakeClock()
    limiter = SlidingWindowRateLimiter(2, 60, clock)
    limiter.check(1)
    clock.now = 10
    limiter.check(1)
    clock.now = 20

    with pytest.raises(RateLimitExceededError) as error:
        limiter.check(1)

    assert error.value.retry_after_seconds == 41


def test_allows_requests_again_once_the_window_has_passed():
    clock = FakeClock()
    limiter = SlidingWindowRateLimiter(1, 60, clock)
    limiter.check(1)
    clock.now = 60
    limiter.check(1)


def test_limits_each_student_separately():
    limiter = SlidingWindowRateLimiter(1, 60, FakeClock())
    limiter.check(1)
    limiter.check(2)
    with pytest.raises(RateLimitExceededError):
        limiter.check(1)


def test_rejected_requests_do_not_extend_the_block():
    clock = FakeClock()
    limiter = SlidingWindowRateLimiter(1, 60, clock)
    limiter.check(1)
    for second in (10, 20, 30):
        clock.now = second
        with pytest.raises(RateLimitExceededError):
            limiter.check(1)
    clock.now = 60
    limiter.check(1)