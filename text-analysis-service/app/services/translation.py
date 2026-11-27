# app/services/translation.py
import logging
from typing import Sequence

import requests

from app.errors import TranslationUnavailableError

logger = logging.getLogger(__name__)


class GoogleTranslateClient:

    # Limit of the API: more than 128 text segments in one request is rejected.
    MAX_SEGMENTS_PER_REQUEST = 128

    def __init__(
        self,
        api_key: str,
        base_url: str,
        timeout_seconds: float,
        session: requests.Session | None = None,
    ):
        self._api_key = api_key
        self._base_url = base_url
        self._timeout = timeout_seconds
        self._session = session or requests.Session()

    def translate(self, texts: Sequence[str], target_language: str) -> list[str]:
        """Translates every text and returns the results in the same order."""
        translations: list[str] = []
        for start in range(0, len(texts), self.MAX_SEGMENTS_PER_REQUEST):
            chunk = texts[start:start + self.MAX_SEGMENTS_PER_REQUEST]
            translations.extend(self._translate_chunk(chunk, target_language))
        return translations

    def _translate_chunk(self, texts: Sequence[str], target_language: str) -> list[str]:
        payload = {"q": list(texts), "source": "zh-CN", "target": target_language, "format": "text"}
        try:
            response = self._session.post(
                self._base_url,
                params={"key": self._api_key},
                json=payload,
                timeout=self._timeout,
            )
        except requests.RequestException as exc:
            # Never log the exception text: it contains the request URL, and the URL contains the API key.
            logger.warning("Translation request failed: %s", type(exc).__name__)
            raise TranslationUnavailableError("The translation service is unavailable") from None

        if response.status_code != 200:
            logger.warning("Translation API answered with status %s", response.status_code)
            raise TranslationUnavailableError("The translation service is unavailable")

        try:
            items = response.json()["data"]["translations"]
            result = [item["translatedText"] for item in items]
        except (KeyError, TypeError, ValueError):
            logger.warning("Translation API answered with an unexpected body")
            raise TranslationUnavailableError("The translation service is unavailable") from None

        if len(result) != len(texts):
            logger.warning("Translation API returned %d items for %d texts", len(result), len(texts))
            raise TranslationUnavailableError("The translation service is unavailable")
        return result


def translate_text_and_words(
    client: GoogleTranslateClient,
    text: str,
    words: Sequence[str],
    target_language: str,
) -> tuple[str, dict[str, str]]:
    """Translates the whole text and each distinct word (a repeated word is paid for once)."""
    distinct_words = list(dict.fromkeys(words))
    translated_text, *translated_words = client.translate([text, *distinct_words], target_language)
    return translated_text, dict(zip(distinct_words, translated_words))