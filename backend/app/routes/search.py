from fastapi import APIRouter, Query

router = APIRouter()


@router.get("/")
async def semantic_search(
    q: str = Query(..., description="Natural language search query"),
    role: str = Query("general", description="User role: intern, tenant, contractor, etc."),
):
    """
    Search contracts by meaning using semantic similarity.
    Returns ranked clauses relevant to the query and user role.
    """
    # TODO: integrate with ML semantic search service
    return {
        "query": q,
        "role": role,
        "results": [],
        "message": "Semantic search endpoint — ML integration pending",
    }
