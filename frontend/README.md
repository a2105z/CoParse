# CoParse Frontend

React single-page application for CoParse.

## Setup

```bash
cd frontend
npm install
```

## Run

```bash
npm start
```

App runs at http://localhost:3000.

## Structure

```
frontend/
├── public/
│   └── index.html
└── src/
    ├── App.js              # Main app component
    ├── index.js            # Entry point
    ├── components/
    │   ├── SearchBar.js    # Semantic search input
    │   └── ResultsList.js  # Display search/analysis results
    ├── pages/              # Page-level components (to be added)
    ├── services/
    │   └── api.js          # API client for backend communication
    └── styles/
        ├── index.css       # Global styles
        └── App.css         # Component styles
```
