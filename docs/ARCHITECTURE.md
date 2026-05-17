# Architecture

Concise overview. For the full technical design (modules, data models, OCR, scoring, API sequences), see **[`TECHNICAL.md`](../TECHNICAL.md)**.

CoParse has two parts in the monorepo:

1. **iOS (`ios/`)** — SwiftUI on-the-spot scanner. Primary product surface.
2. **Backend (`backend/app/`)** — optional FastAPI API + SQLite/Postgres persistence for demos and future sync.

## On-device request flow

1. User accepts disclaimer and completes onboarding.
2. User picks a vertical (lease / internship / freelance) or “any.”
3. User captures pages via **Document Scan**, camera, Photos, and/or imports a PDF.
4. Pages can be reordered or deleted before analysis.
5. **Vision OCR** (images, preprocessed) or **PDFKit** (digital PDF text; OCR fallback if sparse) runs on device.
6. Swift `AnalysisEngine`: segment → classify → explain → missing → score → student packs.
7. Confirm type/role when needed; dashboard / clause / questions UI reads `AnalysisResult`.
8. Optional **SwiftData** auto-save stores full JSON for offline reopen; share exports a text report.

```text
Document Scan / Camera / Photos / PDF
              │
              ▼
     Preprocess · Vision OCR · PDFKit
              │
              ▼
     AnalysisEngine (Swift heuristics)
              │
              ▼
   Report · Questions · Saved (SwiftData)
```

## Optional backend flow

1. Client uploads a file to `POST /v1/documents` (not used by the free on-device path).
2. Backend stores the file, enqueues a job, runs `pipeline/analyze.py`.
3. Client polls `GET /v1/jobs/{id}` and fetches `GET /v1/documents/{id}/analysis`.

## Key boundaries

- iOS owns capture, OCR, on-device analysis, offline storage, and share.
- Backend owns optional networked analysis and deploy demos.
- Heuristic engines should stay behaviorally aligned (Swift port mirrors `backend/app/pipeline/`).

## Main files

- `ios/CoParse/` — SwiftUI app, engine, OCR/PDF/document scanner services
- `ios/CoParse.xcodeproj` — Xcode project
- `ios/CoParse/PrivacyInfo.xcprivacy` — App Store privacy manifest
- `TECHNICAL.md` — full technical specification
- `backend/app/main.py` — API setup
- `backend/app/pipeline/` — Python reference pipeline
- `docs/` — product and API notes
