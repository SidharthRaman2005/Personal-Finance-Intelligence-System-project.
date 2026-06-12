import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function AddExpense() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  const [expenses, setExpenses] = useState([
    { amount: "", category: "", date: "" },
  ]);

  const addRow = () => {
    setExpenses([...expenses, { amount: "", category: "", date: "" }]);
  };

  const removeRow = (index) => {
    if (expenses.length === 1) {
      return;
    }

    setExpenses((prev) => prev.filter((_, i) => i !== index));
  };

  const handleChange = (index, field, value) => {
    const updated = [...expenses];
    updated[index][field] = value;
    setExpenses(updated);
  };

  const handleSubmit = async () => {
    const username = localStorage.getItem("username");
    if (!username) {
      navigate("/", { replace: true });
      return;
    }

    for (const exp of expenses) {
      if (!exp.amount || !exp.category || !exp.date) {
        alert("Please fill all fields in every row");
        return;
      }

      if (Number(exp.amount) <= 0) {
        alert("Amount must be greater than 0");
        return;
      }
    }

    try {
      setSubmitting(true);
      const requests = expenses.map((exp) =>
        fetch("http://localhost:8080/api/expenses/add", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            amount: Number(exp.amount),
            category: exp.category,
            date: exp.date,
          }),
        }).then((res) => res.json())
      );

      const results = await Promise.all(requests);
      const firstFailure = results.find((result) => !result.success);

      if (firstFailure) {
        alert(firstFailure.message || "Some rows could not be saved");
        return;
      }

      alert("All expenses added successfully");
      navigate("/dashboard");
    } catch (err) {
      console.error(err);
      alert("Failed to add expenses");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Transactions</p>
          <h1 className="title">Add expenses</h1>
          <p className="subtitle">Add multiple rows in one go for faster bookkeeping.</p>
        </div>
        <button onClick={() => navigate("/dashboard")} className="btn btn-ghost">
          Back
        </button>
      </div>

      <div className="panel panel-pad stack">
        <div className="row">
          <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Manual entries</h2>
          <span className="dim">{expenses.length} row(s)</span>
        </div>

        <div className="form-table">
        {expenses.map((exp, index) => (
          <div key={index} className="form-row">
            <input
              type="number"
              placeholder="Amount"
              className="field"
              value={exp.amount}
              onChange={(e) => handleChange(index, "amount", e.target.value)}
            />

            <input
              type="text"
              placeholder="Category"
              className="field"
              value={exp.category}
              onChange={(e) => handleChange(index, "category", e.target.value)}
            />

            <input
              type="date"
              className="field"
              value={exp.date}
              onChange={(e) => handleChange(index, "date", e.target.value)}
            />

            <button onClick={() => removeRow(index)} className="btn btn-danger" type="button">
              Remove
            </button>
          </div>
        ))}
        </div>

        <div className="actions-wrap">
          <button onClick={addRow} className="btn btn-secondary" type="button">
            Add Row
          </button>

          <button onClick={handleSubmit} className="btn btn-primary" disabled={submitting} type="button">
            {submitting ? "Saving..." : "Submit Expenses"}
          </button>
        </div>
      </div>
    </div>
  );
}