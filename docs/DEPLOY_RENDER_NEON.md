# Deploy CoParse API to Render + Neon (free tier)

The Android app talks to this **FastAPI** backend over HTTPS. You run **Postgres on Neon** (free tier) and the **API on Render** (free tier). This repo includes a **Render Blueprint** so you can wire things up without Docker on your side.

## What you need

- GitHub (or GitLab) repo connected to Render
- A Neon account ([neon.tech](https://neon.tech))
- A Render account ([render.com](https://render.com))

## 1. Create a Neon database

1. In Neon: create a project and a database (e.g. `neondb`).
2. Copy the **connection string** (it looks like `postgresql://...@ep-....neon.tech/neondb?sslmode=require`).

### SQLAlchemy + psycopg3

This app expects a URL that uses the **psycopg3** driver. Replace the scheme:

- From: `postgresql://...`
- To: `postgresql+psycopg://...`

Keep the rest of the string (host, user, password, database, `sslmode=require`, etc.) unchanged.

Example:

```text
postgresql+psycopg://user:pass@ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
```

Paste that value into Render as `DATABASE_URL` (next step).

## 2. Deploy on Render (Blueprint)

### Option A — `render.yaml` (recommended)

1. Push this repo to GitHub (including `render.yaml` at the repo root).
2. In Render: **New** → **Blueprint** → connect the repository.
3. Apply the blueprint. Render will create the **Web Service** `coparse-api`.
4. Before or after the first deploy, open the service → **Environment**:
   - Set **`DATABASE_URL`** to the Neon string above (with `postgresql+psycopg://`).
   - Leave **`OPENAI_API_KEY`** empty if you want **no OpenAI API cost** (analysis still runs on rules/templates).
5. **Manual Deploy** if the first build failed before `DATABASE_URL` was set (migrations need a valid DB).

Migrations run automatically via **`preDeployCommand`**: `alembic upgrade head`.

### Option B — Manual Web Service

If you prefer not to use the blueprint:

- **Root directory:** `backend`
- **Build command:** `pip install -r requirements.txt`
- **Pre-deploy command:** `alembic upgrade head`
- **Start command:** `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
- **Environment:** same `DATABASE_URL` (and optional `OPENAI_API_KEY`).

## 3. Smoke test

Your service URL will look like `https://coparse-api.onrender.com` (name may vary).

```bash
curl https://YOUR-SERVICE.onrender.com/health
# {"status":"ok"}
```

Open `https://YOUR-SERVICE.onrender.com/docs` for OpenAPI.

## 4. Point the Android release build at production

Set your API URL in `android/local.properties`:

```properties
coparse.releaseApiBaseUrl=https://YOUR-SERVICE.onrender.com/
```

Rebuild a **release** APK/AAB. The app uses `BuildConfig.API_BASE_URL` from that property (see `android/app/build.gradle.kts`).

## Free-tier caveats

- **Cold starts:** Render free web services **sleep** after idle time. The first request after sleep can take **tens of seconds**.
- **Upload storage:** Files live under `./storage` on the instance disk. On free dynos that disk is **ephemeral**; redeploys can wipe uploads. For production persistence, add object storage (e.g. S3-compatible) in a later iteration.
- **Neon limits:** Free tier has caps on storage and compute; fine for early testing.

## Troubleshooting

- **Pre-deploy / migration fails:** Ensure `DATABASE_URL` is set and uses `postgresql+psycopg://` as above.
- **SSL errors to Neon:** Include `sslmode=require` in the connection string (Neon usually adds it).
