"""Heuristic clause / section segmentation (v0)."""

import re


def segment_clauses(text: str, max_clauses: int = 80) -> list[str]:
    text = text.strip()
    if not text:
        return []

    # Split on numbered sections like "1.", "Section 2", ALL CAPS lines
    rough = re.split(r"\n(?=\s*(?:Section\s+\d+|[\d]+\.|(?:[A-Z][A-Z\s]{8,}))\s*\n)", text, flags=re.IGNORECASE)
    chunks = [c.strip() for c in rough if len(c.strip()) > 40]

    if len(chunks) < 2:
        chunks = [p.strip() for p in re.split(r"\n{2,}", text) if len(p.strip()) > 60]

    if len(chunks) < 2:
        chunks = [text[i : i + 2000] for i in range(0, len(text), 1800)]

    return chunks[:max_clauses]
