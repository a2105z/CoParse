# CoParse

**Contract safety for students, renters, and early-career workers** — an Android app plus API that highlights risky or unusual clauses, explains them in plain English, and suggests questions to ask before you sign.

This repository is a **monorepo**:

- [`android/`](android/) — Kotlin, Jetpack Compose, Material 3 (client)
- [`backend/`](backend/) — FastAPI, PostgreSQL, document analysis pipeline
- [`docs/`](docs/) — Product positioning, architecture, API notes

> CoParse provides educational information only and does not provide legal advice. See [`docs/PRODUCT.md`](docs/PRODUCT.md).

## Prerequisites

- **Android:** Android Studio (Ladybug+), JDK 17, Android SDK
- **Backend:** Python 3.11+, [Docker](https://www.docker.com/) (for PostgreSQL)
- **Git**

## Quick start

### 1. Database

```bash
docker compose up -d
```

### 2. Backend

```bash
cd backend
python -m venv .venv
# Windows:
.venv\Scripts\activate
pip install -r requirements.txt
copy ..\.env.example .env
# Edit .env if needed (defaults match docker-compose)

alembic upgrade head
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

- API: http://localhost:8000  
- OpenAPI: http://localhost:8000/docs  

### 3. Android

Create `android/local.properties` with `sdk.dir` (see [`android/local.properties.example`](android/local.properties.example) and [`android/README.md`](android/README.md)).

Open the `android/` folder in Android Studio, sync Gradle, run the `app` configuration.

For a **physical device**, set `coparse.apiBaseUrl` in `local.properties` to your machine’s LAN IP and port (not `localhost`).

## API overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/documents` | Multipart upload (`file`, optional `hint_contract_type`, `hint_role`) |
| GET | `/v1/jobs/{id}` | Job status |
| GET | `/v1/documents/{id}/analysis` | Full analysis JSON |
| POST | `/v1/documents/{id}/reanalyze` | Re-run with `contract_type` + `role` |

Details: [`docs/API.md`](docs/API.md).

## License

TBD.
