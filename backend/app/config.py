from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    database_url: str = "postgresql+psycopg://coparse:coparse_dev@localhost:5432/coparse"
    storage_path: Path = Path("./storage")
    openai_api_key: str | None = None
    openai_model: str = "gpt-4o-mini"


settings = Settings()
