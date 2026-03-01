"""
Semantic search service.

Handles encoding queries, searching the FAISS vector index,
and returning ranked clause results.
"""


class SearchService:
    def __init__(self):
        self.index = None
        self.model = None

    def load_model(self, model_path: str):
        """Load the sentence-transformer model and FAISS index."""
        # TODO: load sentence-transformers model
        # TODO: load FAISS index from disk
        pass

    def search(self, query: str, top_k: int = 10) -> list[dict]:
        """Encode query and return top-k similar clauses."""
        # TODO: encode query -> search index -> return results
        return []
