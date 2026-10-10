# ===== BUILD STAGE =====
FROM python:3.12-slim AS build

WORKDIR /app

RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*


COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

RUN python -m spacy download zh_core_web_md


# ===== RUNTIME STAGE =====
FROM python:3.12-slim

WORKDIR /app

COPY --from=build /usr/local/lib/python3.12/site-packages \
                  /usr/local/lib/python3.12/site-packages

COPY --from=build /usr/local/bin/uvicorn /usr/local/bin/uvicorn

COPY . .

EXPOSE 8085

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8085", "--proxy-headers", "--forwarded-allow-ips", "*"]