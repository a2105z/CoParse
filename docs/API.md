# API (MVP)

Base URL: `http://<host>:8000`

OpenAPI (interactive): `/docs`

## Authentication

MVP: none (add device token or auth in production).

## Endpoints

### `POST /v1/documents`

Multipart form:

- `file` (required): PDF or plain text (`.txt`).
- `hint_contract_type` (optional): `lease` | `internship_offer` | `freelance` | `auto`
- `hint_role` (optional): e.g. `renter`, `student_intern`, `freelancer`, `general`

Response: `document_id`, `job_id`.

### `GET /v1/jobs/{job_id}`

Returns job `status`: `pending` | `processing` | `completed` | `failed`, optional `error_message`, `progress_steps`.

### `GET /v1/documents/{document_id}/analysis`

Returns full analysis (scores, clauses, missing protections, questions). `404` until job completes.

### `POST /v1/documents/{document_id}/reanalyze`

JSON body:

```json
{
  "contract_type": "lease",
  "role": "renter"
}
```

Triggers a new job and re-runs the pipeline with user-confirmed type and role.
