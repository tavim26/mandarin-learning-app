# app/logging_config.py
import logging


def configure_logging(level: str) -> None:
    logging.basicConfig(level=level, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    for noisy in ("urllib3", "requests", "httpx", "httpcore"):
        logging.getLogger(noisy).setLevel(logging.WARNING)