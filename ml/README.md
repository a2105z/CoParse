# CoParse ML Pipeline

Machine learning and NLP components for semantic search, clause classification, and risk analysis.

## Setup

```bash
cd ml
python -m venv venv
venv\Scripts\activate        # Windows
# source venv/bin/activate   # macOS/Linux
pip install -r requirements.txt
python -m spacy download en_core_web_sm
```

## Structure

```
ml/
├── notebooks/           # Jupyter notebooks for exploration and prototyping
├── scripts/
│   ├── preprocess.py    # Data cleaning and preparation
│   ├── train.py         # Model training pipelines
│   └── evaluate.py      # Model evaluation and metrics
├── models/              # Saved model artifacts (git-ignored, except .gitkeep)
├── data/                # Datasets (git-ignored, except .gitkeep)
└── tests/               # ML pipeline tests
```

## Key Components

### 1. Semantic Search
- Encode contract clauses using sentence-transformers
- Build a FAISS vector index for fast similarity search
- Query by natural language to find relevant clauses

### 2. Clause Classification
- Classify clauses by type (termination, liability, non-compete, confidentiality, etc.)
- Multi-label classification using fine-tuned transformers or scikit-learn

### 3. Risk Flagging
- Compare clauses against market-norm baselines
- Flag clauses that are risky, unusual, or missing for the user's role
- Generate plain-English explanations

## Notebooks

Use the `notebooks/` folder for experimentation. Name notebooks descriptively:
- `01_data_exploration.ipynb`
- `02_embedding_experiments.ipynb`
- `03_classifier_training.ipynb`

## Data

Place datasets in `ml/data/`. This folder is git-ignored to avoid committing large files.
Share data with the team via a shared drive or cloud storage link documented here.
