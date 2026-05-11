"""Template-based missing protection detection (per vertical)."""

from __future__ import annotations

from app.pipeline.classify import ContractType

Checks = list[tuple[str, list[str]]]

LEASE_CHECKS: Checks = [
    ("subletting", ["sublet", "assignment", "transfer of lease"]),
    ("early termination", ["early termination", "break lease", "terminate prior"]),
    ("repairs", ["repair", "maintenance", "habitable"]),
    ("security deposit", ["security deposit", "deposit"]),
    ("renewal", ["renew", "automatic renewal"]),
]

INTERNSHIP_CHECKS: Checks = [
    ("compensation", ["compensation", "wage", "salary", "stipend", "pay"]),
    ("ip assignment", ["intellectual property", "invention", "work product"]),
    ("confidentiality", ["confidential", "non-disclosure"]),
    ("dispute resolution", ["arbitration", "mediation", "jurisdiction"]),
    ("duration", ["start date", "end date", "term"]),
]

FREELANCE_CHECKS: Checks = [
    ("payment timing", ["payment", "invoice", "net ", "due"]),
    ("scope / changes", ["scope", "change order", "additional work"]),
    ("ownership", ["work product", "deliverable", "intellectual property"]),
    ("termination", ["terminat", "cancel"]),
    ("late payment", ["late fee", "interest", "past due"]),
]


def find_missing_protections(
    contract_type: ContractType,
    full_text: str,
) -> list[dict]:
    low = full_text.lower()
    checks: Checks
    if contract_type == "lease":
        checks = LEASE_CHECKS
    elif contract_type == "internship_offer":
        checks = INTERNSHIP_CHECKS
    elif contract_type == "freelance":
        checks = FREELANCE_CHECKS
    else:
        checks = LEASE_CHECKS[:3]

    missing: list[dict] = []
    for label, keywords in checks:
        if not any(k in low for k in keywords):
            missing.append(
                {
                    "id": label.replace(" ", "_"),
                    "label": label,
                    "detail": f"No clear language about {label} was detected; worth asking for written clarity.",
                }
            )
    return missing[:8]
