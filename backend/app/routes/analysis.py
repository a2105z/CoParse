from fastapi import APIRouter, UploadFile, File, Form

router = APIRouter()


@router.post("/")
async def analyze_contract(
    file: UploadFile = File(..., description="Contract document to analyze"),
    role: str = Form("general", description="User role for tailored insights"),
):
    """
    Analyze an uploaded contract for risky, unusual, or missing clauses.
    Returns role-aware insights and market-norm comparisons.
    """
    filename = file.filename
    content = await file.read()

    # TODO: integrate with ML clause classifier and risk flagger
    return {
        "filename": filename,
        "role": role,
        "size_bytes": len(content),
        "clauses": [],
        "risks": [],
        "message": "Analysis endpoint — ML integration pending",
    }
