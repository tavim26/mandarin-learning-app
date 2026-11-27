# tests/test_nlp.py
from app.services.hsk import HskVocabulary
from app.services.nlp import MAX_TOKEN_LENGTH, NlpProcessor, WordToken


class FakeSegmenter:
    def __init__(self, words):
        self._words = words

    def segment(self, text):
        return self._words


def processor(words):
    return NlpProcessor(
        HskVocabulary({"你好": 1}), FakeSegmenter(words), romanizer=lambda word: f"py({word})"
    )


def test_tokens_are_enriched_and_numbered_without_gaps():
    tokens = processor([("你好", "INTJ"), (" ", "SPACE"), ("，", "PUNCT"), ("龘", "NOUN")]).process("x")

    assert [t.hanzi for t in tokens] == ["你好", "，", "龘"]
    assert [t.position_index for t in tokens] == [0, 1, 2]
    assert tokens[0] == WordToken("你好", "py(你好)", 1, 0, "interjectie")
    assert tokens[1].pos == "punctuatie"
    assert tokens[2].hsk_level is None


def test_unknown_pos_tags_are_kept_as_they_are():
    assert processor([("你", "WEIRD")]).process("x")[0].pos == "WEIRD"


def test_overlong_words_are_cut_to_the_column_size():
    tokens = processor([("a" * 500, "X")]).process("x")
    assert len(tokens[0].hanzi) == MAX_TOKEN_LENGTH


def test_punctuation_is_not_translatable():
    tokens = processor([("，", "PUNCT"), ("你好", "INTJ"), ("123", "NUM")]).process("x")
    assert [t.is_translatable for t in tokens] == [False, True, True]