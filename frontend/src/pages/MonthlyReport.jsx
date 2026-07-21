import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import html2canvas from "html2canvas";
import { jsPDF } from "jspdf";

const COLORS = ["#64f58a", "#6dd6ff", "#fbd56a", "#f48fb1", "#7f8cff", "#ffab70", "#9be7ff"];

const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

export default function MonthlyReport() {
  const navigate = useNavigate();
  const reportRef = useRef(null);

  const today = new Date();
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    const loadReport = async () => {
      try {
        setLoading(true);
        setError("");

        const token = localStorage.getItem("token");
        const headers = token ? { Authorization: `Bearer ${token}` } : undefined;

        const response = await fetch(
          `http://localhost:8080/api/reports/monthly?month=${month}&year=${year}`,
          { signal: controller.signal, headers }
        );

        let payload = null;
        try {
          payload = await response.json();
        } catch (parseError) {
          payload = null;
        }

        if (!response.ok) {
          const fallback = `Request failed (${response.status})`;
          const message = payload?.message || payload?.error || fallback;
          throw new Error(message);
        }
        if (payload?.success === false) {
          throw new Error(payload.message || "Unable to load monthly report");
        }

        setReport(payload?.data || null);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(err.message || "Unable to load monthly report");
        }
      } finally {
        setLoading(false);
      }
    };

    loadReport();

    return () => controller.abort();
  }, [month, year]);

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const categoryData = report?.categoryBreakdown || [];
  const monthlyTrend = report?.monthlyTrend || [];
  const investmentAllocation = useMemo(() => {
    const allocation = report?.investmentSummary?.allocationPercentageByType || {};
    return Object.entries(allocation).map(([key, value]) => ({
      name: key,
      value: Number(value || 0),
    }));
  }, [report]);

  const handleDownload = async () => {
    if (!reportRef.current || exporting) {
      return;
    }

    try {
      setExporting(true);
      const canvas = await html2canvas(reportRef.current, {
        scale: 2,
        backgroundColor: "#0b0f12",
      });
      const imgData = canvas.toDataURL("image/png");
      const pdf = new jsPDF("p", "pt", "a4");
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const imgProps = pdf.getImageProperties(imgData);
      const pdfHeight = (imgProps.height * pageWidth) / imgProps.width;
      let heightLeft = pdfHeight;
      let position = 0;

      while (heightLeft > 0) {
        pdf.addImage(imgData, "PNG", 0, position, pageWidth, pdfHeight);
        heightLeft -= pageHeight;
        position -= pageHeight;
        if (heightLeft > 0) {
          pdf.addPage();
        }
      }

      pdf.save(`monthly-report-${year}-${month}.pdf`);
    } finally {
      setExporting(false);
    }
  };

  const topCategories = categoryData.slice(0, 3);
  const spendingTrend = report?.spendingTrend;

  return (
    <div className="page">
      <div className="page-head report-head">
        <div>
          <p className="eyebrow">Report</p>
          <h1 className="title">Monthly financial report</h1>
          <p className="subtitle">A complete view of your monthly cash flow, goals, and investment health.</p>
        </div>
        <div className="report-actions">
          <button className="btn btn-ghost" type="button" onClick={() => navigate("/dashboard")}>
            Back to Dashboard
          </button>
          <button className="btn btn-secondary" type="button" onClick={handleDownload} disabled={exporting}>
            {exporting ? "Preparing PDF..." : "Download PDF"}
          </button>
        </div>
      </div>

      <div className="panel panel-pad report-filters">
        <div className="row" style={{ gap: "0.8rem", flexWrap: "wrap" }}>
          <div className="stack" style={{ gap: "0.35rem" }}>
            <span className="dim">Month</span>
            <select className="field" value={month} onChange={(event) => setMonth(Number(event.target.value))}>
              {MONTHS.map((label, index) => (
                <option key={label} value={index + 1}>{label}</option>
              ))}
            </select>
          </div>
          <div className="stack" style={{ gap: "0.35rem" }}>
            <span className="dim">Year</span>
            <input
              className="field"
              type="number"
              value={year}
              onChange={(event) => setYear(Number(event.target.value))}
            />
          </div>
          {spendingTrend ? (
            <div className="report-trend">
              <div className="item-meta">Spending trend</div>
              <div className="value">
                {spendingTrend.direction === "up" ? "▲" : spendingTrend.direction === "down" ? "▼" : "■"} {Math.abs(
                  Number(spendingTrend.percentChange || 0)
                ).toFixed(0)}% vs last month
              </div>
            </div>
          ) : null}
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}

      <div ref={reportRef} className="report-shell">
        {loading ? (
          <div className="panel panel-pad note">Loading report...</div>
        ) : report ? (
          <>
            <div className="report-grid">
              <div className="panel panel-pad report-card tone-blue">
                <div className="item-meta">Total income</div>
                <div className="value">{money(report.totalIncome)}</div>
              </div>
              <div className="panel panel-pad report-card tone-neon">
                <div className="item-meta">Total expense</div>
                <div className="value">{money(report.totalExpense)}</div>
              </div>
              <div className="panel panel-pad report-card tone-blue">
                <div className="item-meta">Total savings</div>
                <div className="value">{money(report.totalSavings)}</div>
              </div>
              <div className="panel panel-pad report-card tone-neon">
                <div className="item-meta">Savings rate</div>
                <div className="value">{Number(report.savingsPercentage || 0).toFixed(1)}%</div>
              </div>
              <div className="panel panel-pad report-card tone-blue">
                <div className="item-meta">Budget usage</div>
                <div className="value">{Number(report.budgetUsagePercentage || 0).toFixed(0)}%</div>
                <div className="item-meta">Remaining {money(report.budgetRemaining)}</div>
              </div>
              <div className="panel panel-pad report-card tone-neon">
                <div className="item-meta">Health score</div>
                <div className="value">{report.healthScore?.score ?? 0}/100</div>
                <div className="item-meta">{report.healthScore?.status}</div>
              </div>
            </div>

            <div className="report-split">
              <div className="panel panel-pad report-section">
                <div className="row">
                  <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Expense categories</h2>
                  <span className="dim">{topCategories.length} top categories</span>
                </div>

                {categoryData.length === 0 ? (
                  <p className="note">No expenses recorded for this month.</p>
                ) : (
                  <div className="report-chart" style={{ height: 240 }}>
                    <ResponsiveContainer>
                      <PieChart>
                        <Pie data={categoryData} dataKey="totalAmount" nameKey="category" outerRadius={90}>
                          {categoryData.map((entry, index) => (
                            <Cell key={entry.category} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip formatter={(value) => money(value)} />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                )}

                <div className="report-badges">
                  {topCategories.map((item) => (
                    <span key={item.category} className="report-badge">
                      {item.category}: {money(item.totalAmount)}
                    </span>
                  ))}
                </div>
              </div>

              <div className="panel panel-pad report-section">
                <div className="row">
                  <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Income vs expense</h2>
                  <span className="dim">Last 6 months</span>
                </div>
                <div className="report-chart">
                  <ResponsiveContainer>
                    <LineChart data={monthlyTrend} margin={{ top: 10, right: 12, left: 0, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.08)" />
                      <XAxis dataKey="label" stroke="#94a3b8" />
                      <YAxis stroke="#94a3b8" />
                      <Tooltip formatter={(value) => money(value)} />
                      <Legend />
                      <Line type="monotone" dataKey="income" stroke="#64f58a" strokeWidth={2} />
                      <Line type="monotone" dataKey="expense" stroke="#f48fb1" strokeWidth={2} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>

            <div className="report-split">
              <div className="panel panel-pad report-section">
                <div className="row">
                  <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Monthly expense trend</h2>
                  <span className="dim">Last 6 months</span>
                </div>
                <div className="report-chart">
                  <ResponsiveContainer>
                    <BarChart data={monthlyTrend} margin={{ top: 10, right: 12, left: 0, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.08)" />
                      <XAxis dataKey="label" stroke="#94a3b8" />
                      <YAxis stroke="#94a3b8" />
                      <Tooltip formatter={(value) => money(value)} />
                      <Bar dataKey="expense" fill="#6dd6ff" radius={[6, 6, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="panel panel-pad report-section">
                <div className="row">
                  <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Investment allocation</h2>
                  <span className="dim">Portfolio view</span>
                </div>
                {investmentAllocation.length === 0 ? (
                  <p className="note">No investments recorded yet.</p>
                ) : (
                  <div className="report-chart" style={{ height: 240 }}>
                    <ResponsiveContainer>
                      <PieChart>
                        <Pie data={investmentAllocation} dataKey="value" nameKey="name" outerRadius={90}>
                          {investmentAllocation.map((entry, index) => (
                            <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip formatter={(value) => `${Number(value || 0).toFixed(1)}%`} />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                )}
                <div className="report-badges">
                  <span className="report-badge">Invested {money(report.investmentSummary?.totalInvested)}</span>
                  <span className="report-badge">Current {money(report.investmentSummary?.totalCurrentValue)}</span>
                  <span className="report-badge">Diversification {Number(report.investmentSummary?.diversificationScore || 0).toFixed(0)}%</span>
                </div>
              </div>
            </div>

            <div className="report-split">
              <div className="panel panel-pad report-section">
                <div className="row">
                  <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Monthly insights</h2>
                  <span className="dim">Smart signals</span>
                </div>
                {report.insights?.length ? (
                  <div className="report-list">
                    {report.insights.map((item, index) => (
                      <div
                        key={`${item}-${index}`}
                        className={`insight-card ${index % 2 === 0 ? "tone-blue" : "tone-neon"}`}
                      >
                        {item}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="note">No insights yet.</p>
                )}
              </div>

              <div className="panel panel-pad report-section">
                <div className="row">
                  <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Smart warnings</h2>
                  <span className="dim">Stay on track</span>
                </div>
                {report.warnings?.length ? (
                  <div className="report-list">
                    {report.warnings.map((item, index) => (
                      <div key={`${item}-${index}`} className="warning-card">{item}</div>
                    ))}
                  </div>
                ) : (
                  <p className="note">No warnings generated.</p>
                )}
              </div>
            </div>

            <div className="panel panel-pad report-section">
              <div className="row">
                <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Financial suggestions</h2>
                <span className="dim">Next actions</span>
              </div>
              {report.suggestions?.length ? (
                <div className="report-list">
                  {report.suggestions.map((item, index) => (
                    <div key={`${item}-${index}`} className="suggestion-card">{item}</div>
                  ))}
                </div>
              ) : (
                <p className="note">No suggestions available.</p>
              )}
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}
