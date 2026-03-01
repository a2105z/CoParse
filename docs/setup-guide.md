# CoParse Setup Guide

Step-by-step instructions for getting the full development environment running on your machine.

---

## Prerequisites

Install these before starting:

| Tool | Version | Download |
|------|---------|----------|
| Python | 3.10+ | https://www.python.org/downloads/ |
| Node.js | 18+ | https://nodejs.org/ |
| Git | Latest | https://git-scm.com/downloads/ |
| VS Code or Cursor | Latest | https://code.visualstudio.com/ or https://cursor.com/ |

---

## Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/CoParse.git
cd CoParse
```

---

## Step 2: Backend Setup

```bash
cd backend

# Create a virtual environment
python -m venv venv

# Activate it
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the server
uvicorn app.main:app --reload
```

Verify it works: open http://localhost:8000/docs in your browser.

---

## Step 3: Frontend Setup

Open a **new terminal** (keep the backend running):

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

Verify it works: open http://localhost:3000 in your browser.

---

## Step 4: ML Environment Setup

Open a **new terminal**:

```bash
cd ml

# Create a virtual environment
python -m venv venv

# Activate it
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Download spaCy language model
python -m spacy download en_core_web_sm
```

---

## Step 5: Verify Everything Works

| Component | URL | Expected |
|-----------|-----|----------|
| Backend API | http://localhost:8000 | JSON message: "CoParse API is running" |
| API Docs | http://localhost:8000/docs | Interactive Swagger UI |
| Frontend | http://localhost:3000 | CoParse search interface |

---

## Common Issues

### "python not found"
Use `python3` instead of `python` on macOS/Linux.

### "npm not found"
Make sure Node.js is installed and restart your terminal.

### CORS errors in browser console
Make sure the backend is running on port 8000 before starting the frontend.

### Port already in use
Kill the existing process or use a different port:
```bash
# Backend on a different port
uvicorn app.main:app --reload --port 8001

# Frontend on a different port
set PORT=3001 && npm start   # Windows
PORT=3001 npm start          # macOS/Linux
```
