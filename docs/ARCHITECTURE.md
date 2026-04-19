# Architecture

```mermaid
flowchart LR
  subgraph client [Android]
    Compose[Compose UI]
    VM[ViewModels]
    Repo[Repositories]
    Room[Room cache]
  end
  subgraph server [Backend]
    API[FastAPI]
    Pipe[Analysis pipeline]
    DB[(PostgreSQL)]
  end
  Compose --> VM --> Repo
  Repo --> API
  Repo --> Room
  API --> Pipe
  API --> DB
  Pipe --> DB
```

- **Client:** MVVM; Retrofit calls `/v1/*`; Room stores recent analyses and starred clauses for offline viewing.
- **Server:** Uploads stored on disk (`STORAGE_PATH`); metadata and analysis in PostgreSQL; processing runs in-process (async task) with job rows for status polling.

See also: [API.md](API.md), [PRODUCT.md](PRODUCT.md).
