import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Analysis() {
  const navigate = useNavigate();
  const [threshold, setThreshold] = useState(2000);
  const [thresholdInput, setThresholdInput] = useState("2000");
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const username = localStorage.getItem("username");

    if (!username) {
      navigate("/");
      return;
    }

    const controller = new AbortController();

    const fetchJson = async (url) => {
      const response = await fetch(url, { signal: controller.signal });

      if (!response.ok) {
        throw new Error(`Failed request: ${url}`);
      }

      return response.json();
    };

    const loadAnalysis = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await fetchJson(
          `http://localhost:8080/api/analysis/overview?threshold=${encodeURIComponent(threshold)}`
        );

        setAnalysis(response?.data || null);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to load analysis at the moment.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadAnalysis();

    return () => controller.abort();
  }, [navigate, threshold]);

  const money = (value) => `₹${Math.round(Number(value || 0)).toLocaleString("en-IN")}`;

  const hidden = analysis?.hiddenExpenseDetector;
  const twin = analysis?.financialTwinSimulator;
  const microShare = Number(hidden?.microTransactionSharePercent || 0);
  const microTone = microShare >= 15 ? "text-danger" : microShare >= 8 ? "text-warning" : "value";
  const warningClass = hidden?.warningLevel === "High" ? "text-danger" : hidden?.warningLevel === "Medium" ? "text-warning" : "value";

  const hiddenCards = [
    {
      title: "Small Purchases",
      value: money(hidden?.microTransactionTotal),
      hint: `Below ${money(hidden?.threshold || threshold)}`,
    },
    {
      title: "Number of Transactions",
      value: String(hidden?.microTransactionCount || 0),
      hint: "Micro transactions this month",
    },
    {
      title: "Percentage of Spending",
      value: `${microShare.toFixed(0)}%`,
      hint: "Share of current month expenses",
    },
  ];

  const scenarioCards = [
    {
      title: twin?.currentPath?.scenario || "Current Path",
      value: money(twin?.currentPath?.fiveYearSavings),
      subtitle: "Based on your current monthly pattern",
      years: [
        { label: "1 year", value: twin?.currentPath?.oneYearSavings },
        { label: "3 years", value: twin?.currentPath?.threeYearSavings },
        { label: "5 years", value: twin?.currentPath?.fiveYearSavings },
      ],
    },
    {
      title: twin?.optimizedPath?.scenario || "Optimized Path",
      value: money(twin?.optimizedPath?.fiveYearSavings),
      subtitle: "Assumes 10% lower expenses",
      years: [
        { label: "1 year", value: twin?.optimizedPath?.oneYearSavings },
        { label: "3 years", value: twin?.optimizedPath?.threeYearSavings },
        { label: "5 years", value: twin?.optimizedPath?.fiveYearSavings },
      ],
    },
  ];

  const savingsGain = Number(twin?.projectedGainFromOptimization || 0);
  const averageIncome = Number(twin?.averageMonthlyIncome || 0);
  const averageExpense = Number(twin?.averageMonthlyExpense || 0);
  const averageSavings = Number(twin?.averageMonthlySavings || 0);
  const projectionBasisNote =
    "Projections use your average monthly income, expenses, and savings. The optimized path assumes expenses are reduced by 10%.";

  const applyThreshold = () => {
    const parsed = Number(thresholdInput);
    const nextThreshold = Number.isFinite(parsed) && parsed > 0 ? parsed : 2000;
    setThreshold(nextThreshold);
    setThresholdInput(String(nextThreshold));
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Insights</p>
          <h1 className="title">Financial intelligence center</h1>
          <p className="subtitle">Focused analysis for hidden spending, savings behavior, and future projection data.</p>
        </div>

        <div className="analysis-toolbar">
          <label className="stack" style={{ gap: "0.35rem" }}>
            <span className="dim">Micro-spend threshold</span>
            <input
              className="field compact-field"
              type="number"
              min="1"
              step="100"
              value={thresholdInput}
              onChange={(event) => setThresholdInput(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  applyThreshold();
                }
              }}
            />
          </label>
          <button className="btn btn-secondary" type="button" onClick={applyThreshold}>
            Apply
          </button>
          <button onClick={() => navigate("/reports/monthly")} className="btn btn-ghost" type="button">
            Monthly report
          </button>
          <button onClick={() => navigate("/dashboard")} className="btn btn-ghost" type="button">
            Back
          </button>
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}

      <section className="panel panel-pad stack analysis-sheet">
        <div className="stack">
          <div className="row">
            <div>
              <h2 style={{ margin: 0, fontSize: "1.15rem" }}>Hidden Expense Detector</h2>
              <p className="subtitle" style={{ marginBottom: 0 }}>
                Small recurring expenses that quietly add up this month.
              </p>
            </div>
            <span className={warningClass}>{hidden?.warningLevel ? `${hidden.warningLevel} warning` : "Loading"}</span>
          </div>

          {loading ? (
            <p className="note">Analyzing current month expenses...</p>
          ) : hidden ? (
            <>
              <div className="metrics-grid">
                {hiddenCards.map((card) => (
                  <div className="metric-card tone-blue" key={card.title}>
                    <div className="metric-title">{card.title}</div>
                    <div className="metric-value">{card.value}</div>
                    <div className="item-meta">{card.hint}</div>
                  </div>
                ))}
              </div>

              <div className="row">
                <span className="dim">Micro-spending share</span>
                <span className={microTone}>{microShare.toFixed(0)}%</span>
              </div>

              <div className="grid-2">
                <div className="panel panel-pad note" style={{ background: "rgba(20, 184, 122, 0.08)" }}>
                  {hidden.insight}
                </div>
                <div className="panel panel-pad note" style={{ background: "rgba(245, 158, 11, 0.08)" }}>
                  {hidden.savingsOpportunityInsight}
                </div>
              </div>

              <div className="panel panel-pad stack">
                <div className="row">
                  <h3 style={{ margin: 0, fontSize: "1.05rem" }}>Top micro-spend categories</h3>
                  <span className="dim">Sorted by amount</span>
                </div>

                {hidden.topCategories?.length ? (
                  <div className="list">
                    {hidden.topCategories.map((item) => (
                      <div className="item" key={item.category}>
                        <div>
                          <div style={{ textTransform: "capitalize" }}>{item.category}</div>
                          <div className="item-meta">{item.percent.toFixed(0)}% of micro-spending</div>
                        </div>
                        <div className="value">{money(item.amount)}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="note">No small transactions found under the current threshold.</p>
                )}
              </div>

              <div className="panel panel-pad note" style={{ margin: 0 }}>
                Micro spending accounts for {microShare.toFixed(0)}% of this month&apos;s total expenses.
              </div>
            </>
          ) : null}
        </div>

        <div className="analysis-divider" />

        <div className="stack">
          <div className="row">
            <div>
              <h2 style={{ margin: 0, fontSize: "1.15rem" }}>Financial Twin Simulator</h2>
              <p className="subtitle" style={{ marginBottom: 0 }}>Projection engine based on your monthly behavior.</p>
            </div>
            <span className="dim">1, 3, and 5 year outlook</span>
          </div>

          <p className="note note-tight">{projectionBasisNote}</p>

          {loading ? (
            <p className="note">Building future projections...</p>
          ) : twin ? (
            <>
              <div className="metrics-grid">
                <div className="metric-card tone-blue">
                  <div className="metric-title">Average monthly income</div>
                  <div className="metric-value">{money(averageIncome)}</div>
                </div>
                <div className="metric-card tone-neon">
                  <div className="metric-title">Average monthly expenses</div>
                  <div className="metric-value">{money(averageExpense)}</div>
                </div>
                <div className="metric-card tone-blue">
                  <div className="metric-title">Average monthly savings</div>
                  <div className="metric-value">{money(averageSavings)}</div>
                </div>
              </div>

              <div className="grid-2">
                {scenarioCards.map((scenario) => (
                  <div className="panel panel-pad stack" key={scenario.title}>
                    <div className="row">
                      <h3 style={{ margin: 0, fontSize: "1.05rem" }}>{scenario.title}</h3>
                      <span className="dim">{scenario.subtitle}</span>
                    </div>
                    <div className="metric-value" style={{ fontSize: "1.85rem" }}>{scenario.value}</div>
                    <div className="stack" style={{ gap: "0.35rem" }}>
                      {scenario.years.map((point) => (
                        <div className="row" key={point.label}>
                          <span className="dim">{point.label}</span>
                          <span className="value">{money(point.value)}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              <div className="grid-2 projection-summary">
                <div className="panel panel-pad stack">
                  <div className="row">
                    <h3 style={{ margin: 0, fontSize: "1.05rem" }}>5-year baseline</h3>
                    <span className="dim">Current pattern</span>
                  </div>
                  <div className="metric-value">{money(twin.currentPath?.fiveYearSavings)}</div>
                  <p className="note note-tight" style={{ margin: 0 }}>{twin.currentStrategyInsight}</p>
                </div>
                <div className="panel panel-pad stack">
                  <div className="row">
                    <h3 style={{ margin: 0, fontSize: "1.05rem" }}>5-year optimized</h3>
                    <span className="dim">10% lower expenses</span>
                  </div>
                  <div className="metric-value">{money(twin.optimizedPath?.fiveYearSavings)}</div>
                  <p className="note note-tight" style={{ margin: 0 }}>{twin.improvedStrategyInsight}</p>
                </div>
              </div>

              <div className="panel panel-pad stack compact-summary">
                <div className="row">
                  <h3 style={{ margin: 0, fontSize: "1.05rem" }}>Optimization gain</h3>
                  <span className="dim">5-year difference</span>
                </div>
                <div className="summary-stat">
                  <span className="dim">Projected gain</span>
                  <span className="value">{money(savingsGain)}</span>
                </div>
                <div className="summary-stat">
                  <span className="dim">Current vs optimized gap</span>
                  <span className="value">{money(Number(twin.optimizedPath?.fiveYearSavings || 0) - Number(twin.currentPath?.fiveYearSavings || 0))}</span>
                </div>
              </div>
            </>
          ) : null}
        </div>
      </section>
    </div>
  );
}
