import { useEffect, useMemo, useState } from "react";
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

const emptyForm = {
  investmentType: "",
  investmentName: "",
  investedAmount: "",
  purchaseDate: "",
  riskLevel: "",
  notes: "",
};

export default function Investments() {
  const navigate = useNavigate();
  const [investments, setInvestments] = useState([]);
  const [summary, setSummary] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [refreshTick, setRefreshTick] = useState(0);

  const showEditSuggestions = isSuggestionType(editForm.investmentType);
  const editNameSuggestions = showEditSuggestions
    ? getInvestmentNameSuggestions(editForm.investmentType, editForm.investmentName)
    : [];

  useEffect(() => {
    const controller = new AbortController();

    const fetchJson = async (url, optional = false) => {
      const response = await fetch(url, { signal: controller.signal });

      if (!response.ok) {
        if (optional) {
          return null;
        }
        throw new Error(`Failed request: ${url}`);
      }

      return response.json();
    };

    const loadInvestments = async () => {
      try {
        setLoading(true);
        setError("");

        const [investmentsJson, summaryJson] = await Promise.all([
          fetchJson("http://localhost:8080/api/investments"),
          fetchJson("http://localhost:8080/api/investments/summary", true),
        ]);

        setInvestments(Array.isArray(investmentsJson?.data) ? investmentsJson.data : []);
        setSummary(summaryJson?.data || null);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to load investments right now.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadInvestments();

    return () => controller.abort();
  }, [refreshTick]);

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const totalInvested = Number(summary?.totalInvested || 0);
  const diversificationScore = Number(summary?.diversificationScore || 0);

  const allocationRows = useMemo(() => {
    if (!summary?.allocationPercentageByType) {
      return [];
    }

    return Object.entries(summary.allocationPercentageByType)
      .map(([category, percent]) => ({
        category,
        percent: Number(percent || 0),
      }))
      .sort((a, b) => b.percent - a.percent);
  }, [summary]);

  const investmentCount = investments.length;

  const riskTone = (risk) => {
    const normalized = String(risk || "").toLowerCase();
    if (normalized.includes("high")) {
      return "pill high";
    }
    if (normalized.includes("moderate")) {
      return "pill moderate";
    }
    if (normalized.includes("low")) {
      return "pill low";
    }
    return "pill";
  };

  const handleStartEdit = (investment) => {
    setEditingId(investment.id);
    setEditForm({
      investmentType: investment.investmentType || "",
      investmentName: investment.investmentName || "",
      investedAmount: investment.investedAmount ?? "",
      purchaseDate: investment.purchaseDate || "",
      riskLevel: investment.riskLevel || "",
      notes: investment.notes || "",
    });
    setSuccessMessage("");
    setError("");
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditForm(emptyForm);
  };

  const handleUpdate = async () => {
    if (!editingId) {
      return;
    }

    const investedAmount = Number(editForm.investedAmount);

    if (!editForm.investmentType) {
      setError("Select an investment type.");
      return;
    }

    if (!editForm.investmentName.trim()) {
      setError("Investment name is required.");
      return;
    }

    if (!Number.isFinite(investedAmount) || investedAmount < 0) {
      setError("Enter a valid invested amount (0 or higher).");
      return;
    }

    if (!editForm.purchaseDate) {
      setError("Select a purchase date.");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch(`http://localhost:8080/api/investments/${editingId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          investmentType: editForm.investmentType,
          investmentName: editForm.investmentName.trim(),
          investedAmount,
          purchaseDate: editForm.purchaseDate,
          riskLevel: editForm.riskLevel || null,
          notes: editForm.notes.trim() || null,
        }),
      });

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to update investment");
      }

      setSuccessMessage("Investment updated.");
      setEditingId(null);
      setEditForm(emptyForm);
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError(err.message || "Unable to update the investment.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (investmentId) => {
    if (!window.confirm("Delete this investment?") || !investmentId) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch(`http://localhost:8080/api/investments/${investmentId}`, {
        method: "DELETE",
      });

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to delete investment");
      }

      setSuccessMessage("Investment deleted.");
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError(err.message || "Unable to delete the investment.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Portfolio</p>
          <h1 className="title">Investment management</h1>
          <p className="subtitle">Track allocations, risk posture, and performance in one view.</p>
        </div>
        <div className="actions-wrap">
          <button className="btn btn-ghost" onClick={() => navigate("/dashboard")}>Back</button>
          <button className="btn btn-secondary" onClick={() => navigate("/investment-analysis")}>Analysis</button>
          <button className="btn btn-primary" onClick={() => navigate("/add-investment")}>Add investment</button>
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}
      {successMessage ? <div className="panel panel-pad note">{successMessage}</div> : null}

      <div className="summary-grid">
        <div className="metric-card tone-blue">
          <div className="metric-title">Total invested</div>
          <div className="metric-value">{money(totalInvested)}</div>
        </div>
        <div className="metric-card tone-neon">
          <div className="metric-title">Diversification</div>
          <div className="metric-value">{diversificationScore.toFixed(0)}%</div>
        </div>
      </div>

      <div className="split-grid">
        <div className="panel panel-pad stack">
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Allocation by type</h2>
            <span className="dim">{investmentCount} holdings</span>
          </div>

          {loading ? (
            <p className="note">Loading allocation...</p>
          ) : allocationRows.length === 0 ? (
            <p className="note">No allocation data yet. Add your first investment.</p>
          ) : (
            <div className="category-list">
              {allocationRows.map((row) => (
                <div key={row.category} className="category-item">
                  <div className="row">
                    <span>{row.category}</span>
                    <span className="value">{row.percent.toFixed(1)}%</span>
                  </div>
                  <div className="bar">
                    <div className="bar-fill" style={{ width: `${row.percent}%` }} />
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="actions-wrap">
            <button className="btn btn-ghost" onClick={() => navigate("/investment-analysis")}>Open allocation chart</button>
          </div>
        </div>

        <div className="panel panel-pad stack">
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Portfolio insights</h2>
            <span className="dim">Auto signals</span>
          </div>

          {summary?.insights?.length ? (
            <div className="insight-list">
              {summary.insights.map((insight, index) => (
                <div
                  key={`${insight}-${index}`}
                  className={`insight-card ${index % 2 === 0 ? "tone-blue" : "tone-neon"}`}
                >
                  {insight}
                </div>
              ))}
            </div>
          ) : (
            <p className="note">Insights appear after you add investments.</p>
          )}

          <div className="row">
            <span className="dim">Highest concentration</span>
            <span className="value">{summary?.highestInvestmentCategory || "-"}</span>
          </div>
        </div>
      </div>

      {editingId ? (
        <div className="panel panel-pad stack">
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Edit investment</h2>
            <button className="btn btn-ghost" type="button" onClick={handleCancelEdit}>Cancel</button>
          </div>

          <div className="form-grid">
            <div className="stack">
              <label className="item-meta">Investment type</label>
              <select
                className="field"
                value={editForm.investmentType}
                onChange={(event) => setEditForm((prev) => ({ ...prev, investmentType: event.target.value }))}
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
                value={editForm.investmentName}
                list={showEditSuggestions ? "investment-name-edit-suggestions" : undefined}
                autoComplete={showEditSuggestions ? "off" : "on"}
                onChange={(event) => setEditForm((prev) => ({ ...prev, investmentName: event.target.value }))}
              />
              {showEditSuggestions ? (
                <datalist id="investment-name-edit-suggestions">
                  {editNameSuggestions.map((suggestion) => (
                    <option key={suggestion} value={suggestion} />
                  ))}
                </datalist>
              ) : null}
            </div>

            <div className="stack">
              <label className="item-meta">Invested amount</label>
              <input
                className="field"
                type="number"
                value={editForm.investedAmount}
                onChange={(event) => setEditForm((prev) => ({ ...prev, investedAmount: event.target.value }))}
              />
            </div>

            <div className="stack">
              <label className="item-meta">Purchase date</label>
              <input
                className="field"
                type="date"
                value={editForm.purchaseDate}
                onChange={(event) => setEditForm((prev) => ({ ...prev, purchaseDate: event.target.value }))}
              />
            </div>

            <div className="stack">
              <label className="item-meta">Risk level</label>
              <select
                className="field"
                value={editForm.riskLevel}
                onChange={(event) => setEditForm((prev) => ({ ...prev, riskLevel: event.target.value }))}
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
                value={editForm.notes}
                onChange={(event) => setEditForm((prev) => ({ ...prev, notes: event.target.value }))}
              />
            </div>
          </div>

          <div className="actions-wrap">
            <button className="btn btn-secondary" type="button" onClick={handleUpdate} disabled={saving}>
              {saving ? "Saving..." : "Save changes"}
            </button>
          </div>
        </div>
      ) : null}

      <div className="panel panel-pad stack">
        <div className="row">
          <h2 style={{ margin: 0, fontSize: "1.15rem" }}>All investments</h2>
          <span className="dim">{investmentCount} entries</span>
        </div>

        {loading ? (
          <p className="note">Loading investments...</p>
        ) : investments.length === 0 ? (
          <p className="note">No investments yet. Add one to start tracking.</p>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Type</th>
                  <th className="right">Invested</th>
                  <th>Risk</th>
                  <th>Date</th>
                  <th className="right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {investments.map((investment) => {
                  const investedAmount = Number(investment.investedAmount || 0);

                  return (
                    <tr key={investment.id}>
                      <td>{investment.investmentName}</td>
                      <td><span className="pill">{investment.investmentType}</span></td>
                      <td className="right">{money(investedAmount)}</td>
                      <td><span className={riskTone(investment.riskLevel)}>{investment.riskLevel || "-"}</span></td>
                      <td>{investment.purchaseDate || "-"}</td>
                      <td className="right">
                        <div className="table-actions">
                          <button
                            className="btn btn-ghost"
                            type="button"
                            onClick={() => handleStartEdit(investment)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-danger"
                            type="button"
                            onClick={() => handleDelete(investment.id)}
                            disabled={saving}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
