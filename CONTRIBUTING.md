# Contributing to CoParse

Welcome to the team! This guide covers everything you need to know to start contributing.

---

## First-Time Setup

1. **Clone the repo:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/CoParse.git
   cd CoParse
   ```

2. **Set up your environments** — follow the instructions in the main [README.md](README.md).

3. **Configure Git** (if you haven't already):
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```

---

## Branch Naming Convention

Always create a new branch for your work. Use this format:

| Prefix | Use Case | Example |
|--------|----------|---------|
| `feature/` | New feature | `feature/semantic-search` |
| `fix/` | Bug fix | `fix/search-results-empty` |
| `docs/` | Documentation only | `docs/update-api-reference` |
| `refactor/` | Code restructuring | `refactor/clean-up-routes` |
| `ml/` | ML pipeline or model work | `ml/clause-classifier-v1` |

```bash
git checkout -b feature/your-feature-name
```

---

## Commit Message Guidelines

Write clear, descriptive commit messages:

```
<type>: <short summary>

# Examples:
feat: add semantic search endpoint
fix: handle empty contract uploads
docs: update setup instructions for Windows
ml: add clause classification training script
refactor: extract search logic into service layer
```

**Types:** `feat`, `fix`, `docs`, `ml`, `refactor`, `test`, `chore`

---

## Pull Request Process

1. **Push your branch** to GitHub:
   ```bash
   git push -u origin feature/your-feature-name
   ```

2. **Open a Pull Request** on GitHub against `main`.

3. **Fill out the PR template** — describe what you changed and why.

4. **Request a review** from at least one teammate.

5. **Address feedback** — push new commits to the same branch.

6. **Merge** once approved. Use "Squash and merge" to keep history clean.

---

## Code Style

### Python (Backend & ML)
- Follow [PEP 8](https://peps.python.org/pep-0008/)
- Use type hints where practical
- Keep functions focused and under ~50 lines
- Use docstrings for public functions

### JavaScript (Frontend)
- Use functional components and React hooks
- Use `camelCase` for variables/functions, `PascalCase` for components
- Keep components small and composable

---

## What to Work On

Check the **Issues** tab on GitHub for open tasks. Issues are labeled by area:

- `backend` — API and server work
- `frontend` — React UI work
- `ml` — Machine learning and NLP
- `docs` — Documentation
- `good first issue` — Great starting points for new contributors

If you want to work on something, comment on the issue so others know.

---

## Communication

- Use **GitHub Issues** for task tracking and bugs
- Use **Pull Request comments** for code discussion
- Keep teammates in the loop — if you're stuck, ask!

---

## Questions?

If anything is unclear, open an issue or ask a teammate. There are no bad questions.
