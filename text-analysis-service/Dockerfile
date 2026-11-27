# Dockerfile
# ===== BUILD STAGE =====
FROM python:3.12-slim AS build

WORKDIR /app

RUN apt-get update && apt-get install -y gcc g++ && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

RUN python -m spacy download zh_core_web_md


# ===== RUNTIME STAGE =====
FROM python:3.12-slim

RUN useradd --create-home --uid 10001 app \
    && mkdir -p /models/easyocr \
    && chown app:app /models/easyocr

WORKDIR /app

COPY --from=build /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=build /usr/local/bin/uvicorn /usr/local/bin/uvicorn

COPY app ./app
COPY resources ./resources

ENV OCR_MODEL_DIR=/models/easyocr

USER app
EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8085/health', timeout=3)"

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8085", "--proxy-headers", "--forwarded-allow-ips", "*"]