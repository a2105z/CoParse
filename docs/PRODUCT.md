# CoParse — Product

## Positioning

CoParse is a **mobile-first contract safety layer** for **students, renters, and early-career workers** who sign common agreements without legal training.

**Phase 1 focus (MVP verticals):**

1. Internship / job offer (student intern lens)
2. Residential lease (renter lens)
3. Freelance / contractor agreement (freelancer lens)

CoParse is **not** enterprise procurement software, not a lawyer workflow tool, and not a substitute for professional legal advice.

## What the product does

- Extract text from uploaded PDFs (server-side)
- Guess contract type; user can confirm or override
- Highlight clauses that may deserve attention for the selected **role**
- Surface **missing protections** using template checks (to reduce hallucinated “missing” items)
- Provide plain-English explanations and **practical questions to ask** before signing
- Offer a **signature readiness style score** with transparent category breakdowns

## What the product does not do

- Tell a user to “sign” or “not sign”
- Provide legal advice or predict enforceability with certainty
- Replace attorneys, legal aid, or university housing/legal resources

## Disclaimers (must remain visible in the app)

- Outputs are **educational** and based on text extraction and automated analysis, which can be incomplete or wrong (especially if OCR/text extraction fails).
- Users should verify important details against the original document and seek qualified help when stakes are high.

## Privacy and data (MVP defaults)

- Define retention and deletion policies before production launch.
- Avoid logging full contract text in application logs.
- Keep third-party model API keys on the server only.

## Evaluation process (recommended)

- Maintain a small, rights-cleared **evaluation set** (synthetic templates + public-domain samples).
- Track regressions when changing segmentation, scoring, or prompts.
- Never train or evaluate on user-uploaded documents without explicit consent and a clear policy.
