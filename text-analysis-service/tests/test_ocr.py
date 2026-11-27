# tests/test_ocr.py
import io

import pytest
from PIL import Image

from app.errors import InvalidImageError, ServiceBusyError
from app.services.ocr import OcrReader, load_image


def image_bytes(size=(40, 20), fmt="PNG", mode="RGB") -> bytes:
    buffer = io.BytesIO()
    Image.new(mode, size, "white").save(buffer, fmt)
    return buffer.getvalue()


def test_valid_image_is_converted_to_rgb():
    image = load_image(image_bytes(mode="RGBA"), max_pixels=10_000, max_side=100)
    assert image.mode == "RGB"


def test_large_image_is_scaled_down():
    image = load_image(image_bytes((400, 200)), max_pixels=1_000_000, max_side=100)
    assert max(image.size) == 100


def test_too_many_pixels_is_rejected_before_decoding():
    with pytest.raises(InvalidImageError, match="resolution"):
        load_image(image_bytes((200, 100)), max_pixels=1_000, max_side=100)


def test_garbage_is_rejected():
    with pytest.raises(InvalidImageError, match="not a valid image"):
        load_image(b"not an image", max_pixels=1_000, max_side=100)


def test_truncated_image_is_rejected():
    with pytest.raises(InvalidImageError):
        load_image(image_bytes((200, 200))[:80], max_pixels=1_000_000, max_side=100)


def test_unlisted_format_is_rejected():
    with pytest.raises(InvalidImageError, match="Unsupported"):
        load_image(image_bytes(fmt="GIF"), max_pixels=10_000, max_side=100)


class FakeEasyOcr:
    def readtext(self, array, detail=1):
        # (bounding box, text, confidence); boxes are [[x, y], ...]
        return [
            ([[0, 50], [10, 50]], "世界", 0.9),
            ([[0, 0], [10, 0]], "你好", 0.9),
        ]


def make_reader(factory, **overrides):
    options = dict(max_image_pixels=10_000, max_side=100, max_concurrency=1, queue_timeout_seconds=0.05)
    options.update(overrides)
    return OcrReader(reader_factory=factory, **options)


def test_text_is_read_top_to_bottom():
    assert make_reader(FakeEasyOcr).extract_text(image_bytes()) == "你好世界"


def test_model_loads_once_and_only_on_first_use():
    loads = []

    def factory():
        loads.append(1)
        return FakeEasyOcr()

    reader = make_reader(factory)
    assert loads == []
    reader.extract_text(image_bytes())
    reader.extract_text(image_bytes())
    assert loads == [1]


def test_busy_reader_answers_503_instead_of_queueing_forever():
    reader = make_reader(FakeEasyOcr)
    reader._slots.acquire()  # simulate a recognition in progress
    with pytest.raises(ServiceBusyError):
        reader.extract_text(image_bytes())


def test_slot_is_released_after_a_recognition_failure():
    class Broken:
        def readtext(self, array, detail=1):
            raise RuntimeError("model crashed")

    reader = make_reader(Broken)
    with pytest.raises(InvalidImageError):
        reader.extract_text(image_bytes())
    assert reader._slots.acquire(timeout=0.01)