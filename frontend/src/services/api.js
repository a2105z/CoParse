const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8000";

export async function searchContracts(query, role = "general") {
  const response = await fetch(
    `${API_BASE}/api/search/?q=${encodeURIComponent(query)}&role=${role}`
  );
  if (!response.ok) throw new Error("Search request failed");
  return response.json();
}

export async function analyzeContract(file, role = "general") {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("role", role);

  const response = await fetch(`${API_BASE}/api/analysis/`, {
    method: "POST",
    body: formData,
  });
  if (!response.ok) throw new Error("Analysis request failed");
  return response.json();
}

export async function getContracts() {
  const response = await fetch(`${API_BASE}/api/contracts/`);
  if (!response.ok) throw new Error("Failed to fetch contracts");
  return response.json();
}
