import React from "react";

function ResultsList({ results }) {
  if (!results || results.length === 0) {
    return (
      <div className="results-empty">
        <p>No results yet. Try searching for a clause or uploading a contract.</p>
      </div>
    );
  }

  return (
    <div className="results-list">
      {results.map((result, index) => (
        <div key={index} className={`result-card risk-${result.risk_level}`}>
          <div className="result-header">
            <span className="clause-type">{result.clause_type}</span>
            <span className={`risk-badge ${result.risk_level}`}>
              {result.risk_level} risk
            </span>
          </div>
          <p className="clause-text">{result.text}</p>
          <p className="explanation">{result.explanation}</p>
        </div>
      ))}
    </div>
  );
}

export default ResultsList;
