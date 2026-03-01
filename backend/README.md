# CoParse Backend

FastAPI server providing the REST API for CoParse.

## Setup

```bash
cd backend
python -m venv venv
venv\Scripts\activate        # Windows
# source venv/bin/activate   # macOS/Linux
pip install -r requirements.txt
```

## Run

```bash
uvicorn app.main:app --reload
```

- API: http://localhost:8000
- Interactive docs: http://localhost:8000/docs

## Test

```bash
pytest
```

## Structure

```
backend/
├── app/
│   ├── main.py           # FastAPI app entry point
│   ├── config.py          # Environment config
│   ├── routes/            # API endpoint definitions
│   │   ├── search.py      # Semantic search endpoints
│   │   ├── analysis.py    # Contract analysis endpoints
│   │   └── contracts.py   # Contract CRUD endpoints
│   ├── models/
│   │   └── schemas.py     # Pydantic request/response models
│   ├── services/          # Business logic layer
│   │   ├── search_service.py
│   │   └── analysis_service.py
│   └── utils/
│       └── helpers.py     # Shared utilities
└── tests/
    └── test_routes.py     # API tests
```
