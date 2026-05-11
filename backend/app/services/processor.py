from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy.orm import Session

from app.models import AnalysisRun, Document, Job
from app.pipeline.analyze import analyze_text
from app.pipeline.extract import extract_text_from_file
from app.config import settings


async def process_job(
    db: Session,
    job_id: uuid.UUID,
    contract_type_override: str | None = None,
    role: str = "general",
) -> None:
    job = db.query(Job).filter(Job.id == job_id).first()
    if not job:
        return

    doc = db.query(Document).filter(Document.id == job.document_id).first()
    if not doc:
        job.status = "failed"
        job.error_message = "Document not found"
        db.commit()
        return

    try:
        job.status = "processing"
        job.progress_steps = [
            {"step": "extract", "done": False},
            {"step": "segment", "done": False},
            {"step": "score", "done": False},
        ]
        db.commit()

        path = settings.storage_path / doc.storage_path
        if not doc.extracted_text:
            text = extract_text_from_file(path, doc.content_type)
            doc.extracted_text = text
            db.commit()
        else:
            text = doc.extracted_text

        job.progress_steps = [
            {"step": "extract", "done": True},
            {"step": "segment", "done": True},
            {"step": "score", "done": False},
        ]
        db.commit()

        result = await analyze_text(
            text,
            doc.hint_contract_type,
            contract_type_override,
            role,
        )

        overall = int(result["overall_score"])
        cat = {k: int(v) for k, v in result["category_scores"].items()}
        rec = result["signature_readiness"]["recommendation_key"]

        run = AnalysisRun(
            id=uuid.uuid4(),
            document_id=doc.id,
            job_id=job.id,
            contract_type=result["contract_type"],
            role=result["role"],
            overall_score=overall,
            category_scores=cat,
            recommendation=rec,
            result=result,
        )
        db.add(run)

        job.status = "completed"
        job.progress_steps = [
            {"step": "extract", "done": True},
            {"step": "segment", "done": True},
            {"step": "score", "done": True},
        ]
        job.error_message = None
        job.updated_at = datetime.now().astimezone()
        db.commit()
    except Exception as e:  # noqa: BLE001
        job.status = "failed"
        job.error_message = str(e)
        job.updated_at = datetime.now().astimezone()
        db.commit()
