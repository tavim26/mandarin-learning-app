# app/services/hsk.py
import json
from collections import Counter
from pathlib import Path
from typing import Iterable, Mapping

DEFAULT_VOCABULARY_PATH = Path(__file__).resolve().parents[2] / "resources" / "hsk_words.json"


class HskVocabulary:
    """HSK level lookup for Chinese words."""

    def __init__(self, levels_by_word: Mapping[str, int]):
        self._levels = dict(levels_by_word)
        self._totals = dict(Counter(self._levels.values()))

    @classmethod
    def from_file(cls, path: Path = DEFAULT_VOCABULARY_PATH) -> "HskVocabulary":
        try:
            with path.open(encoding="utf-8") as file:
                return cls(json.load(file))
        except FileNotFoundError:
            raise RuntimeError(f"HSK vocabulary file not found: {path}") from None
        except json.JSONDecodeError as exc:
            raise RuntimeError(f"HSK vocabulary file is not valid JSON: {exc}") from exc

    def level_of(self, word: str) -> int | None:
        return self._levels.get(word)

    def total_words_per_level(self) -> dict[int, int]:
        return dict(self._totals)


def dominant_level(levels: Iterable[int | None]) -> int | None:
    """Most frequent HSK level; on a tie the higher level wins. None when no word has a level."""
    counts = Counter(level for level in levels if level is not None)
    if not counts:
        return None
    return max(counts, key=lambda level: (counts[level], level))