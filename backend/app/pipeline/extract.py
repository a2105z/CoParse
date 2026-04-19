"""Extract plain text from PDF or pass-through for .txt."""

from pathlib import Path

from pypdf import PdfReader


def extract_text_from_file(path: Path, content_type: str) -> str:
    suffix = path.suffix.lower()
    if suffix == ".txt" or content_type.startswith("text/plain"):
        return path.read_text(encoding="utf-8", errors="replace")

    if suffix == ".pdf" or "pdf" in content_type:
        reader = PdfReader(str(path))
        parts: list[str] = []
        for page in reader.pages:
            t = page.extract_text() or ""
            parts.append(t)
        return "\n\n".join(parts).strip()

    raise ValueError(f"Unsupported file type: {suffix or content_type}")
