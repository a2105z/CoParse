from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_root():
    response = client.get("/")
    assert response.status_code == 200
    assert "CoParse" in response.json()["message"]


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"


def test_search_endpoint():
    response = client.get("/api/search/?q=termination+clause")
    assert response.status_code == 200
    assert response.json()["query"] == "termination clause"


def test_contracts_list():
    response = client.get("/api/contracts/")
    assert response.status_code == 200
