from __future__ import annotations

import asyncio
import uuid
from datetime import datetime
from pathlib import Path

from fastapi import APIRouter, BackgroundTasks, Depends, File, Form, HTTPException, UploadFile
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session

from app.config import settings
from app.database import get_db
from app.models import AnalysisRun, Document, Job
from app.schemas import AnalysisResponse, DocumentCreateResponse, ReanalyzeRequest
from app.services.processor import process_job

router = APIRouter()


def _ensure_storage() -> None:
    settings.storage_path.mkdir(parents=True, exist_ok=True)


@router.post("", response_model=DocumentCreateResponse)
async def create_document(
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db),
    file: UploadFile = File(...),
    hint_contract_type: str | None = Form(None),
    hint_role: str | None = Form(None),
):
    _ensure_storage()
    doc_id = uuid.uuid4()
    job_id = uuid.uuid4()

    suffix = ""
    if file.filename and "." in file.filename:
        suffix = file.filename.rsplit(".", 1)[-1].lower()
    if suffix not in ("pdf", "txt"):
        suffix = "pdf"

    rel = f"{doc_id}.{suffix}"
    path = settings.storage_path / rel
    content = await file.read()
    path.write_bytes(content)

    content_type = file.content_type or "application/octet-stream"
    doc = Document(
        id=doc_id,
        filename=file.filename or "upload",
        content_type=content_type,
        storage_path=rel,
        hint_contract_type=hint_contract_type or "auto",
        created_at=datetime.now().astimezone(),
    )
    job = Job(
        id=job_id,
        document_id=doc_id,
        status="pending",
        created_at=datetime.now().astimezone(),
        updated_at=datetime.now().astimezone(),
    )
    db.add(doc)
    db.add(job)
    db.commit()

    role = hint_role or "general"

    async def _run() -> None:
        from app.database import SessionLocal

        session = SessionLocal()
        try:
            await process_job(session, job_id, contract_type_override=None, role=role)
        finally:
            session.close()

    background_tasks.add_task(lambda: asyncio.run(_run()))
    return DocumentCreateResponse(document_id=doc_id, job_id=job_id)


@router.get("/{document_id}/analysis", response_model=AnalysisResponse)
def get_analysis(document_id: uuid.UUID, db: Session = Depends(get_db)):
    runs = (
        db.query(AnalysisRun)
        .filter(AnalysisRun.document_id == document_id)
        .order_by(AnalysisRun.created_at.desc())
        .limit(2)
        .all()
    )
    if not runs:
        raise HTTPException(status_code=404, detail="Analysis not ready or not found")

    run = runs[0]
    previous = runs[1] if len(runs) > 1 else None
    r = run.result
    return AnalysisResponse(
        document_id=document_id,
        job_id=run.job_id,
        contract_type=r.get("contract_type", run.contract_type),
        role=r.get("role", run.role),
        overall_score=r.get("overall_score", run.overall_score),
        signature_readiness=r.get("signature_readiness", {}),
        analysis_confidence=r.get("analysis_confidence"),
        limitations=r.get("limitations", []),
        category_scores=r.get("category_scores", run.category_scores),
        top_issues=r.get("top_issues", []),
        clauses=r.get("clauses", []),
        missing_protections=r.get("missing_protections", []),
        questions_to_ask=r.get("questions_to_ask", []),
        timeline=r.get("timeline", []),
        student_journey=r.get("student_journey"),
        next_steps=r.get("next_steps"),
        changes_since_last_run=_changes_since_last_run(r, previous.result if previous else None),
    )


@router.get("/{document_id}/file")
def download_document_file(document_id: uuid.UUID, db: Session = Depends(get_db)):
    doc = db.query(Document).filter(Document.id == document_id).first()
    if not doc:
        raise HTTPException(status_code=404, detail="Document not found")

    file_path = (settings.storage_path / doc.storage_path).resolve()
    storage_root = settings.storage_path.resolve()
    if storage_root not in file_path.parents and file_path != storage_root:
        raise HTTPException(status_code=400, detail="Invalid storage path")
    if not file_path.exists():
        raise HTTPException(status_code=404, detail="File not found")

    return FileResponse(
        path=Path(file_path),
        media_type=doc.content_type or "application/octet-stream",
        filename=doc.filename or file_path.name,
    )


def _changes_since_last_run(current: dict, previous: dict | None) -> dict | None:
    if not previous:
        return None

    score_delta = int(current.get("overall_score", 0)) - int(previous.get("overall_score", 0))

    current_cat = current.get("category_scores", {}) or {}
    prev_cat = previous.get("category_scores", {}) or {}
    category_deltas = {
        k: int(current_cat.get(k, 0)) - int(prev_cat.get(k, 0))
        for k in sorted(set(current_cat.keys()) | set(prev_cat.keys()))
    }

    current_themes = {
        i.get("theme", "general")
        for i in (current.get("top_issues", []) or [])
        if isinstance(i, dict)
    }
    prev_themes = {
        i.get("theme", "general")
        for i in (previous.get("top_issues", []) or [])
        if isinstance(i, dict)
    }
    newly_flagged = sorted(current_themes - prev_themes)
    resolved = sorted(prev_themes - current_themes)

    summary_parts: list[str] = []
    if score_delta > 0:
        summary_parts.append(f"Readiness improved by {score_delta} points.")
    elif score_delta < 0:
        summary_parts.append(f"Readiness dropped by {abs(score_delta)} points.")
    else:
        summary_parts.append("Readiness score is unchanged.")

    if newly_flagged:
        summary_parts.append(f"New flagged themes: {', '.join(newly_flagged)}.")
    if resolved:
        summary_parts.append(f"Resolved themes: {', '.join(resolved)}.")

    return {
        "score_delta": score_delta,
        "category_deltas": category_deltas,
        "newly_flagged_themes": newly_flagged,
        "resolved_themes": resolved,
        "summary": " ".join(summary_parts),
    }


@router.post("/{document_id}/reanalyze", response_model=DocumentCreateResponse)
async def reanalyze_document(
    document_id: uuid.UUID,
    body: ReanalyzeRequest,
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db),
):
    doc = db.query(Document).filter(Document.id == document_id).first()
    if not doc:
        raise HTTPException(status_code=404, detail="Document not found")

    job_id = uuid.uuid4()
    job = Job(
        id=job_id,
        document_id=document_id,
        status="pending",
        created_at=datetime.now().astimezone(),
        updated_at=datetime.now().astimezone(),
    )
    db.add(job)
    db.commit()

    async def _run() -> None:
        from app.database import SessionLocal

        session = SessionLocal()
        try:
            await process_job(
                session,
                job_id,
                contract_type_override=body.contract_type,
                role=body.role,
            )
        finally:
            session.close()

    background_tasks.add_task(lambda: asyncio.run(_run()))
    return DocumentCreateResponse(document_id=document_id, job_id=job_id)
