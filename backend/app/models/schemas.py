from pydantic import BaseModel


class ClauseResult(BaseModel):
    text: str
    clause_type: str
    risk_level: str  # "low", "medium", "high"
    explanation: str
    similarity_score: float | None = None


class SearchResponse(BaseModel):
    query: str
    role: str
    results: list[ClauseResult]


class RiskFlag(BaseModel):
    clause_text: str
    risk_type: str  # "risky", "unusual", "missing"
    severity: str
    explanation: str
    market_norm: str | None = None


class AnalysisResponse(BaseModel):
    filename: str
    role: str
    total_clauses: int
    risks: list[RiskFlag]
    summary: str
