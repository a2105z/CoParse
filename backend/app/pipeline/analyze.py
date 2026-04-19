"""Orchestrate extraction → segmentation → classification → scoring → result JSON."""

from __future__ import annotations

from typing import Any

from app.pipeline.classify import classify_theme, detect_contract_type, risk_level_for_clause
from app.pipeline.explain import build_clause_card, maybe_enrich_with_llm
from app.pipeline.missing import find_missing_protections
from app.pipeline.score import compute_scores
from app.pipeline.segment import segment_clauses


async def analyze_text(
    text: str,
    hint_contract_type: str | None,
    contract_type_override: str | None,
    role: str,
) -> dict[str, Any]:
    ctype = contract_type_override or detect_contract_type(text, hint_contract_type or "auto")
    if ctype == "unknown":
        ctype = "lease"

    clauses_raw = segment_clauses(text)
    clauses: list[dict[str, Any]] = []

    for i, chunk in enumerate(clauses_raw):
        theme = classify_theme(chunk)
        risk = risk_level_for_clause(theme, chunk, ctype)
        card = build_clause_card(chunk, theme, risk, ctype, role)
        clauses.append(
            {
                "id": f"c{i}",
                "text": chunk,
                "theme": theme,
                "risk_level": risk,
                "tag": "high_risk" if risk == "high" else "unusual" if risk == "medium" else "standard",
                **card,
            }
        )

    clauses = sorted(clauses, key=lambda c: {"high": 0, "medium": 1, "low": 2}[c["risk_level"]])
    clauses = await maybe_enrich_with_llm(clauses)

    missing = find_missing_protections(ctype, text)
    overall, category_scores, recommendation = compute_scores(ctype, clauses, missing)

    top_issues = [c for c in clauses if c["risk_level"] in ("high", "medium")][:5]
    questions = []
    for c in top_issues[:6]:
        questions.append(
            {
                "clause_id": c["id"],
                "question": c.get("suggested_question_neutral", ""),
                "context": c["theme"],
            }
        )

    timeline = _timeline(ctype, clauses)

    return {
        "contract_type": ctype,
        "role": role,
        "overall_score": overall,
        "signature_readiness": {
            "score": overall,
            "recommendation_key": recommendation,
            "recommendation_text": _rec_text(recommendation),
        },
        "category_scores": category_scores,
        "top_issues": [
            {"id": c["id"], "theme": c["theme"], "risk_level": c["risk_level"], "plain_english": c["plain_english"][:400]}
            for c in top_issues
        ],
        "clauses": clauses,
        "missing_protections": missing,
        "questions_to_ask": questions,
        "timeline": timeline,
    }


def _rec_text(key: str) -> str:
    return {
        "mostly_standard": "Looks mostly standard—still review flagged items before signing.",
        "worth_clarifying": "Worth clarifying several items before signing.",
        "caution": "Several terms may be unusually restrictive or unclear—consider professional review.",
        "strongly_consider_review": "Strongly consider reviewing with legal aid or counsel before signing.",
    }.get(key, "Review details carefully before signing.")


def _timeline(ctype: str, clauses: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Group themes into before / during / after for UX."""
    before = ["payment", "deposit", "scope", "ip"]
    during = ["confidentiality", "maintenance", "subletting"]
    after = ["termination", "disputes", "renewal"]

    def pick(phase_keys: list[str]) -> list[str]:
        return [c["id"] for c in clauses if c["theme"] in phase_keys][:4]

    return [
        {"phase": "before_signing", "clause_ids": pick(before)},
        {"phase": "during_term", "clause_ids": pick(during)},
        {"phase": "after", "clause_ids": pick(after)},
    ]
