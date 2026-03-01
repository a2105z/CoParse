from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routes import search, analysis, contracts

app = FastAPI(
    title="CoParse API",
    description="AI-powered legal search and contract analysis",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(search.router, prefix="/api/search", tags=["Search"])
app.include_router(analysis.router, prefix="/api/analysis", tags=["Analysis"])
app.include_router(contracts.router, prefix="/api/contracts", tags=["Contracts"])


@app.get("/")
async def root():
    return {"message": "CoParse API is running", "docs": "/docs"}


@app.get("/health")
async def health_check():
    return {"status": "healthy"}
