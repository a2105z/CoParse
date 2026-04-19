"""Signature readiness score and category breakdown."""

from __future__ import annotations

from typing import Any

from app.pipeline.classify import ContractType


def compute_scores(
    contract_type: ContractType,
    clauses: list[dict[str, Any]],
    missing: list[dict],
) -> tuple[int, dict[str, int], str]:
    # Base 85, subtract for high/medium risks and missing items
    base = 85
    cat = {"money": 80, "termination": 80, "ip_privacy": 80, "disputes": 80, "flexibility": 80}

    for c in clauses:
        lvl = c.get("risk_level", "low")
        theme = c.get("theme", "general")
        penalty = 6 if lvl == "high" else 3 if lvl == "medium" else 0
        base -= penalty

        if theme == "payment" or theme == "deposit":
            cat["money"] -= penalty
        if theme == "termination" or theme == "renewal":
            cat["termination"] -= penalty
            cat["flexibility"] -= penalty // 2
        if theme in ("ip", "confidentiality"):
            cat["ip_privacy"] -= penalty
        if theme == "disputes":
            cat["disputes"] -= penalty

    for _ in missing:
        base -= 4
        cat["flexibility"] -= 3

    for k in cat:
        cat[k] = max(35, min(100, cat[k]))

    overall = max(25, min(100, base))

    if overall >= 75:
        rec = "mostly_standard"
    elif overall >= 55:
        rec = "worth_clarifying"
    elif overall >= 40:
        rec = "caution"
    else:
        rec = "strongly_consider_review"

    return overall, cat, rec
