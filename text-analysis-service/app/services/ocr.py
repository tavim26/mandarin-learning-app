# app/services/ocr.py
import io
import logging
import threading
from typing import Any, Callable

import numpy as np
from PIL import Image, ImageOps, UnidentifiedImageError

from app.errors import InvalidImageError, ServiceBusyError

logger = logging.getLogger(__name__)

ALLOWED_IMAGE_FORMATS = {"PNG", "JPEG", "WEBP", "BMP"}


def load_image(data: bytes, *, max_pixels: int, max_side: int) -> Image.Image:
    """Validates an uploaded image and prepares it for OCR (RGB, upright, bounded size).

    The pixel count is checked from the header, before the image is decoded, so a small file
    that expands to a huge bitmap is rejected cheaply.
    """
    try:
        image = Image.open(io.BytesIO(data))
        if image.format not in ALLOWED_IMAGE_FORMATS:
            raise InvalidImageError("Unsupported image format. Use PNG, JPEG, WEBP or BMP")
        width, height = image.size
        if width * height > max_pixels:
            raise InvalidImageError("The image resolution is too large")
        image = ImageOps.exif_transpose(image).convert("RGB")
    except InvalidImageError:
        raise
    except (UnidentifiedImageError, Image.DecompressionBombError, OSError, ValueError):
        raise InvalidImageError("The provided file is not a valid image") from None

    image.thumbnail((max_side, max_side))
    return image


class OcrReader:
    """EasyOCR for simplified Chinese. The model loads on first use; recognitions run one at a time."""

    def __init__(
        self,
        *,
        max_image_pixels: int,
        max_side: int,
        max_concurrency: int,
        queue_timeout_seconds: float,
        model_dir: str | None = None,
        reader_factory: Callable[[], Any] | None = None,
    ):
        self._max_pixels = max_image_pixels
        self._max_side = max_side
        self._queue_timeout = queue_timeout_seconds
        self._slots = threading.BoundedSemaphore(max_concurrency)
        self._reader_factory = reader_factory or (lambda: _create_easyocr_reader(model_dir))
        self._reader: Any = None
        self._load_lock = threading.Lock()

    def extract_text(self, image_bytes: bytes) -> str:
        image = load_image(image_bytes, max_pixels=self._max_pixels, max_side=self._max_side)

        if not self._slots.acquire(timeout=self._queue_timeout):
            raise ServiceBusyError("Text recognition is busy. Please try again in a moment")
        try:
            results = self._get_reader().readtext(np.array(image), detail=1)
        except Exception:
            logger.exception("OCR failed")
            raise InvalidImageError("Text could not be extracted from the image") from None
        finally:
            self._slots.release()

        # Reading order: top to bottom, then left to right.
        results.sort(key=lambda r: (r[0][0][1], r[0][0][0]))
        return "".join(text for _, text, _ in results)

    def _get_reader(self) -> Any:
        if self._reader is None:
            with self._load_lock:
                if self._reader is None:
                    self._reader = self._reader_factory()
        return self._reader


def _create_easyocr_reader(model_dir: str | None) -> Any:
    import easyocr  # heavy import, only when OCR is really used

    options: dict[str, Any] = {"gpu": False}
    if model_dir:
        options["model_storage_directory"] = model_dir
    return easyocr.Reader(["ch_sim"], **options)