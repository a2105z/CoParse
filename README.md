# CoParse

**AI-powered legal search and contract analysis for everyone.**

CoParse helps non-lawyers understand real risks in common agreements — internships, NDAs, housing leases, and more. Search contracts by meaning (not keywords), get highlights of risky or unusual clauses, and see how language compares to market norms — all tailored to your role.

> CoParse does not replace a lawyer. It helps you ask the right questions before you sign.

---

## Features

- **Semantic Contract Search** — find clauses by meaning, not exact keywords
- **Clause Classification & Risk Flagging** — automatically detect risky, unusual, or missing clauses
- **Role-Aware Insights** — get tailored analysis for interns, tenants, contractors, and more
- **Market-Norm Comparisons** — see how clauses stack up against common industry language
- **Plain-English Explanations** — clear summaries designed for non-technical users

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React, HTML/CSS, JavaScript |
| **Backend API** | Python, FastAPI |
| **ML / NLP** | Python, sentence-transformers, scikit-learn, spaCy |
| **Database** | PostgreSQL (or SQLite for local dev) |
| **Search** | FAISS (vector similarity search) |
| **Version Control** | Git & GitHub |

---

## Project Structure

```
CoParse/
├── backend/          # FastAPI server, routes, services, database models
├── frontend/         # React single-page application
├── ml/               # ML pipelines, training scripts, notebooks, model artifacts
├── docs/             # Architecture docs, API reference, setup guides
├── .github/          # PR & issue templates for team collaboration
├── CONTRIBUTING.md   # How to contribute (read this first!)
└── README.md         # You are here
```

---

## Getting Started

### Prerequisites

- **Python 3.10+** — [Download](https://www.python.org/downloads/)
- **Node.js 18+** — [Download](https://nodejs.org/)
- **Git** — [Download](https://git-scm.com/downloads)

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/CoParse.git
cd CoParse
```

### 2. Set Up the Backend

```bash
cd backend
python -m venv venv

# Windows
venv\Scripts\activate

# macOS/Linux
source venv/bin/activate

pip install -r requirements.txt
uvicorn app.main:app --reload
```

The API will be running at `http://localhost:8000`. Interactive docs at `http://localhost:8000/docs`.

### 3. Set Up the Frontend

```bash
cd frontend
npm install
npm start
```

The app will be running at `http://localhost:3000`.

### 4. Set Up the ML Environment

```bash
cd ml
python -m venv venv

# Windows
venv\Scripts\activate

# macOS/Linux
source venv/bin/activate

pip install -r requirements.txt
```

See [`ml/README.md`](ml/README.md) for details on training and evaluation.

---

## Team Workflow

We use a **feature-branch workflow**:

1. Pull the latest `main`: `git pull origin main`
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes and commit often with clear messages
4. Push your branch: `git push -u origin feature/your-feature-name`
5. Open a Pull Request on GitHub and request review from a teammate
6. After approval, merge into `main`

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the full guide.

---

## Team

| Role | Area |
|------|------|
| **ML Engineer(s)** | Semantic search, clause classification, NLP pipelines |
| **Backend Developer(s)** | FastAPI routes, database, API design |
| **Frontend Developer(s)** | React UI, user experience, styling |
| **Full-Stack / Integration** | Connecting ML outputs to the API and frontend |

---

## License

This project is for educational and collaborative purposes. License TBD.
