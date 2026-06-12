import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function AddIncome() {
  const [amount, setAmount] = useState("");
  const [source, setSource] = useState("");
  const [date, setDate] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async () => {
    const username = localStorage.getItem("username");
    if (!username) {
      navigate("/", { replace: true });
      return;
    }

    if (!amount || !source || !date) {
      alert("Please fill all fields");
      return;
    }

    if (Number(amount) <= 0) {
      alert("Amount must be greater than 0");
      return;
    }

    try {
      setLoading(true);
      const res = await fetch(
        "http://localhost:8080/api/income/add",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            amount: Number(amount),
            source: source,
            date: date,
          }),
        }
      );

      const data = await res.json();

      if (data.success) {
        alert("Income added successfully!");
        navigate("/dashboard");
      } else {
        alert(data.message);
      }
    } catch (err) {
      console.error(err);
      alert("Failed to add income");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="panel panel-pad auth-card stack">
        <p className="eyebrow">Income</p>
        <h1 className="title" style={{ fontSize: "1.8rem" }}>
          Add an income entry
        </h1>

        <input
          type="number"
          placeholder="Amount"
          className="field"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />

        <input
          type="text"
          placeholder="Source (Salary, Freelance...)"
          className="field"
          value={source}
          onChange={(e) => setSource(e.target.value)}
        />

        <input
          type="date"
          className="field"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />

        <div className="actions-wrap">
          <button onClick={() => navigate("/dashboard")} className="btn btn-ghost" type="button">
            Back
          </button>
          <button onClick={handleSubmit} className="btn btn-primary" disabled={loading} type="button">
            {loading ? "Saving..." : "Add Income"}
          </button>
        </div>
      </div>
    </div>
  );
}