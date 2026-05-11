"""Contract type and clause theme classification (keyword heuristics v0)."""

from __future__ import annotations

import re
from typing import Literal

ContractType = Literal["lease", "internship_offer", "freelance", "unknown"]
Theme = Literal[
    "payment",
    "termination",
    "ip",
    "confidentiality",
    "disputes",
    "renewal",
    "deposit",
    "maintenance",
    "subletting",
    "scope",
    "indemnity",
    "general",
]


TYPE_KEYWORDS: dict[ContractType, list[str]] = {
    "lease": ["landlord", "tenant", "rent", "security deposit", "premises", "lease term"],
    "internship_offer": ["intern", "internship", "at-will", "employment", "offer letter", "wage", "salary"],
    "freelance": ["independent contractor", "sow", "statement of work", "milestone", "invoice", "services"],
    "unknown": [],
}


THEME_PATTERNS: list[tuple[Theme, re.Pattern[str]]] = [
    ("payment", re.compile(r"payment|compensation|invoice|fee|rent|deposit|salary|wage", re.I)),
    ("termination", re.compile(r"terminat|cancel|breach|notice period|at-will", re.I)),
    ("ip", re.compile(r"intellectual property|invention|work product|assignment|derivative", re.I)),
    ("confidentiality", re.compile(r"confidential|non-disclosure|proprietary|trade secret", re.I)),
    ("disputes", re.compile(r"arbitrat|mediation|jurisdiction|governing law|class action", re.I)),
    ("renewal", re.compile(r"renew|automatically renew|month-to-month|extension", re.I)),
    ("deposit", re.compile(r"security deposit|damage deposit|withhold", re.I)),
    ("maintenance", re.compile(r"repair|maintain|habitable|landlord|tenant obligation", re.I)),
    ("subletting", re.compile(r"sublet|assign|transfer|guest", re.I)),
    ("scope", re.compile(r"scope of services|deliverable|change order|milestone", re.I)),
    ("indemnity", re.compile(r"indemnif|hold harmless|liability cap", re.I)),
]


def detect_contract_type(text: str, hint: str | None) -> ContractType:
    if hint and hint in ("lease", "internship_offer", "freelance"):
        return hint  # type: ignore[return-value]
    if hint == "auto" or not hint:
        scores: dict[ContractType, int] = {"lease": 0, "internship_offer": 0, "freelance": 0, "unknown": 0}
        low = text.lower()
        for ctype, kws in TYPE_KEYWORDS.items():
            if ctype == "unknown":
                continue
            scores[ctype] = sum(1 for k in kws if k in low)
        best = max(scores, key=lambda k: scores[k])
        if scores[best] >= 2:
            return best
        return "unknown"
    return "unknown"


def classify_theme(clause_text: str) -> Theme:
    for theme, pat in THEME_PATTERNS:
        if pat.search(clause_text):
            return theme
    return "general"


def risk_level_for_clause(theme: Theme, clause_text: str, contract_type: ContractType) -> str:
    low = clause_text.lower()
    aggressive = any(
        x in low
        for x in [
            "sole discretion",
            "unlimited",
            "irrevocable",
            "perpetual",
            "automatic renewal",
            "binding arbitration",
            "exclusive",
            "assigns all",
        ]
    )
    vague = any(x in low for x in ["reasonable", "as determined", "may", "sole"]) and len(clause_text) > 200

    if aggressive:
        return "high"
    if vague or theme in ("ip", "indemnity", "disputes"):
        return "medium"
    return "low"
