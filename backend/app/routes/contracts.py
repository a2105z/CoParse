from fastapi import APIRouter

router = APIRouter()


@router.get("/")
async def list_contracts():
    """List all stored contracts."""
    # TODO: query database for contracts
    return {"contracts": [], "message": "Contracts endpoint — database integration pending"}


@router.get("/{contract_id}")
async def get_contract(contract_id: int):
    """Get a specific contract by ID."""
    # TODO: fetch contract from database
    return {"contract_id": contract_id, "message": "Contract detail — database integration pending"}
