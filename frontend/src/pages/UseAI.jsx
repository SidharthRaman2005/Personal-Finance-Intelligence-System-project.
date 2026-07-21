import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const CHATGPT_URL = "https://chatgpt.com/";

export default function UseAI() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [copying, setCopying] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [payload, setPayload] = useState(null);

  useEffect(() => {
    const username = localStorage.getItem("username");

    if (!username) {
      navigate("/");
      return;
    }

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

    const loadPayload = async () => {
      try {
        setLoading(true);
        setError("");

        const [summaryJson, expenseJson, incomeJson, goalsJson, investmentJson, healthJson, insightJson, analysisJson] =
          await Promise.all([
            fetchJson("http://localhost:8080/api/dashboard"),
            fetchJson("http://localhost:8080/api/expenses/user", true),
            fetchJson("http://localhost:8080/api/income/user", true),
            fetchJson("http://localhost:8080/api/goals", true),
            fetchJson("http://localhost:8080/api/investments/summary", true),
            fetchJson("http://localhost:8080/api/insights/health-score/overall", true),
            fetchJson("http://localhost:8080/api/insights/overall", true),
            fetchJson("http://localhost:8080/api/analysis/overview?threshold=2000", true),
          ]);

        const nextPayload = buildPrompt({
          username,
          summary: summaryJson || {},
          expenses: Array.isArray(expenseJson?.data) ? expenseJson.data : [],
          income: Array.isArray(incomeJson?.data) ? incomeJson.data : [],
          goals: Array.isArray(goalsJson?.data) ? goalsJson.data : [],
          investmentSummary: investmentJson?.data || null,
          health: healthJson?.data || null,
          insights: Array.isArray(insightJson?.data?.insights) ? insightJson.data.insights : [],
          analysis: analysisJson?.data || null,
        });

  setPayload(nextPayload);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to prepare your AI brief right now.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadPayload();

    return () => controller.abort();
  }, [navigate]);

  const previewText = useMemo(() => payload || "", [payload]);

  const copyBrief = async () => {
    if (!payload) {
      setError("AI brief is not ready yet.");
      return false;
    }

    try {
      setCopying(true);
      setError("");
      setSuccessMessage("");
      await navigator.clipboard.writeText(payload);
      setSuccessMessage("Finance brief copied to clipboard.");
      return true;
    } catch (err) {
      setError("Could not copy to clipboard. Please copy the text manually.");
      return false;
    } finally {
      setCopying(false);
    }
  };

  const openChatGPT = () => {
    const chatWindow = window.open(CHATGPT_URL, "_blank", "noopener,noreferrer");
    if (!chatWindow) {
      setError("ChatGPT could not be opened. Please allow popups and try again.");
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">AI assist</p>
          <h1 className="title">Use AI</h1>
          <p className="subtitle">
            Copy a clean finance brief, then continue in ChatGPT for budgeting, planning, or follow-up questions.
          </p>
        </div>
        <div className="actions-wrap">
          <button className="btn btn-ghost" type="button" onClick={() => navigate("/dashboard")}>
            Back
          </button>
          <button className="btn btn-secondary" type="button" onClick={copyBrief} disabled={loading || copying}>
            {copying ? "Copying..." : "Copy brief"}
          </button>
          <button className="btn btn-primary" type="button" onClick={openChatGPT} disabled={loading || copying}>
            Open ChatGPT
          </button>
        </div>
      </div>

      <div className="panel panel-pad note" style={{ marginBottom: "1rem" }}>
        Powered by ChatGPT. The copied brief excludes passwords, tokens, and account credentials.
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}
      {successMessage ? <div className="panel panel-pad note">{successMessage}</div> : null}

      <div className="panel panel-pad stack">
        <div className="row">
          <h2 style={{ margin: 0, fontSize: "1.1rem" }}>What will be copied</h2>
          <span className="dim">Safe finance context</span>
        </div>
        <p className="note" style={{ marginTop: 0 }}>
          This brief is designed to give ChatGPT enough context to answer finance questions without exposing
          sensitive data.
        </p>

        <div className="stack" style={{ gap: "0.5rem" }}>
          <div className="item-meta">User: display name only</div>
          <div className="item-meta">Summary: income, expense, net balance, budget, budget usage</div>
          <div className="item-meta">Transactions: recent expenses and income with dates and amounts</div>
          <div className="item-meta">Goals: target, current progress, target date, and completion percentage</div>
          <div className="item-meta">Investments: totals, profit/loss, diversification, allocation by type</div>
          <div className="item-meta">Insights: health score, savings rate, and analysis signals</div>
        </div>
      </div>

      <div className="panel panel-pad stack" style={{ marginTop: "1rem" }}>
        <div className="row">
          <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Clipboard preview</h2>
          <span className="dim">Editable copy target</span>
        </div>
        {loading ? (
          <p className="note">Preparing your finance brief...</p>
        ) : (
          <textarea
            className="field"
            rows="22"
            readOnly
            value={previewText}
            style={{ fontFamily: "inherit", lineHeight: 1.6 }}
          />
        )}
      </div>
    </div>
  );
}

function buildPrompt({
  username,
  summary,
  expenses,
  income,
  goals,
  investmentSummary,
  health,
  insights,
  analysis,
}) {
  const recentExpenses = expenses
    .slice()
    .sort((left, right) => new Date(right.date) - new Date(left.date))
    .slice(0, 5)
    .map((item) => `- ${item.date}: ${item.category || item.title || "Expense"} - ₹${Number(item.amount || 0).toLocaleString("en-IN")}`)
    .join("\n");

  const recentIncome = income
    .slice()
    .sort((left, right) => new Date(right.date) - new Date(left.date))
    .slice(0, 5)
    .map((item) => `- ${item.date}: ${item.source || "Income"} - ₹${Number(item.amount || 0).toLocaleString("en-IN")}`)
    .join("\n");

  const goalLines = goals
    .slice(0, 5)
    .map((goal) => `- ${goal.goalName}: ₹${Number(goal.currentAmount || 0).toLocaleString("en-IN")} saved of ₹${Number(goal.targetAmount || 0).toLocaleString("en-IN")} (${Number(goal.progressPercentage || 0).toFixed(0)}%)`)
    .join("\n");

  const investmentLines = investmentSummary?.allocationPercentageByType
    ? Object.entries(investmentSummary.allocationPercentageByType)
        .map(([type, percent]) => `- ${type}: ${Number(percent || 0).toFixed(0)}%`)
        .join("\n")
    : "- No allocation data yet";

  const analysisLines = analysis?.financialTwinSimulator
    ? [
        `- Current path 1 year: ₹${Number(analysis.financialTwinSimulator.currentPath?.oneYearSavings || 0).toLocaleString("en-IN")}`,
        `- Current path 3 years: ₹${Number(analysis.financialTwinSimulator.currentPath?.threeYearSavings || 0).toLocaleString("en-IN")}`,
        `- Current path 5 years: ₹${Number(analysis.financialTwinSimulator.currentPath?.fiveYearSavings || 0).toLocaleString("en-IN")}`,
        `- Optimized path 1 year: ₹${Number(analysis.financialTwinSimulator.optimizedPath?.oneYearSavings || 0).toLocaleString("en-IN")}`,
        `- Optimized path 3 years: ₹${Number(analysis.financialTwinSimulator.optimizedPath?.threeYearSavings || 0).toLocaleString("en-IN")}`,
        `- Optimized path 5 years: ₹${Number(analysis.financialTwinSimulator.optimizedPath?.fiveYearSavings || 0).toLocaleString("en-IN")}`,
      ].join("\n")
    : "- No long-term projection data yet";

  return [
    "Use this finance context to answer questions clearly and practically.",
    `User: ${username}`,
    "",
    "Current summary:",
    `- Total income: ₹${Number(summary.totalIncome || 0).toLocaleString("en-IN")}`,
    `- Total expense: ₹${Number(summary.totalExpense || 0).toLocaleString("en-IN")}`,
    `- Net balance: ₹${Number(summary.netBalance || 0).toLocaleString("en-IN")}`,
    `- Monthly budget: ${summary.monthlyBudget === null || summary.monthlyBudget === undefined ? "not set" : `₹${Number(summary.monthlyBudget || 0).toLocaleString("en-IN")}`}`,
    `- Remaining budget: ${summary.remainingBudget === null || summary.remainingBudget === undefined ? "not set" : `₹${Number(summary.remainingBudget || 0).toLocaleString("en-IN")}`}`,
    `- Budget usage: ${Number(summary.percentageUsed || 0).toFixed(0)}%`,
    "",
    "Recent expenses:",
    recentExpenses || "- No recent expenses",
    "",
    "Recent income:",
    recentIncome || "- No recent income",
    "",
    "Goals:",
    goalLines || "- No goals yet",
    "",
    "Investments:",
    `- Total invested: ₹${Number(investmentSummary?.totalInvested || 0).toLocaleString("en-IN")}`,
    `- Current value: ₹${Number(investmentSummary?.totalCurrentValue || 0).toLocaleString("en-IN")}`,
    `- Profit/Loss: ₹${Number(investmentSummary?.totalProfitLoss || 0).toLocaleString("en-IN")}`,
    `- Diversification score: ${Number(investmentSummary?.diversificationScore || 0).toFixed(0)}%`,
    investmentLines,
    "",
    "Analysis and guidance:",
    `- Health score: ${health?.score ?? 0}/100`,
    `- Savings rate: ${Number(health?.savingsRate || 0).toFixed(1)}%`,
    insights.length ? insights.map((item) => `- ${item}`).join("\n") : "- No insights yet",
    analysisLines,
    "",
    "Please answer with concise, practical advice and call out any risky spending or savings gaps.",
    "Credits: Prepared for ChatGPT from Financeapp.",
  ]
    .filter(Boolean)
    .join("\n");
}