# CoParse

**Contract safety for students, renters, and early-career workers.**

CoParse is a native **iOS** app that scans paper contracts (or imports PDFs), highlights clauses that may deserve attention, explains them in plain English, and suggests questions to ask before you sign. Analysis runs **on-device** with Apple Vision OCR and a heuristic engine — free, offline-capable, no account.

> Educational information only — not legal advice. See [`docs/PRODUCT.md`](docs/PRODUCT.md).

## Product surface

| Capability | Detail |
| --- | --- |
| Capture | VisionKit document scan (recommended), camera, Photos, PDF import |
| OCR | Apple Vision + image preprocess; PDFKit text with OCR fallback |
| Analysis | Segment → classify → explain → missing protections → score → role packs |
| Report | Score, category bars, timeline, if/then nudges, questions, email draft |
| Storage | SwiftData saves on device; optional auto-save; share as text report |
| Privacy | Default path never uploads contract images/text |

**Verticals:** residential lease (renter), internship / offer (student intern), freelance agreement (freelancer), plus scan-any with confirm.

## Stack

| Layer | Tech |
| --- | --- |
| iOS | SwiftUI, Vision, VisionKit, PDFKit, SwiftData, AVFoundation |
| Analysis | On-device heuristic engine (aligned with `backend/app/pipeline/`) |
| Backend (optional) | FastAPI + PostgreSQL for demos / future sync — not required to scan |
| Infra (optional) | Docker Compose (local), Render + Neon (API deploy) |

## Quick start (iOS)

1. Open [`ios/CoParse.xcodeproj`](ios/CoParse.xcodeproj) in Xcode 15+.
2. Set your Development Team for signing (`com.coparse.app`).
3. Run on a simulator or device (`⌘R`).
4. On device, grant Camera / Photos when prompted.

**Flow:** Disclaimer → onboarding → Home → Scan → Analyze → Confirm (when needed) → Report.

## Optional backend

```bash
docker compose up -d
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp ../.env.example .env
alembic upgrade head
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

- API: http://localhost:8000  
- OpenAPI: http://localhost:8000/docs  
- Deploy: [`docs/DEPLOY_RENDER_NEON.md`](docs/DEPLOY_RENDER_NEON.md)

The shipping iOS scan path does **not** require the API.

## Docs

- [`docs/PRODUCT.md`](docs/PRODUCT.md) — positioning, privacy, free model  
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — on-device + optional API  
- [`docs/API.md`](docs/API.md) — optional backend endpoints  
- [`ios/README.md`](ios/README.md) — Xcode notes  

## Cost model

- **Users:** free, no account, no ads in this release.  
- **Operator runtime:** $0 for the on-device path (no LLM/OCR bill).  
- **Distribution:** Apple Developer Program (~$99/year) for TestFlight / App Store.

## License

TBD.
