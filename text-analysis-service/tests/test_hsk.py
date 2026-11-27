# tests/test_hsk.py
import json

import pytest

from app.services.hsk import HskVocabulary, dominant_level


def test_most_frequent_level_wins():
    assert dominant_level([1, 2, 2, None]) == 2


def test_higher_level_wins_a_tie():
    assert dominant_level([1, 3, 1, 3]) == 3


def test_no_levels_gives_none():
    assert dominant_level([None, None]) is None
    assert dominant_level([]) is None


def test_vocabulary_lookup_and_totals():
    vocabulary = HskVocabulary({"你": 1, "好": 1, "谢谢": 2})
    assert vocabulary.level_of("你") == 1
    assert vocabulary.level_of("龘") is None
    assert vocabulary.total_words_per_level() == {1: 2, 2: 1}


def test_vocabulary_loads_from_file(tmp_path):
    path = tmp_path / "words.json"
    path.write_text(json.dumps({"你": 1}), encoding="utf-8")
    assert HskVocabulary.from_file(path).level_of("你") == 1


def test_missing_vocabulary_file_gives_a_clear_error(tmp_path):
    with pytest.raises(RuntimeError, match="not found"):
        HskVocabulary.from_file(tmp_path / "missing.json")