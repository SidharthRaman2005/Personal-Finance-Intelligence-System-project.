import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Pie, PieChart, ResponsiveContainer, Tooltip, Cell } from "recharts";

const PIE_COLORS = ["#62f58a", "#86efac", "#34d399", "#f59e0b", "#f87171", "#38bdf8", "#c084fc"];

export default function InvestmentAnalysis() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    const loadSummary = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await fetch("http://localhost:8080/api/investments/summary", {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error("Failed to load investment summary");
        }

        const data = await response.json();
        setSummary(data?.data || null);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to load investment analysis.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadSummary();

    return () => controller.abort();
  }, []);

  const allocationData = useMemo(() => {
    if (!summary?.allocationPercentageByType) {
      return [];
    }

    return Object.entries(summary.allocationPercentageByType)
      .map(([name, value]) => ({
        name,
        value: Number(value || 0),
      }))
      .sort((a, b) => b.value - a.value);
  }, [summary]);

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const totalInvested = Number(summary?.totalInvested || 0);
  const totalCurrent = Number(summary?.totalCurrentValue || 0);
  const profitLoss = Number(summary?.totalProfitLoss || 0);
  const diversificationScore = Number(summary?.diversificationScore || 0);
  const profitLabel = `${profitLoss >= 0 ? "+" : "-"}${money(Math.abs(profitLoss))}`;

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Investments</p>
          <h1 className="title">Allocation analysis</h1>
          <p className="subtitle">Visualize portfolio weight, diversification, and risk balance.</p>
        </div>
        <div className="actions-wrap">
          <button className="btn btn-ghost" onClick={() => navigate("/investments")}>Back</button>
          <button className="btn btn-secondary" onClick={() => navigate("/add-investment")}>Add investment</button>
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}

      <div className="summary-grid">
        <div className="metric-card tone-blue">
          <div className="metric-title">Total invested</div>
          <div className="metric-value">{money(totalInvested)}</div>
        </div>
        <div className="metric-card tone-neon">
          <div className="metric-title">Current value</div>
          <div className="metric-value">{money(totalCurrent)}</div>
        </div>
        
        <div className="metric-card tone-neon">
          <div className="metric-title">Diversification</div>
          <div className="metric-value">{diversificationScore.toFixed(0)}%</div>
        </div>
      </div>

      <div className="split-grid">
        <div className="panel panel-pad stack">
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Allocation chart</h2>
            <span className="dim">% of portfolio</span>
          </div>

          {loading ? (
            <p className="note">Loading allocation chart...</p>
          ) : allocationData.length === 0 ? (
            <p className="note">No allocation data yet. Add investments to see your mix.</p>
          ) : (
            <div className="chart-shell">
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie
                    data={allocationData}
                    dataKey="value"
                    nameKey="name"
                    innerRadius={70}
                    outerRadius={110}
                    paddingAngle={2}
                  >
                    {allocationData.map((entry, index) => (
                      <Cell key={`${entry.name}-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(value, name) => [`${Number(value).toFixed(1)}%`, name]}
                    contentStyle={{
                      background: "#14181c",
                      border: "1px solid #2a2f35",
                      borderRadius: "12px",
                      color: "#edf2ee",
                    }}
                    itemStyle={{ color: "#edf2ee" }}
                  />
                </PieChart>
              </ResponsiveContainer>

              <div className="pie-legend">
                {allocationData.map((item, index) => (
                  <div className="pie-legend-row" key={item.name}>
                    <div className="row" style={{ width: "100%" }}>
                      <span className="legend-left">
                        <span
                          className="legend-dot"
                          style={{ backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }}
                        />
                        <span>{item.name}</span>
                      </span>
                      <span className="value">{item.value.toFixed(1)}%</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="panel panel-pad stack">
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Health signals</h2>
            <span className="dim">Portfolio focus</span>
          </div>

          <div className="stack" style={{ gap: "0.4rem" }}>
            <div className="row">
              <span className="dim">Highest concentration</span>
              <span className="value">{summary?.highestInvestmentCategory || "-"}</span>
            </div>
            <div className="row">
              <span className="dim">Diversification score</span>
              <span className="value">{diversificationScore.toFixed(0)}%</span>
            </div>
            <div className="progress-track">
              <div className="progress-fill" style={{ width: `${Math.min(100, diversificationScore)}%` }} />
            </div>
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
            <p className="note">Insights will appear once investments are tracked.</p>
          )}
        </div>
      </div>
    </div>
  );
}
