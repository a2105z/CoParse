"""Shared utility functions."""


def extract_text_from_file(content: bytes, filename: str) -> str:
    """Extract plain text from uploaded file (PDF, DOCX, or TXT)."""
    if filename.endswith(".txt"):
        return content.decode("utf-8")

    # TODO: add PDF extraction (e.g., PyPDF2 or pdfplumber)
    # TODO: add DOCX extraction (e.g., python-docx)

    raise ValueError(f"Unsupported file type: {filename}")


def split_into_clauses(text: str) -> list[str]:
    """Split contract text into individual clauses/sections."""
    # TODO: implement smarter clause splitting (by section headers, numbered items, etc.)
    paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
    return paragraphs
