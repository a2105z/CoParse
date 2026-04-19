"""Orchestrate extraction -> segmentation -> classification -> scoring -> result JSON."""

from __future__ import annotations

import re
from typing import Any

from app.pipeline.classify import classify_theme, detect_contract_type, risk_level_for_clause
from app.pipeline.explain import build_clause_card, maybe_enrich_with_llm
from app.pipeline.missing import find_missing_protections
from app.pipeline.score import compute_scores
from app.pipeline.segment import segment_clauses
from app.pipeline.student import build_next_steps, build_student_journey


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
    analysis_confidence = _analysis_confidence(text, clauses_raw)
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
                "flag_reason": _flag_reason(theme, risk, ctype),
                "confidence_note": _clause_confidence_note(chunk),
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
        q = c.get("suggested_question_neutral", "")
        if q:
            questions.append(
                {
                    "clause_id": c["id"],
                    "question": q,
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
        "analysis_confidence": analysis_confidence,
        "limitations": [
            "Educational support only - not legal advice.",
            "OCR/text extraction can miss or distort clauses, especially scanned PDFs.",
            "Always verify key terms against the original document before signing.",
        ],
        "category_scores": category_scores,
        "top_issues": [
            {
                "id": c["id"],
                "theme": c["theme"],
                "risk_level": c["risk_level"],
                "flag_reason": c.get("flag_reason"),
                "plain_english": c["plain_english"][:400],
            }
            for c in top_issues
        ],
        "clauses": clauses,
        "missing_protections": missing,
        "questions_to_ask": questions,
        "timeline": timeline,
        "student_journey": build_student_journey(ctype, role),
        "next_steps": build_next_steps(ctype, top_issues, missing),
    }


def _rec_text(key: str) -> str:
    return {
        "mostly_standard": "Looks mostly standard but still review flagged items before signing.",
        "worth_clarifying": "Worth clarifying several items before signing.",
        "caution": "Several terms may be unusually restrictive or unclear; consider professional review.",
        "strongly_consider_review": "Strongly consider reviewing with legal aid or counsel before signing.",
    }.get(key, "Review details carefully before signing.")


def _analysis_confidence(text: str, clauses: list[str]) -> dict[str, Any]:
    reasons: list[str] = []
    low = text.lower()
    alpha_ratio = sum(ch.isalpha() for ch in text) / max(1, len(text))
    noisy_markers = sum(low.count(x) for x in ["�", "\\x00", "  ", "...."])

    score = 0
    if len(text) < 1200:
        score -= 1
        reasons.append("The extracted text is short, so some sections may be missing.")
    if len(clauses) < 4:
        score -= 1
        reasons.append("Few clause segments were detected; section boundaries may be unclear.")
    if alpha_ratio < 0.55 or noisy_markers > 6:
        score -= 1
        reasons.append("Text quality appears noisy, likely from scan/OCR issues.")
    if any(k in low for k in ["signature", "agreement", "term", "payment", "liability"]):
        score += 1

    level = "high" if score >= 1 else "medium" if score == 0 else "low"
    if not reasons:
        reasons.append("Clause boundaries and text quality look usable for a first-pass review.")

    return {
        "level": level,
        "reasons": reasons,
        "summary": {
            "high": "High confidence in extraction and segmentation quality.",
            "medium": "Moderate confidence; review flagged items with original text side by side.",
            "low": "Lower confidence due to extraction/segmentation quality. Treat results as directional.",
        }[level],
    }


def _flag_reason(theme: str, risk_level: str, contract_type: str) -> str:
    theme_reason = {
        "payment": "money timing or obligations may be unclear or one-sided",
        "termination": "exit terms or penalties can materially affect your options",
        "ip": "ownership language may transfer rights beyond what you expect",
        "confidentiality": "scope or duration may be broader than typical",
        "disputes": "dispute process/location can reduce your practical leverage",
        "renewal": "auto-renewal can lock you in if notice is missed",
        "deposit": "deposit return/withholding terms may be ambiguous",
        "maintenance": "repair duties may be shifted in a way that increases your burden",
        "subletting": "restrictions can limit flexibility during the term",
        "scope": "scope ambiguity often leads to unpaid or disputed extra work",
        "indemnity": "liability-shifting can create outsized financial risk",
        "general": "wording appears broad enough to deserve clarification",
    }[theme]
    severity = {
        "high": "Flagged high risk because",
        "medium": "Flagged medium risk because",
        "low": "Flagged for awareness because",
    }[risk_level]
    return f"{severity} {theme_reason} for this {contract_type.replace('_', ' ')} context."


def _clause_confidence_note(clause_text: str) -> str:
    if len(clause_text) < 80:
        return "Short section detected; surrounding context may change interpretation."
    if re.search(r"[\[\]{}]{2,}|_{3,}|\.{5,}", clause_text):
        return "Formatting appears noisy in this section, which may reduce analysis accuracy."
    return "Text quality in this section looks adequate for educational review."


def _timeline(contract_type: str, clauses: list[dict[str, Any]]) -> list[dict[str, Any]]:
    stage_map = {
        "before_signing": ["payment", "deposit", "scope", "ip"],
        "during_term": ["confidentiality", "maintenance", "subletting"],
        "ending_or_dispute": ["termination", "disputes", "renewal"],
    }
    stage_title = {
        "before_signing": "Before signing",
        "during_term": "During the term",
        "ending_or_dispute": "If things change or end",
    }

    items: list[dict[str, Any]] = []
    for stage, keys in stage_map.items():
        selected = [c for c in clauses if c["theme"] in keys][:3]
        if not selected:
            continue
        items.append(
            {
                "phase": stage,
                "title": stage_title[stage],
                "when": _phase_when_text(stage, contract_type),
                "watch_for": ", ".join(dict.fromkeys(c["theme"].replace("_", " ") for c in selected)),
                "clause_ids": [c["id"] for c in selected],
            }
        )
    return items


def _phase_when_text(phase: str, contract_type: str) -> str:
    if phase == "before_signing":
        return "Verify these terms before committing."
    if phase == "during_term":
        return "Track these terms while the agreement is active."
    if contract_type == "lease":
        return "Critical around move-out, renewal notices, or disputes."
    return "Critical when ending, renewing, or handling disputes."
