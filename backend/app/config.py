import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    APP_NAME: str = "CoParse"
    DEBUG: bool = os.getenv("DEBUG", "true").lower() == "true"
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./coparse.db")
    ML_MODEL_PATH: str = os.getenv("ML_MODEL_PATH", "../ml/models")


settings = Settings()
