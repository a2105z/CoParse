# CoParse Architecture

## High-Level Overview

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│              │     │              │     │                  │
│   React UI   │────▶│  FastAPI     │────▶│  ML Pipeline     │
│  (Frontend)  │◀────│  (Backend)   │◀────│  (Python/NLP)    │
│              │     │              │     │                  │
└──────────────┘     └──────┬───────┘     └────────┬─────────┘
                            │                      │
                     ┌──────▼───────┐     ┌────────▼─────────┐
                     │  PostgreSQL  │     │  FAISS Vector    │
                     │  (Database)  │     │  Index (Search)  │
                     └──────────────┘     └──────────────────┘
```

## Component Breakdown

### Frontend (React)
- **Purpose:** User interface for searching, uploading, and viewing analysis
- **Key pages:** Search, Upload & Analyze, Results Detail
- **Communicates with:** Backend API via REST (JSON)

### Backend (FastAPI)
- **Purpose:** REST API layer connecting frontend to ML and database
- **Key routes:**
  - `GET /api/search/` — Semantic search over stored clauses
  - `POST /api/analysis/` — Upload and analyze a contract
  - `GET /api/contracts/` — List and retrieve stored contracts
- **Communicates with:** Database (SQLAlchemy), ML services (Python imports)

### ML Pipeline
- **Purpose:** NLP models for semantic search, clause classification, and risk flagging
- **Key components:**
  - **Sentence Transformer** — Encodes clauses into vector embeddings
  - **FAISS Index** — Fast nearest-neighbor search over clause vectors
  - **Clause Classifier** — Categorizes clauses by type (termination, liability, etc.)
  - **Risk Flagger** — Compares clauses against market norms, flags outliers
- **Communicates with:** Backend services (imported as Python modules)

### Database (PostgreSQL / SQLite)
- **Purpose:** Persistent storage for contracts, clauses, and analysis results
- **Tables (planned):** contracts, clauses, analysis_results, market_norms

### FAISS Vector Index
- **Purpose:** In-memory similarity search over clause embeddings
- **Updated:** When new contracts are ingested
- **Queried:** During semantic search requests

## Data Flow

### Search Flow
1. User types a natural language query in the React UI
2. Frontend sends `GET /api/search/?q=...&role=...` to backend
3. Backend encodes the query using the sentence transformer
4. FAISS returns the top-k most similar clauses
5. Results are filtered/ranked by role relevance
6. Backend returns structured results to frontend
7. Frontend displays results with risk badges and explanations

### Analysis Flow
1. User uploads a contract file (PDF, DOCX, or TXT)
2. Frontend sends `POST /api/analysis/` with file and role
3. Backend extracts text and splits into clauses
4. Each clause is classified by type and checked against market norms
5. Risk flags are generated with plain-English explanations
6. Full analysis is returned and displayed in the UI
