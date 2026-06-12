import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Details() {
  const today = new Date();
  const month = today.getMonth() + 1;
  const year = today.getFullYear();

  const [username, setUsername] = useState("");
  const [summary, setSummary] = useState(null);
  const [expenses, setExpenses] = useState([]);
  const [goals, setGoals] = useState([]);
  const [health, setHealth] = useState(null);
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    const user = localStorage.getItem("username");

    if (!user) {
      navigate("/", { replace: true });
      return;
    }

    setUsername(user);

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

    const loadData = async () => {
      try {
        setLoading(true);
        setError("");

        const [summaryJson, expenseJson, goalsJson, healthJson, insightJson] = await Promise.all([
          fetchJson("http://localhost:8080/api/dashboard"),
          fetchJson("http://localhost:8080/api/expenses/user"),
          fetchJson("http://localhost:8080/api/goals", true),
          fetchJson(
            `http://localhost:8080/api/insights/health-score?month=${month}&year=${year}`,
            true
          ),
          fetchJson(
            `http://localhost:8080/api/insights/monthly?month=${month}&year=${year}`,
            true
          ),
        ]);

        setSummary(summaryJson || null);
        setExpenses(Array.isArray(expenseJson?.data) ? expenseJson.data : []);
        setGoals(Array.isArray(goalsJson?.data) ? goalsJson.data : []);
        setHealth(healthJson?.data || null);
        setInsights(Array.isArray(insightJson?.data?.insights) ? insightJson.data.insights : []);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to load detailed dashboard data.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadData();

    return () => controller.abort();
  }, [month, navigate, year]);

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const sortedExpenses = useMemo(
    () => [...expenses].sort((a, b) => new Date(b.date) - new Date(a.date)),
    [expenses]
  );

  const sortedGoals = useMemo(
    () => [...goals].sort((a, b) => Number(b.progress || 0) - Number(a.progress || 0)),
    [goals]
  );

  const score = Math.round(Number(health?.score || 0));
  const monthlyBudget = Number(summary?.monthlyBudget || 0);
  const remainingBudget = Number(summary?.remainingBudget || 0);
  const usedPercent = Math.max(0, Math.min(100, Number(summary?.percentageUsed || 0)));

  return (
    <div className="page">
      <div className="page-head animate-rise" style={{ "--delay": "0ms" }}>
        <div>
          <p className="eyebrow">Detailed Hub</p>
          <h1 className="title">Performance and data deep dive</h1>
          <p className="subtitle">{username ? `Everything for ${username}` : "Your complete monthly financial details"}</p>
        </div>

        <div className="actions-wrap">
          <button className="btn btn-ghost" onClick={() => navigate("/dashboard")}>Back to Dashboard</button>
          <button className="btn btn-secondary" onClick={() => navigate("/analysis")}>Open Analysis Charts</button>
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}

      <div className="details-top-grid">
        <div className="panel panel-pad stack animate-rise" style={{ "--delay": "70ms" }}>
          <div className="row">
            <h3 style={{ margin: 0 }}>Financial score</h3>
            <span className="dim">Score out of 100</span>
          </div>

          {loading ? (
            <p className="note">Loading score...</p>
          ) : health ? (
            <>
              <div className="details-score">{score}/100</div>
              <div className="row"><span className="dim">Status</span><span className="value">{health.status}</span></div>
              <div className="row"><span className="dim">Savings rate</span><span>{Number(health.savingsRate || 0).toFixed(1)}%</span></div>
              <p className="note" style={{ margin: 0 }}>{health.message}</p>

              {health.breakdown ? (
                <div className="score-breakdown">
                  <div className="breakdown-row"><span>Budget Control</span><span className={health.breakdown.budgetControlStatus === "High" ? "text-danger" : "value"}>{health.breakdown.budgetControlStatus}</span></div>
                  <div className="breakdown-row"><span>Savings</span><span className={health.breakdown.savingsStatus === "High" ? "text-danger" : "value"}>{health.breakdown.savingsStatus}</span></div>
                  <div className="breakdown-row"><span>Stability</span><span className={health.breakdown.stabilityStatus === "High" ? "text-danger" : "value"}>{health.breakdown.stabilityStatus}</span></div>
                </div>
              ) : null}
            </>
          ) : (
            <p className="note">No health data for this month yet.</p>
          )}
        </div>

        <div className="panel panel-pad stack animate-rise" style={{ "--delay": "140ms" }}>
          <div className="row">
            <h3 style={{ margin: 0 }}>Budget and summary</h3>
            <span className="dim">Current month</span>
          </div>

          {loading ? (
            <p className="note">Loading summary...</p>
          ) : summary ? (
            <>
              <div className="row"><span className="dim">Total Income</span><span>{money(summary.totalIncome)}</span></div>
              <div className="row"><span className="dim">Total Expense</span><span>{money(summary.totalExpense)}</span></div>
              <div className="row"><span className="dim">Net Balance</span><span className="value">{money(summary.netBalance)}</span></div>
              <div className="row"><span className="dim">Monthly Budget</span><span>{monthlyBudget > 0 ? money(monthlyBudget) : "Not set"}</span></div>
              <div className="row"><span className="dim">Remaining Budget</span><span className={remainingBudget < 0 ? "text-danger" : "value"}>{money(remainingBudget)}</span></div>

              <div className="progress-track">
                <div className="progress-fill" style={{ width: `${usedPercent}%` }} />
              </div>
              <div className="item-meta">Budget used: {usedPercent.toFixed(1)}%</div>
            </>
          ) : (
            <p className="note">No summary data available.</p>
          )}
        </div>
      </div>

      <div className="details-grid">
        <div className="panel panel-pad stack animate-rise" style={{ "--delay": "210ms" }}>
          <div className="row">
            <h3 style={{ margin: 0 }}>All recent transactions</h3>
            <span className="dim">{sortedExpenses.length} entries</span>
          </div>

          {loading ? (
            <p className="note">Loading transactions...</p>
          ) : sortedExpenses.length === 0 ? (
            <p className="note">No transactions yet.</p>
          ) : (
            <div className="list list-scroll details-scroll">
              {sortedExpenses.map((expense) => (
                <div key={expense.id} className="item">
                  <div>
                    <div style={{ textTransform: "capitalize" }}>{expense.category}</div>
                    <div className="item-meta">{expense.date}</div>
                  </div>
                  <div className="value">-{money(expense.amount)}</div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="stack details-side-stack">
          <div className="panel panel-pad stack animate-rise" style={{ "--delay": "280ms" }}>
            <div className="row">
              <h3 style={{ margin: 0 }}>Savings goals</h3>
              <span className="dim">{sortedGoals.length} goals</span>
            </div>

            {loading ? (
              <p className="note">Loading goals...</p>
            ) : sortedGoals.length === 0 ? (
              <p className="note">No goals created yet.</p>
            ) : (
              sortedGoals.map((goal) => {
                const progress = Math.max(0, Math.min(100, Number(goal.progressPercentage || 0)));
                return (
                  <div key={goal.goalName} className="goal-item">
                    <div className="row">
                      <span>{goal.goalName}</span>
                      <span className="value">{progress.toFixed(0)}%</span>
                    </div>
                    <div className="progress-track">
                      <div className="progress-fill" style={{ width: `${progress}%` }} />
                    </div>
                    <div className="item-meta">
                      {money(goal.currentAmount)} saved of {money(goal.targetAmount)}
                    </div>
                  </div>
                );
              })
            )}
          </div>

          <div className="panel panel-pad stack animate-rise" style={{ "--delay": "350ms" }}>
            <div className="row">
              <h3 style={{ margin: 0 }}>AI insights</h3>
              <span className="dim">Rule-based engine</span>
            </div>

            {loading ? (
              <p className="note">Loading insights...</p>
            ) : insights.length === 0 ? (
              <p className="note">No insights generated yet for this month.</p>
            ) : (
              <div className="insight-list">
                {insights.map((insight, index) => (
                  <div
                    key={`${insight}-${index}`}
                    className={`insight-card ${index % 2 === 0 ? "tone-blue" : "tone-neon"}`}
                  >
                    {insight}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
