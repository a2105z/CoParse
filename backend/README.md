# CoParse backend

FastAPI service for document upload, async analysis jobs, and JSON results.

## Setup

```bash
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
```

Copy `../.env.example` to `.env` and ensure `DATABASE_URL` matches Docker Postgres (see root `README.md`).

```bash
alembic upgrade head
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## Tests

```bash
pytest
```

## Storage

Uploaded files are stored under `STORAGE_PATH` (default `./storage`).
