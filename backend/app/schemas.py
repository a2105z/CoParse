from __future__ import annotations

import uuid
from typing import Any

from pydantic import BaseModel, Field


class DocumentCreateResponse(BaseModel):
    document_id: uuid.UUID
    job_id: uuid.UUID


class JobStatusResponse(BaseModel):
    id: uuid.UUID
    document_id: uuid.UUID
    status: str
    progress_steps: list[dict[str, Any]] | None = None
    error_message: str | None = None


class ReanalyzeRequest(BaseModel):
    contract_type: str = Field(..., description="lease | internship_offer | freelance")
    role: str = Field(..., description="renter | student_intern | freelancer | ...")


class AnalysisResponse(BaseModel):
    document_id: uuid.UUID
    job_id: uuid.UUID | None = None
    contract_type: str
    role: str
    overall_score: int
    signature_readiness: dict[str, Any]
    category_scores: dict[str, int]
    top_issues: list[dict[str, Any]]
    clauses: list[dict[str, Any]]
    missing_protections: list[dict[str, Any]]
    questions_to_ask: list[dict[str, Any]]
    timeline: list[dict[str, Any]]
