# tests/test_translation.py
import logging

import pytest
import requests

from app.errors import TranslationUnavailableError
from app.services.translation import GoogleTranslateClient, translate_text_and_words


class FakeResponse:
    def __init__(self, status_code=200, body=None):
        self.status_code = status_code
        self._body = body

    def json(self):
        if self._body is None:
            raise ValueError("no json")
        return self._body


class FakeSession:
    """Echoes every text as '<text>!' and records the requests."""

    def __init__(self, response=None, error=None):
        self.calls = []
        self._response = response
        self._error = error

    def post(self, url, params, json, timeout):
        self.calls.append({"url": url, "params": params, "json": json, "timeout": timeout})
        if self._error:
            raise self._error
        if self._response:
            return self._response
        translations = [{"translatedText": f"{text}!"} for text in json["q"]]
        return FakeResponse(200, {"data": {"translations": translations}})


def make_client(session):
    return GoogleTranslateClient("SECRET-KEY", "https://example.test/v2", 5, session)


def test_translations_keep_the_order_of_the_input():
    assert make_client(FakeSession()).translate(["a", "b"], "ro") == ["a!", "b!"]


def test_sends_a_json_body_with_source_and_target():
    session = FakeSession()
    make_client(session).translate(["a"], "ro")
    call = session.calls[0]
    assert call["json"] == {"q": ["a"], "source": "zh-CN", "target": "ro", "format": "text"}
    assert call["timeout"] == 5


def test_splits_requests_at_128_segments():
    session = FakeSession()
    texts = [str(i) for i in range(300)]

    result = make_client(session).translate(texts, "en")

    assert [len(call["json"]["q"]) for call in session.calls] == [128, 128, 44]
    assert result == [f"{text}!" for text in texts]


def test_text_and_words_are_translated_with_repeated_words_sent_once():
    session = FakeSession()

    text, words = translate_text_and_words(make_client(session), "你好你", ["你", "好", "你"], "en")

    assert text == "你好你!"
    assert words == {"你": "你!", "好": "好!"}
    assert session.calls[0]["json"]["q"] == ["你好你", "你", "好"]


def test_upstream_errors_are_hidden_from_the_caller():
    session = FakeSession(FakeResponse(403, {"error": "API key not valid: SECRET-KEY"}))
    with pytest.raises(TranslationUnavailableError) as error:
        make_client(session).translate(["a"], "en")
    assert "SECRET-KEY" not in error.value.detail


def test_network_failure_does_not_leak_the_key_in_logs(caplog):
    failure = requests.ConnectionError("failed: https://example.test/v2?key=SECRET-KEY")
    with caplog.at_level(logging.DEBUG):
        with pytest.raises(TranslationUnavailableError):
            make_client(FakeSession(error=failure)).translate(["a"], "en")
    assert "SECRET-KEY" not in caplog.text


@pytest.mark.parametrize("body", [{}, {"data": {}}, {"data": {"translations": [{"x": 1}]}}, None])
def test_malformed_responses_are_rejected(body):
    with pytest.raises(TranslationUnavailableError):
        make_client(FakeSession(FakeResponse(200, body))).translate(["a"], "en")


def test_wrong_number_of_translations_is_rejected():
    body = {"data": {"translations": [{"translatedText": "only one"}]}}
    with pytest.raises(TranslationUnavailableError):
        make_client(FakeSession(FakeResponse(200, body))).translate(["a", "b"], "en")