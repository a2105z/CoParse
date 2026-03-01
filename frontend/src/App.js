import React, { useState } from "react";
import SearchBar from "./components/SearchBar";
import ResultsList from "./components/ResultsList";
import "./styles/App.css";

function App() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (query, role) => {
    setLoading(true);
    try {
      const response = await fetch(
        `http://localhost:8000/api/search/?q=${encodeURIComponent(query)}&role=${role}`
      );
      const data = await response.json();
      setResults(data.results || []);
    } catch (error) {
      console.error("Search failed:", error);
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>CoParse</h1>
        <p>Understand your contracts before you sign.</p>
      </header>
      <main>
        <SearchBar onSearch={handleSearch} />
        {loading && <p className="loading">Searching...</p>}
        <ResultsList results={results} />
      </main>
    </div>
  );
}

export default App;
