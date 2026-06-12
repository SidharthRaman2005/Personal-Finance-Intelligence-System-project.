import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [income, setIncome] = useState([]);
  const [activeTab, setActiveTab] = useState("expenses");
  const [editingExpenseId, setEditingExpenseId] = useState(null);
  const [editingIncomeId, setEditingIncomeId] = useState(null);
  const [expenseForm, setExpenseForm] = useState({
    title: "",
    description: "",
    amount: "",
    category: "",
    date: "",
  });
  const [incomeForm, setIncomeForm] = useState({
    source: "",
    amount: "",
    date: "",
  });
  const [rowActionLoadingId, setRowActionLoadingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const username = localStorage.getItem("username");

    if (!username) {
      navigate("/");
      return;
    }

    const controller = new AbortController();

    const loadData = async () => {
      try {
        setLoading(true);
        setError("");

        const [expenseRes, incomeRes] = await Promise.all([
          fetch("http://localhost:8080/api/expenses/user", {
            signal: controller.signal,
          }),
          fetch("http://localhost:8080/api/income/user", {
            signal: controller.signal,
          }),
        ]);

        if (!expenseRes.ok || !incomeRes.ok) {
          throw new Error("Unable to fetch ledger data");
        }

        const expenseJson = await expenseRes.json();
        const incomeJson = await incomeRes.json();

        setExpenses(expenseJson.data || []);
        setIncome(incomeJson.data || []);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to fetch expenses or income right now.");
        }
      } finally {
        setLoading(false);
      }
    };

    loadData();

    return () => controller.abort();
  }, [navigate]);

  const sortedExpenses = useMemo(
    () => [...expenses].sort((a, b) => new Date(b.date) - new Date(a.date)),
    [expenses]
  );

  const sortedIncome = useMemo(
    () => [...income].sort((a, b) => new Date(b.date) - new Date(a.date)),
    [income]
  );

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const startExpenseEdit = (exp) => {
    setEditingExpenseId(exp.id);
    setExpenseForm({
      title: exp.title || "",
      description: exp.description || "",
      amount: String(exp.amount ?? ""),
      category: exp.category || "",
      date: exp.date || "",
    });
  };

  const cancelExpenseEdit = () => {
    setEditingExpenseId(null);
    setExpenseForm({
      title: "",
      description: "",
      amount: "",
      category: "",
      date: "",
    });
  };

  const startIncomeEdit = (entry) => {
    setEditingIncomeId(entry.id);
    setIncomeForm({
      source: entry.source || "",
      amount: String(entry.amount ?? ""),
      date: entry.date || "",
    });
  };

  const cancelIncomeEdit = () => {
    setEditingIncomeId(null);
    setIncomeForm({ source: "", amount: "", date: "" });
  };

  const handleDeleteExpense = async (id) => {
    if (!window.confirm("Delete this expense?")) {
      return;
    }

    try {
      setRowActionLoadingId(`expense-delete-${id}`);
      setError("");
      setSuccessMessage("");

      const res = await fetch(`http://localhost:8080/api/expenses/${id}`, {
        method: "DELETE",
      });

      if (!res.ok) {
        throw new Error("Failed to delete expense");
      }

      setExpenses((prev) => prev.filter((item) => item.id !== id));
      setSuccessMessage("Expense deleted.");
    } catch (err) {
      setError("Could not delete expense.");
    } finally {
      setRowActionLoadingId(null);
    }
  };

  const handleDeleteIncome = async (id) => {
    if (!window.confirm("Delete this income entry?")) {
      return;
    }

    try {
      setRowActionLoadingId(`income-delete-${id}`);
      setError("");
      setSuccessMessage("");

      const res = await fetch(`http://localhost:8080/api/income/${id}`, {
        method: "DELETE",
      });

      if (!res.ok) {
        throw new Error("Failed to delete income");
      }

      setIncome((prev) => prev.filter((item) => item.id !== id));
      setSuccessMessage("Income deleted.");
    } catch (err) {
      setError("Could not delete income.");
    } finally {
      setRowActionLoadingId(null);
    }
  };

  const handleSaveExpense = async (id) => {
    const numericAmount = Number(expenseForm.amount);

    if (!expenseForm.category.trim() || !expenseForm.date || !Number.isFinite(numericAmount) || numericAmount <= 0) {
      setError("Expense update needs category, date, and amount > 0.");
      return;
    }

    try {
      setRowActionLoadingId(`expense-save-${id}`);
      setError("");
      setSuccessMessage("");

      const res = await fetch(`http://localhost:8080/api/expenses/${id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          title: expenseForm.title || null,
          description: expenseForm.description || null,
          amount: numericAmount,
          category: expenseForm.category.trim(),
          date: expenseForm.date,
        }),
      });

      if (!res.ok) {
        throw new Error("Failed to update expense");
      }

      const payload = await res.json();
      const updated = payload.data;

      setExpenses((prev) => prev.map((item) => (item.id === id ? updated : item)));
      cancelExpenseEdit();
      setSuccessMessage("Expense updated.");
    } catch (err) {
      setError("Could not update expense.");
    } finally {
      setRowActionLoadingId(null);
    }
  };

  const handleSaveIncome = async (id) => {
    const numericAmount = Number(incomeForm.amount);

    if (!incomeForm.source.trim() || !incomeForm.date || !Number.isFinite(numericAmount) || numericAmount <= 0) {
      setError("Income update needs source, date, and amount > 0.");
      return;
    }

    try {
      setRowActionLoadingId(`income-save-${id}`);
      setError("");
      setSuccessMessage("");

      const res = await fetch(`http://localhost:8080/api/income/${id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          source: incomeForm.source.trim(),
          amount: numericAmount,
          date: incomeForm.date,
        }),
      });

      if (!res.ok) {
        throw new Error("Failed to update income");
      }

      const payload = await res.json();
      const updated = payload.data;

      setIncome((prev) => prev.map((item) => (item.id === id ? updated : item)));
      cancelIncomeEdit();
      setSuccessMessage("Income updated.");
    } catch (err) {
      setError("Could not update income.");
    } finally {
      setRowActionLoadingId(null);
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Ledger</p>
          <h1 className="title">Your transactions</h1>
          <p className="subtitle">Edit or delete any expense or income entry directly.</p>
        </div>
        <button onClick={() => navigate("/dashboard")} className="btn btn-ghost">
          Back
        </button>
      </div>

      <div className="actions-wrap">
        <button
          className={`btn ${activeTab === "expenses" ? "btn-primary" : "btn-ghost"}`}
          type="button"
          onClick={() => {
            setActiveTab("expenses");
            cancelIncomeEdit();
          }}
        >
          Expenses
        </button>
        <button
          className={`btn ${activeTab === "income" ? "btn-primary" : "btn-ghost"}`}
          type="button"
          onClick={() => {
            setActiveTab("income");
            cancelExpenseEdit();
          }}
        >
          Income
        </button>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}
      {successMessage ? <div className="panel panel-pad note">{successMessage}</div> : null}

      <div className="panel panel-pad">
        {loading ? (
          <p className="note">Loading rows...</p>
        ) : activeTab === "expenses" && sortedExpenses.length === 0 ? (
          <p className="note">No expenses found.</p>
        ) : activeTab === "income" && sortedIncome.length === 0 ? (
          <p className="note">No income entries found.</p>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                {activeTab === "expenses" ? (
                  <tr>
                    <th>Date</th>
                    <th>Category</th>
                    <th className="right">Amount</th>
                    <th className="right">Actions</th>
                  </tr>
                ) : (
                  <tr>
                    <th>Date</th>
                    <th>Source</th>
                    <th className="right">Amount</th>
                    <th className="right">Actions</th>
                  </tr>
                )}
              </thead>
              <tbody>
                {activeTab === "expenses"
                  ? sortedExpenses.map((exp) => {
                      const isEditing = editingExpenseId === exp.id;

                      return (
                        <tr key={exp.id}>
                          <td>
                            {isEditing ? (
                              <input
                                className="field"
                                type="date"
                                value={expenseForm.date}
                                onChange={(event) =>
                                  setExpenseForm((prev) => ({ ...prev, date: event.target.value }))
                                }
                              />
                            ) : (
                              exp.date
                            )}
                          </td>
                          <td style={{ textTransform: "capitalize" }}>
                            {isEditing ? (
                              <input
                                className="field"
                                type="text"
                                value={expenseForm.category}
                                onChange={(event) =>
                                  setExpenseForm((prev) => ({ ...prev, category: event.target.value }))
                                }
                              />
                            ) : (
                              exp.category
                            )}
                          </td>
                          <td className="right">
                            {isEditing ? (
                              <input
                                className="field"
                                type="number"
                                value={expenseForm.amount}
                                onChange={(event) =>
                                  setExpenseForm((prev) => ({ ...prev, amount: event.target.value }))
                                }
                              />
                            ) : (
                              <span className="value">-{money(exp.amount)}</span>
                            )}
                          </td>
                          <td className="right">
                            {isEditing ? (
                              <div className="actions-wrap" style={{ justifyContent: "flex-end" }}>
                                <button
                                  className="btn btn-secondary"
                                  type="button"
                                  disabled={rowActionLoadingId === `expense-save-${exp.id}`}
                                  onClick={() => handleSaveExpense(exp.id)}
                                >
                                  {rowActionLoadingId === `expense-save-${exp.id}` ? "Saving..." : "Save"}
                                </button>
                                <button className="btn btn-ghost" type="button" onClick={cancelExpenseEdit}>
                                  Cancel
                                </button>
                              </div>
                            ) : (
                              <div className="actions-wrap" style={{ justifyContent: "flex-end" }}>
                                <button className="btn btn-ghost" type="button" onClick={() => startExpenseEdit(exp)}>
                                  Modify
                                </button>
                                <button
                                  className="btn btn-danger"
                                  type="button"
                                  disabled={rowActionLoadingId === `expense-delete-${exp.id}`}
                                  onClick={() => handleDeleteExpense(exp.id)}
                                >
                                  {rowActionLoadingId === `expense-delete-${exp.id}` ? "Deleting..." : "Delete"}
                                </button>
                              </div>
                            )}
                          </td>
                        </tr>
                      );
                    })
                  : sortedIncome.map((entry) => {
                      const isEditing = editingIncomeId === entry.id;

                      return (
                        <tr key={entry.id}>
                          <td>
                            {isEditing ? (
                              <input
                                className="field"
                                type="date"
                                value={incomeForm.date}
                                onChange={(event) =>
                                  setIncomeForm((prev) => ({ ...prev, date: event.target.value }))
                                }
                              />
                            ) : (
                              entry.date
                            )}
                          </td>
                          <td>
                            {isEditing ? (
                              <input
                                className="field"
                                type="text"
                                value={incomeForm.source}
                                onChange={(event) =>
                                  setIncomeForm((prev) => ({ ...prev, source: event.target.value }))
                                }
                              />
                            ) : (
                              entry.source
                            )}
                          </td>
                          <td className="right">
                            {isEditing ? (
                              <input
                                className="field"
                                type="number"
                                value={incomeForm.amount}
                                onChange={(event) =>
                                  setIncomeForm((prev) => ({ ...prev, amount: event.target.value }))
                                }
                              />
                            ) : (
                              <span className="value">{money(entry.amount)}</span>
                            )}
                          </td>
                          <td className="right">
                            {isEditing ? (
                              <div className="actions-wrap" style={{ justifyContent: "flex-end" }}>
                                <button
                                  className="btn btn-secondary"
                                  type="button"
                                  disabled={rowActionLoadingId === `income-save-${entry.id}`}
                                  onClick={() => handleSaveIncome(entry.id)}
                                >
                                  {rowActionLoadingId === `income-save-${entry.id}` ? "Saving..." : "Save"}
                                </button>
                                <button className="btn btn-ghost" type="button" onClick={cancelIncomeEdit}>
                                  Cancel
                                </button>
                              </div>
                            ) : (
                              <div className="actions-wrap" style={{ justifyContent: "flex-end" }}>
                                <button className="btn btn-ghost" type="button" onClick={() => startIncomeEdit(entry)}>
                                  Modify
                                </button>
                                <button
                                  className="btn btn-danger"
                                  type="button"
                                  disabled={rowActionLoadingId === `income-delete-${entry.id}`}
                                  onClick={() => handleDeleteIncome(entry.id)}
                                >
                                  {rowActionLoadingId === `income-delete-${entry.id}` ? "Deleting..." : "Delete"}
                                </button>
                              </div>
                            )}
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