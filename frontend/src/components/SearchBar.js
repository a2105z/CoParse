import React, { useState } from "react";

const ROLES = ["general", "intern", "tenant", "contractor", "employee"];

function SearchBar({ onSearch }) {
  const [query, setQuery] = useState("");
  const [role, setRole] = useState("general");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) {
      onSearch(query.trim(), role);
    }
  };

  return (
    <form className="search-bar" onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Describe what you're looking for (e.g., 'non-compete clause')"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      <select value={role} onChange={(e) => setRole(e.target.value)}>
        {ROLES.map((r) => (
          <option key={r} value={r}>
            {r.charAt(0).toUpperCase() + r.slice(1)}
          </option>
        ))}
      </select>
      <button type="submit">Search</button>
    </form>
  );
}

export default SearchBar;
