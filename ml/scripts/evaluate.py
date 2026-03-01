"""
Model evaluation pipeline.

Evaluate classifier accuracy, search relevance, and risk flagging quality.
"""

import argparse


def evaluate_classifier(model_path: str, test_data_path: str):
    """Evaluate clause classifier on test data."""
    # TODO: load model and test data
    # TODO: compute accuracy, precision, recall, F1
    print(f"Evaluating classifier from {model_path} on {test_data_path}...")


def evaluate_search(index_path: str, queries_path: str):
    """Evaluate semantic search with sample queries."""
    # TODO: load FAISS index and sample queries
    # TODO: compute retrieval metrics (MRR, recall@k)
    print(f"Evaluating search index from {index_path}...")


def main():
    parser = argparse.ArgumentParser(description="Evaluate CoParse ML models")
    parser.add_argument("--task", choices=["classifier", "search", "all"], default="all")
    parser.add_argument("--model", required=True, help="Path to model/index")
    parser.add_argument("--data", required=True, help="Path to test data")
    args = parser.parse_args()

    if args.task in ("classifier", "all"):
        evaluate_classifier(args.model, args.data)

    if args.task in ("search", "all"):
        evaluate_search(args.model, args.data)


if __name__ == "__main__":
    main()
