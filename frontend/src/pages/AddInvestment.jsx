import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getInvestmentNameSuggestions,
  isSuggestionType,
} from "../data/investmentSuggestions";

const INVESTMENT_TYPES = [
  "Stocks",
  "Mutual Funds",
  "Fixed Deposits",
  "Gold",
  "Crypto",
  "Real Estate",
  "PPF",
  "EPF",
  "Bonds",
  "Savings",
];

const RISK_LEVELS = ["Low", "Moderate", "High"];

export default function AddInvestment() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    investmentType: "",
    investmentName: "",
    investedAmount: "",
    purchaseDate: "",
    riskLevel: "",
    notes: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const showSuggestions = isSuggestionType(form.investmentType);
  const nameSuggestions = showSuggestions
    ? getInvestmentNameSuggestions(form.investmentType, form.investmentName)
    : [];

  const updateField = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async () => {
    const investedAmount = Number(form.investedAmount);

    if (!form.investmentType) {
      setError("Select an investment type.");
      return;
    }

    if (!form.investmentName.trim()) {
      setError("Enter an investment name.");
      return;
    }

    if (!Number.isFinite(investedAmount) || investedAmount < 0) {
      setError("Enter a valid invested amount (0 or higher).");
      return;
    }

    if (!form.purchaseDate) {
      setError("Select a purchase date.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await fetch("http://localhost:8080/api/investments/add", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          investmentType: form.investmentType,
          investmentName: form.investmentName.trim(),
          investedAmount,
          purchaseDate: form.purchaseDate,
          riskLevel: form.riskLevel || null,
          notes: form.notes.trim() || null,
        }),
      });

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to save investment");
      }

      navigate("/investments");
    } catch (err) {
      setError(err.message || "Unable to save the investment.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Investments</p>
          <h1 className="title">Add a new investment</h1>
          <p className="subtitle">Track portfolio entries with risk posture and purchase details.</p>
        </div>
        <button onClick={() => navigate("/investments")} className="btn btn-ghost">
          Back
        </button>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}

      <div className="panel panel-pad stack">
        <div className="form-grid">
          <div className="stack">
            <label className="item-meta">Investment type</label>
            <select
              className="field"
              value={form.investmentType}
              onChange={(event) => updateField("investmentType", event.target.value)}
            >
              <option value="">Select type</option>
              {INVESTMENT_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>

          <div className="stack">
            <label className="item-meta">Investment name</label>
            <input
              className="field"
              placeholder="e.g. HDFC Index Fund"
              value={form.investmentName}
              list={showSuggestions ? "investment-name-suggestions" : undefined}
              autoComplete={showSuggestions ? "off" : "on"}
              onChange={(event) => updateField("investmentName", event.target.value)}
            />
            {showSuggestions ? (
              <datalist id="investment-name-suggestions">
                {nameSuggestions.map((suggestion) => (
                  <option key={suggestion} value={suggestion} />
                ))}
              </datalist>
            ) : null}
            {showSuggestions ? (
              <span className="item-meta">Type to see suggestions, or enter a custom name.</span>
            ) : null}
          </div>

          <div className="stack">
            <label className="item-meta">Invested amount</label>
            <input
              className="field"
              type="number"
              placeholder="0"
              value={form.investedAmount}
              onChange={(event) => updateField("investedAmount", event.target.value)}
            />
          </div>

          <div className="stack">
            <label className="item-meta">Purchase date</label>
            <input
              className="field"
              type="date"
              value={form.purchaseDate}
              onChange={(event) => updateField("purchaseDate", event.target.value)}
            />
          </div>

          <div className="stack">
            <label className="item-meta">Risk level</label>
            <select
              className="field"
              value={form.riskLevel}
              onChange={(event) => updateField("riskLevel", event.target.value)}
            >
              <option value="">Select level</option>
              {RISK_LEVELS.map((level) => (
                <option key={level} value={level}>
                  {level}
                </option>
              ))}
            </select>
          </div>

          <div className="stack full">
            <label className="item-meta">Notes</label>
            <textarea
              className="field"
              rows="3"
              placeholder="Optional notes or strategy"
              value={form.notes}
              onChange={(event) => updateField("notes", event.target.value)}
            />
          </div>
        </div>

        <div className="actions-wrap">
          <button className="btn btn-ghost" type="button" onClick={() => navigate("/investments")}>Back to list</button>
          <button className="btn btn-primary" type="button" onClick={handleSubmit} disabled={loading}>
            {loading ? "Saving..." : "Save Investment"}
          </button>
        </div>
      </div>
    </div>
  );
}
