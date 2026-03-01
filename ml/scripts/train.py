"""
Model training pipeline.

Trains the clause classifier and builds the semantic search index.
"""

import argparse


def train_classifier(data_path: str, output_path: str):
    """Train the clause classification model."""
    # TODO: load preprocessed data
    # TODO: fine-tune transformer or train sklearn classifier
    # TODO: save model to output_path
    print(f"Training classifier with data from {data_path}...")
    print(f"Model will be saved to {output_path}")


def build_search_index(data_path: str, output_path: str):
    """Build FAISS index from clause embeddings."""
    # TODO: encode clauses with sentence-transformers
    # TODO: build and save FAISS index
    print(f"Building search index from {data_path}...")
    print(f"Index will be saved to {output_path}")


def main():
    parser = argparse.ArgumentParser(description="Train CoParse ML models")
    parser.add_argument("--task", choices=["classifier", "index", "all"], default="all")
    parser.add_argument("--data", required=True, help="Path to processed data")
    parser.add_argument("--output", default="../models", help="Output directory for models")
    args = parser.parse_args()

    if args.task in ("classifier", "all"):
        train_classifier(args.data, args.output)

    if args.task in ("index", "all"):
        build_search_index(args.data, args.output)


if __name__ == "__main__":
    main()
