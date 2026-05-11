"""Plain-English explanations and negotiation prompts (template + optional LLM)."""

from __future__ import annotations

import json

import httpx

from app.config import settings
from app.pipeline.classify import ContractType


def build_clause_card(
    clause_text: str,
    theme: str,
    risk_level: str,
    contract_type: ContractType,
    role: str,
) -> dict:
    meaning = _template_meaning(theme, clause_text)
    compare = _template_compare(risk_level, contract_type)
    question = _template_question(theme, role)
    return {
        "plain_english": meaning,
        "compare_note": compare,
        "suggested_question_neutral": question,
        "suggested_question_polite": f"Could you help me understand this section: {question}",
        "negotiability": "often_worth_asking" if risk_level == "high" else "sometimes_negotiable",
    }


def _template_meaning(theme: str, clause_text: str) -> str:
    snippet = clause_text[:320] + ("…" if len(clause_text) > 320 else "")
    guides = {
        "payment": "This language describes money flow—who pays whom, when, and what triggers payment.",
        "termination": "This describes how the agreement can end and what happens afterward.",
        "ip": "This may affect who owns work product or inventions related to the relationship.",
        "confidentiality": "This limits what you can share about the other party's information.",
        "disputes": "This describes how disagreements are resolved (courts vs arbitration, location, etc.).",
        "deposit": "This concerns money held up front and when it may be returned or withheld.",
        "maintenance": "This allocates repair and upkeep responsibilities.",
        "subletting": "This limits whether you can sublet, assign, or have guests long-term.",
        "scope": "This defines what work is included and how changes are handled.",
        "indemnity": "This can shift legal/financial responsibility between parties.",
        "general": "This section sets expectations or obligations between the parties.",
    }
    return f"{guides.get(theme, guides['general'])}\n\nExcerpt: {snippet}"


def _template_compare(risk_level: str, contract_type: ContractType) -> str:
    if risk_level == "high":
        return f"Compared to many {contract_type.replace('_', ' ')} templates, this language looks more restrictive or one-sided—worth clarifying."
    if risk_level == "medium":
        return "This is not uncommon, but the wording may leave room for disagreement—confirm details that matter to you."
    return "This looks broadly in line with typical agreements, but read the specifics against your situation."


def _template_question(theme: str, role: str) -> str:
    _ = role
    q = {
        "payment": "What is the exact payment schedule, and what happens if payment is late?",
        "termination": "How much notice is required to end this agreement, and are there penalties?",
        "ip": "Does this apply to projects I create on my own time without company resources?",
        "confidentiality": "How long do confidentiality obligations last after the relationship ends?",
        "disputes": "Can we use mediation before binding arbitration, and where would disputes be heard?",
        "deposit": "Under what conditions can the deposit be withheld, and when is it returned?",
        "maintenance": "Who is responsible for specific repairs, and what is the timeline for fixing issues?",
        "subletting": "What is the process and any fees for subletting or assigning the agreement?",
        "scope": "How are scope changes approved and billed?",
        "indemnity": "Can we cap liability or narrow indemnity to direct damages?",
        "general": "Can you walk me through how this section applies day-to-day?",
    }
    return q.get(theme, q["general"])


async def maybe_enrich_with_llm(clauses: list[dict]) -> list[dict]:
    """Optional OpenAI pass: tighten wording; no new facts without clause text."""
    if not settings.openai_api_key:
        return clauses

    api_key = settings.openai_api_key
    model = settings.openai_model
    payload_clauses = [{"id": i, "text": c["text"][:1200], "theme": c["theme"]} for i, c in enumerate(clauses[:12])]

    prompt = (
        "You rewrite legal clause explanations for non-lawyers. "
        "Use ONLY the provided clause text. Output JSON array of objects with keys: id, plain_english_one_sentence. "
        "No legal advice; educational tone.\n\n"
        + json.dumps(payload_clauses)
    )

    try:
        async with httpx.AsyncClient(timeout=60.0) as client:
            r = await client.post(
                "https://api.openai.com/v1/chat/completions",
                headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
                json={
                    "model": model,
                    "messages": [{"role": "user", "content": prompt}],
                    "temperature": 0.2,
                },
            )
            r.raise_for_status()
            content = r.json()["choices"][0]["message"]["content"]
            data = json.loads(content)
            if isinstance(data, list):
                for item in data:
                    idx = int(item.get("id", -1))
                    if 0 <= idx < len(clauses) and item.get("plain_english_one_sentence"):
                        clauses[idx]["plain_english_one_sentence"] = item["plain_english_one_sentence"]
    except Exception:
        pass

    return clauses
