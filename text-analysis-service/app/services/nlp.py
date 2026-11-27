# app/services/nlp.py
from dataclasses import dataclass
from typing import Callable, Iterable, Protocol

from app.services.hsk import HskVocabulary

# Universal Dependencies POS tags -> labels shown to students.
# https://universaldependencies.org/u/pos/
POS_LABELS: dict[str, str] = {
    "NOUN": "substantiv",
    "VERB": "verb",
    "ADJ": "adjectiv",
    "ADV": "adverb",
    "PRON": "pronume",
    "PROPN": "nume propriu",
    "NUM": "numar",
    "PART": "particula",
    "ADP": "prepozitie",
    "CONJ": "conjunctie",
    "CCONJ": "conjunctie",
    "SCONJ": "conjunctie",
    "DET": "determinant",
    "AUX": "auxiliar",
    "INTJ": "interjectie",
    "PUNCT": "punctuatie",
    "SYM": "simbol",
    "X": "necunoscut",
}

# Length of the `analysis_tokens.hanzi` column.
MAX_TOKEN_LENGTH = 100


@dataclass(frozen=True)
class WordToken:
    hanzi: str
    pinyin: str
    hsk_level: int | None
    position_index: int
    pos: str | None

    @property
    def is_translatable(self) -> bool:
        """Pure punctuation has nothing to translate."""
        return any(char.isalnum() for char in self.hanzi)


class Segmenter(Protocol):
    def segment(self, text: str) -> Iterable[tuple[str, str]]:
        """Yields (word, universal POS tag) pairs."""



class SpacySegmenter:
    def __init__(self, model_name: str = "zh_core_web_md"):
        import spacy  # heavy import, kept out of the module level

        self._nlp = spacy.load(model_name)

    def segment(self, text: str) -> Iterable[tuple[str, str]]:
        return [(token.text, token.pos_) for token in self._nlp(text)]


    def to_pinyin(word: str) -> str:
        from pypinyin import Style, pinyin

        return " ".join(syllable[0] for syllable in pinyin(word, style=Style.TONE))




class NlpProcessor:
    """Splits Chinese text into words and enriches each with pinyin, HSK level and part of speech."""

    def __init__(
        self,
        vocabulary: HskVocabulary,
        segmenter: Segmenter,
        romanizer: Callable[[str], str] = to_pinyin,
    ):
        self._vocabulary = vocabulary
        self._segmenter = segmenter
        self._romanizer = romanizer

    def process(self, text: str) -> list[WordToken]:
        tokens: list[WordToken] = []
        for raw_word, pos_tag in self._segmenter.segment(text):
            word = raw_word.strip()[:MAX_TOKEN_LENGTH]
            if not word:
                continue
            tokens.append(
                WordToken(
                    hanzi=word,
                    pinyin=self._romanizer(word),
                    hsk_level=self._vocabulary.level_of(word),
                    position_index=len(tokens),
                    pos=POS_LABELS.get(pos_tag, pos_tag),
                )
            )
        return tokens