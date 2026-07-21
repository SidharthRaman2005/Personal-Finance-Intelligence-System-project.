import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Card from "../components/Card";

export default function Dashboard() {
  const [username, setUsername] = useState("");
  const [summary, setSummary] = useState({
    totalIncome: 0,
    totalExpense: 0,
    netBalance: 0,
    monthlyBudget: null,
    remainingBudget: null,
    percentageUsed: 0,
  });
  const [expenses, setExpenses] = useState([]);
  const [goals, setGoals] = useState([]);
  const [investmentSummary, setInvestmentSummary] = useState(null);
  const [health, setHealth] = useState(null);
  const [insights, setInsights] = useState([]);
  const [budgetInput, setBudgetInput] = useState("");
  const [budgetSaving, setBudgetSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [refreshTick, setRefreshTick] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { label: "Dashboard", path: "/dashboard" },
    { label: "Transactions", path: "/expenses" },
    { label: "Analysis", path: "/analysis" },
    { label: "Investments", path: "/investments" },
    { label: "Goals", path: "/goals" },
    { label: "Monthly report", path: "/reports/monthly" },
    { label: "Use AI", path: "/use-ai" },
    { label: "Add Expense", path: "/add-expense" },
    { label: "Add Income", path: "/add-income" },
    { label: "Upload CSV", path: "/upload-csv" },
  ];

  useEffect(() => {
    const user = localStorage.getItem("username");

    if (!user) {
      navigate("/");
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

    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError("");

        const [summaryJson, expenseJson, goalsJson, healthJson, insightJson, investmentJson] = await Promise.all([
          fetchJson("http://localhost:8080/api/dashboard"),
          fetchJson("http://localhost:8080/api/expenses/user"),
          fetchJson("http://localhost:8080/api/goals", true),
          fetchJson("http://localhost:8080/api/insights/health-score/overall", true),
          fetchJson("http://localhost:8080/api/insights/overall", true),
          fetchJson("http://localhost:8080/api/investments/summary", true),
        ]);

        setSummary(summaryJson || {});
        setExpenses(expenseJson.data || []);
        setGoals(Array.isArray(goalsJson?.data) ? goalsJson.data : []);
        setHealth(healthJson?.data || null);
        setInsights(Array.isArray(insightJson?.data?.insights) ? insightJson.data.insights : []);
        setInvestmentSummary(investmentJson?.data || null);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to load dashboard. Please refresh.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();

    return () => controller.abort();
  }, [navigate, refreshTick]);

  const handleSetBudget = async () => {
    const amount = Number(budgetInput);

    if (!budgetInput || !Number.isFinite(amount) || amount <= 0) {
      alert("Enter a valid budget amount greater than 0");
      return;
    }

    try {
      setBudgetSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch(
        `http://localhost:8080/api/user/budget?budget=${amount}`,
        { method: "POST" }
      );

      if (!response.ok) {
        throw new Error("Failed to set budget");
      }

      const data = await response.json();
      if (data.success === false) {
        throw new Error(data.message || "Failed to set budget");
      }

      setBudgetInput("");
      setSuccessMessage("Monthly budget saved.");
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError("Unable to save budget. Please try again.");
    } finally {
      setBudgetSaving(false);
    }
  };

  const recentExpenses = useMemo(
    () =>
      [...expenses]
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .slice(0, 3),
    [expenses]
  );

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const hasBudget = summary.monthlyBudget !== null && summary.monthlyBudget !== undefined;
  const monthlyBudget = Number(summary.monthlyBudget || 0);
  const remainingBudget = Number(summary.remainingBudget || 0);
  const spentBudget = monthlyBudget - remainingBudget;
  const isOverspent = hasBudget && remainingBudget < 0;
  const budgetUsed = Number(summary.percentageUsed || 0);
  const budgetUsedClamped = Math.max(0, Math.min(100, budgetUsed));
  const goalCount = goals.length;
  const investedTotal = Number(investmentSummary?.totalInvested || 0);
  const currentValue = Number(investmentSummary?.totalCurrentValue || 0);
  const profitLoss = Number(investmentSummary?.totalProfitLoss || 0);
  const diversification = Number(investmentSummary?.diversificationScore || 0);
  const profitLabel = `${profitLoss >= 0 ? "+" : "-"}${money(Math.abs(profitLoss))}`;
  const healthScore = Math.round(Number(health?.score || 0));
  const savingsRate = health ? Number(health.savingsRate || 0).toFixed(1) : null;

  return (
    <div className="page">
      <div className="dashboard-nav animate-rise" style={{ "--delay": "0ms" }}>
        <div>
          <div className="nav-brand">Financeapp</div>
          <div className="item-meta">Unified finance workspace</div>
        </div>
        <div className="nav-links">
          {navItems.map((item) => (
            <button
              key={item.path}
              className={`nav-link${location.pathname === item.path ? " is-active" : ""}`}
              type="button"
              onClick={() => navigate(item.path)}
              aria-current={location.pathname === item.path ? "page" : undefined}
            >
              {item.label}
            </button>
          ))}
          <button
            className="nav-link nav-link-danger"
            type="button"
            onClick={() => {
              localStorage.removeItem("token");
              localStorage.removeItem("tokenType");
              localStorage.removeItem("tokenExpiresAt");
              localStorage.removeItem("username");
              navigate("/", { replace: true });
            }}
          >
            Logout
          </button>
        </div>
      </div>

      <div className="page-head dashboard-hero animate-rise" style={{ "--delay": "0ms" }}>
        <div>
          <p className="eyebrow">Workspace</p>
          <h1 className="title">Financial performance hub</h1>
          <p className="subtitle">
            Welcome, <span className="value">{username}</span>. Keep your overall financial rhythm strong.
          </p>
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}
      {successMessage ? <div className="panel panel-pad note">{successMessage}</div> : null}

      <div className="metrics-grid">
        <div className="animate-rise" style={{ "--delay": "70ms" }}>
          <Card title="Income" value={money(summary.totalIncome)} color="tone-blue" />
        </div>
        <div className="animate-rise" style={{ "--delay": "140ms" }}>
          <Card title="Expense" value={money(summary.totalExpense)} color="tone-neon" />
        </div>
        <div className="animate-rise" style={{ "--delay": "210ms" }}>
          <Card title="Balance" value={money(summary.netBalance)} color="tone-blue" />
        </div>
        <div className="animate-rise" style={{ "--delay": "280ms" }}>
          <Card title="Health Score" value={health ? `${healthScore}/100` : "No data"} color="tone-neon" />
        </div>
        <div className="animate-rise" style={{ "--delay": "350ms" }}>
          <Card title="Savings Rate" value={health ? `${savingsRate}%` : "No data"} color="tone-blue" />
        </div>
      </div>

      <div className="dashboard-split">
        <div className="panel panel-pad stack animate-rise" style={{ "--delay": "260ms" }}>
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.25rem" }}>Monthly budget pulse</h2>
            <span className="dim">Live control</span>
          </div>

          {hasBudget ? (
            <div className="budget-ring-wrap">
              <div
                className="budget-ring"
                style={{
                  background: `conic-gradient(var(--accent) ${budgetUsedClamped}%, rgba(255,255,255,0.12) ${budgetUsedClamped}% 100%)`,
                }}
              >
                <div className="budget-ring-inner">
                  <div className="budget-ring-value">{budgetUsed.toFixed(0)}%</div>
                  <div className="dim">Used</div>
                </div>
              </div>

              <div className="stack" style={{ gap: "0.45rem", width: "100%" }}>
                <div className="row">
                  <span className="dim">Budget</span>
                  <span>{money(monthlyBudget)}</span>
                </div>
                <div className="row">
                  <span className="dim">Spent</span>
                  <span className="value">{money(spentBudget)}</span>
                </div>
                <div className="row">
                  <span className="dim">Remaining</span>
                  <span className={isOverspent ? "text-danger" : "value"}>
                    {money(remainingBudget)}
                  </span>
                </div>
              </div>
            </div>
          ) : (
            <p className="note">Set a monthly budget to activate your spending ring.</p>
          )}

          <div className="mini-form">
            <input
              type="number"
              className="field"
              placeholder="Set monthly budget"
              value={budgetInput}
              onChange={(event) => setBudgetInput(event.target.value)}
            />
            <button
              className="btn btn-neon"
              type="button"
              onClick={handleSetBudget}
              disabled={budgetSaving}
            >
              {budgetSaving ? "Saving..." : "Save Budget"}
            </button>
          </div>
        </div>

        <div className="panel panel-pad stack animate-rise" style={{ "--delay": "320ms" }}>
          <div className="row">
            <h3 style={{ margin: 0 }}>Scoreboard</h3>
            <span className="dim">Overall</span>
          </div>

          {health ? (
            <>
              <div className="health-banner">
                <div className="health-score">{healthScore}</div>
                <div>
                  <div className="value">{health.status}</div>
                  <div className="item-meta">Savings rate {Number(health.savingsRate || 0).toFixed(1)}%</div>
                </div>
              </div>
              <p className="note" style={{ margin: 0 }}>{health.message}</p>
            </>
          ) : (
            <p className="note">No score yet. Add income and expenses to see the overall score.</p>
          )}

          <div className="quick-stats-grid">
            <div className="quick-stat-card tone-neon">
              <div className="item-meta">Goals</div>
              <div className="value">{goalCount}</div>
            </div>
            <div className="quick-stat-card tone-blue">
              <div className="item-meta">Insights</div>
              <div className="value">{insights.length}</div>
            </div>
          </div>

        </div>
      </div>

      <div className="dashboard-layout">
        <div className="dashboard-column">
          <div className="panel panel-pad stack animate-rise" style={{ "--delay": "380ms" }}>
            <div className="row">
              <h2 className="title" style={{ fontSize: "1.25rem" }}>
                Recent transactions
              </h2>
              <button className="btn btn-ghost" type="button" onClick={() => navigate("/details")}>View all</button>
            </div>

            {loading ? (
              <p className="note">Loading transactions...</p>
            ) : recentExpenses.length === 0 ? (
              <p className="note">No expenses found yet.</p>
            ) : (
              <div className="list" style={{ marginTop: "0.35rem" }}>
                {recentExpenses.map((exp) => (
                  <div key={exp.id} className="item">
                    <div>
                      <div style={{ textTransform: "capitalize" }}>{exp.category}</div>
                      <div className="item-meta">{exp.date}</div>
                    </div>
                    <div className="value">-{money(exp.amount)}</div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="panel panel-pad stack animate-rise" style={{ "--delay": "500ms" }}>
            <div className="row">
              <h3 style={{ margin: 0 }}>Quick insights</h3>
              <span className="dim">Overall signals</span>
            </div>

            {insights.length === 0 ? (
              <p className="note">No insights yet. Add more activity to surface overall trends.</p>
            ) : (
              <div className="insight-list">
                {insights.slice(0, 2).map((insight, index) => (
                  <div
                    key={`${insight}-${index}`}
                    className={`insight-card ${index % 2 === 0 ? "tone-neon" : "tone-blue"}`}
                  >
                    {insight}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="dashboard-column">
          <div className="panel panel-pad stack animate-rise" style={{ "--delay": "470ms" }}>
            <div className="row">
              <h3 style={{ margin: 0 }}>Fund your goals</h3>
              <span className="dim">Increase progress</span>
            </div>

            {goals.length === 0 ? (
              <p className="note">Create a goal first to track its progress here.</p>
            ) : (
              goals.map((goal) => {
                const progress = Math.max(0, Math.min(100, Number(goal.progressPercentage || 0)));

                return (
                  <div className="goal-item" key={goal.id || goal.goalName}>
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

          <div className="panel panel-pad stack animate-rise" style={{ "--delay": "485ms" }}>
            <div className="row">
              <h3 style={{ margin: 0 }}>Investment pulse</h3>
              <span className="dim">Portfolio snapshot</span>
            </div>

            {investmentSummary ? (
              <>
                <div className="row">
                  <span className="dim">Invested</span>
                  <span>{money(investedTotal)}</span>
                </div>
                <div className="row">
                  <span className="dim">Current value</span>
                  <span className="value">{money(currentValue)}</span>
                </div>
                <div className="row">
                  <span className="dim">Profit / Loss</span>
                  <span className={profitLoss < 0 ? "text-danger" : "value"}>{profitLabel}</span>
                </div>
                <div className="row">
                  <span className="dim">Diversification</span>
                  <span className="value">{diversification.toFixed(0)}%</span>
                </div>
              </>
            ) : (
              <p className="note">No investments tracked yet. Add your first investment.</p>
            )}
          </div>

        </div>
      </div>
    </div>
  );
}

