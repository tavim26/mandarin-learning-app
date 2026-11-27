# app/services/rate_limiter.py
import threading
import time
from collections import deque
from typing import Callable

from app.errors import RateLimitExceededError


class SlidingWindowRateLimiter:

    _SWEEP_THRESHOLD = 10_000

    def __init__(
        self,
        max_requests: int,
        window_seconds: float,
        clock: Callable[[], float] = time.monotonic,
    ):
        self._max_requests = max_requests
        self._window = window_seconds
        self._clock = clock
        self._hits: dict[int, deque[float]] = {}
        self._lock = threading.Lock()

    def check(self, key: int) -> None:
        now = self._clock()
        with self._lock:
            if len(self._hits) > self._SWEEP_THRESHOLD:
                self._sweep(now)
            hits = self._hits.setdefault(key, deque())
            self._drop_expired(hits, now)
            if len(hits) >= self._max_requests:
                retry_after = int(self._window - (now - hits[0])) + 1
                raise RateLimitExceededError(max(1, retry_after))
            hits.append(now)

    def _drop_expired(self, hits: deque[float], now: float) -> None:
        while hits and now - hits[0] >= self._window:
            hits.popleft()

    def _sweep(self, now: float) -> None:
        for key in list(self._hits):
            self._drop_expired(self._hits[key], now)
            if not self._hits[key]:
                del self._hits[key]