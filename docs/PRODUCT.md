# CoParse — Product

## Positioning

CoParse is a **mobile-first, on-the-spot contract safety layer** for **students, renters, and early-career workers** who sign common agreements without legal training — including when they only have a **paper copy**.

**Supported verticals:**

1. Internship / job offer (student intern lens)
2. Residential lease (renter lens)
3. Freelance / contractor agreement (freelancer lens)
4. Scan any contract (type/role confirm when confidence is low)

CoParse is **not** enterprise procurement software, not a lawyer workflow tool, and not a substitute for professional legal advice.

## What the product does

- **Document Scan** (VisionKit), multi-page camera / Photos capture, or PDF import
- **On-device OCR** (Apple Vision + preprocess) and PDFKit text extraction — no upload required
- Guess contract type; prompt confirm when confidence is low or type was unspecified
- Highlight clauses that may deserve attention for the selected **role**
- Surface **missing protections** via template checks
- Plain-English explanations, **questions to ask**, email drafts, and if/then nudges
- **Signature readiness score** with category breakdowns and timeline
- **Save / auto-save** analyses on-device; **share** a text report
- Settings, privacy screen, and first-run onboarding

## What the product does not do

- Tell a user to “sign” or “not sign”
- Provide legal advice or predict enforceability with certainty
- Replace attorneys, legal aid, or university housing/legal resources
- Require an account or paid cloud API for the default scan path

## Disclaimers (must remain visible in the app)

- Outputs are **educational** and based on text extraction and automated analysis, which can be incomplete or wrong (especially if OCR fails).
- Users should verify important details against the original document and seek qualified help when stakes are high.

## Privacy and data

- Default analysis runs **entirely on-device**; contract images/text are not uploaded for the free path.
- Optional FastAPI backend remains in-repo for demos / future sync — not required for scanning.
- Avoid logging full contract text in application logs.
- Keep third-party model API keys on the server only (if optional cloud enrich is enabled later).
- App Store privacy nutrition: no tracking; UserDefaults for local preferences only (`PrivacyInfo.xcprivacy`).

## Free forever design

- Users: free app, no account, offline-capable scans.
- Operator: no per-scan LLM/OCR bill; App Store distribution requires Apple Developer Program (~$99/year).

## Evaluation process (recommended)

- Maintain a small, rights-cleared **evaluation set** (synthetic templates + public-domain samples).
- Track regressions when changing segmentation, scoring, or explanations.
- Never train or evaluate on user-uploaded documents without explicit consent and a clear policy.
