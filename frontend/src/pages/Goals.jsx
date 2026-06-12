import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const emptyCreateForm = {
  goalName: "",
  targetAmount: "",
  currentAmount: "",
  targetDate: "",
};

const emptyEditForm = {
  goalName: "",
  targetAmount: "",
  currentAmount: "",
  targetDate: "",
};

export default function Goals() {
  const navigate = useNavigate();
  const [goals, setGoals] = useState([]);
  const [createForm, setCreateForm] = useState(emptyCreateForm);
  const [editForm, setEditForm] = useState(emptyEditForm);
  const [editingId, setEditingId] = useState(null);
  const [goalContributionById, setGoalContributionById] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [refreshTick, setRefreshTick] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    const fetchGoals = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await fetch("http://localhost:8080/api/goals", {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error("Failed to load goals");
        }

        const data = await response.json();
        setGoals(Array.isArray(data?.data) ? data.data : []);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError("Unable to load goals.");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchGoals();

    return () => controller.abort();
  }, [refreshTick]);

  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

  const sortedGoals = useMemo(
    () => [...goals].sort((a, b) => Number(b.progressPercentage || 0) - Number(a.progressPercentage || 0)),
    [goals]
  );

  const updateCreateField = (field, value) => {
    setCreateForm((prev) => ({ ...prev, [field]: value }));
  };

  const updateEditField = (field, value) => {
    setEditForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleCreateGoal = async () => {
    const goalName = createForm.goalName.trim();
    const targetAmount = Number(createForm.targetAmount);
    const currentAmount = createForm.currentAmount === "" ? null : Number(createForm.currentAmount);

    if (!goalName) {
      setError("Enter a goal name.");
      return;
    }

    if (!Number.isFinite(targetAmount) || targetAmount <= 0) {
      setError("Target amount must be greater than 0.");
      return;
    }

    if (currentAmount !== null && (!Number.isFinite(currentAmount) || currentAmount < 0)) {
      setError("Current amount cannot be negative.");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch("http://localhost:8080/api/goals", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          goalName,
          targetAmount,
          currentAmount,
          targetDate: createForm.targetDate || null,
        }),
      });

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to create goal");
      }

      setCreateForm(emptyCreateForm);
      setSuccessMessage("Goal created.");
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError(err.message || "Unable to create goal.");
    } finally {
      setSaving(false);
    }
  };

  const handleStartEdit = (goal) => {
    setEditingId(goal.id);
    setEditForm({
      goalName: goal.goalName || "",
      targetAmount: goal.targetAmount ?? "",
      currentAmount: goal.currentAmount ?? "",
      targetDate: goal.targetDate || "",
    });
    setError("");
    setSuccessMessage("");
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditForm(emptyEditForm);
  };

  const handleUpdateGoal = async () => {
    if (!editingId) {
      return;
    }

    const payload = {};

    if (editForm.goalName.trim()) {
      payload.goalName = editForm.goalName.trim();
    }

    if (editForm.targetAmount !== "") {
      const targetAmount = Number(editForm.targetAmount);
      if (!Number.isFinite(targetAmount) || targetAmount <= 0) {
        setError("Target amount must be greater than 0.");
        return;
      }
      payload.targetAmount = targetAmount;
    }

    if (editForm.currentAmount !== "") {
      const currentAmount = Number(editForm.currentAmount);
      if (!Number.isFinite(currentAmount) || currentAmount < 0) {
        setError("Current amount cannot be negative.");
        return;
      }
      payload.currentAmount = currentAmount;
    }

    if (editForm.targetDate) {
      payload.targetDate = editForm.targetDate;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch(`http://localhost:8080/api/goals/${editingId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to update goal");
      }

      setSuccessMessage("Goal updated.");
      setEditingId(null);
      setEditForm(emptyEditForm);
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError(err.message || "Unable to update goal.");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteGoal = async (goalId) => {
    if (!goalId || !window.confirm("Delete this goal?")) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch(`http://localhost:8080/api/goals/${goalId}`, {
        method: "DELETE",
      });

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to delete goal");
      }

      setSuccessMessage("Goal deleted.");
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError(err.message || "Unable to delete goal.");
    } finally {
      setSaving(false);
    }
  };

  const handleAddMoney = async (goalId) => {
    const amount = Number(goalContributionById[goalId] || 0);

    if (!goalId) {
      setError("Goal id is missing.");
      return;
    }

    if (!Number.isFinite(amount) || amount <= 0) {
      setError("Enter a valid amount greater than 0.");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await fetch(
        `http://localhost:8080/api/goals/${goalId}/add-money?amount=${amount}`,
        { method: "POST" }
      );

      const data = await response.json();

      if (!response.ok || data.success === false) {
        throw new Error(data.message || "Failed to add money");
      }

      setGoalContributionById((prev) => ({ ...prev, [goalId]: "" }));
      setSuccessMessage("Goal updated.");
      setRefreshTick((value) => value + 1);
    } catch (err) {
      setError(err.message || "Unable to add money to goal.");
    } finally {
      setSaving(false);
    }
  };

  const formatTargetDate = (value) => {
    if (!value) {
      return "-";
    }

    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }

    return parsed.toLocaleDateString("en-IN", {
      year: "numeric",
      month: "short",
      day: "2-digit",
    });
  };

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <p className="eyebrow">Goals</p>
          <h1 className="title">Savings goal studio</h1>
          <p className="subtitle">Set targets, track progress, and move money with intent.</p>
        </div>
        <div className="actions-wrap">
          <button className="btn btn-ghost" onClick={() => navigate("/dashboard")}>Back</button>
          <button className="btn btn-secondary" onClick={() => navigate("/investments")}>Investments</button>
        </div>
      </div>

      {error ? <div className="panel panel-pad note">{error}</div> : null}
      {successMessage ? <div className="panel panel-pad note">{successMessage}</div> : null}

      <div className="goals-layout">
        <div className="stack">
          <div className="panel panel-pad stack">
            <div className="row">
              <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Create a goal</h2>
              <span className="dim">New target</span>
            </div>

            <div className="form-grid">
              <div className="stack">
                <label className="item-meta">Goal name</label>
                <input
                  className="field"
                  placeholder="Emergency fund"
                  value={createForm.goalName}
                  onChange={(event) => updateCreateField("goalName", event.target.value)}
                />
              </div>
              <div className="stack">
                <label className="item-meta">Target amount</label>
                <input
                  className="field"
                  type="number"
                  placeholder="0"
                  value={createForm.targetAmount}
                  onChange={(event) => updateCreateField("targetAmount", event.target.value)}
                />
              </div>
              <div className="stack">
                <label className="item-meta">Current amount</label>
                <input
                  className="field"
                  type="number"
                  placeholder="0"
                  value={createForm.currentAmount}
                  onChange={(event) => updateCreateField("currentAmount", event.target.value)}
                />
              </div>
              <div className="stack">
                <label className="item-meta">Target date</label>
                <input
                  className="field"
                  type="date"
                  value={createForm.targetDate}
                  onChange={(event) => updateCreateField("targetDate", event.target.value)}
                />
              </div>
            </div>

            <div className="actions-wrap">
              <button className="btn btn-primary" type="button" onClick={handleCreateGoal} disabled={saving}>
                {saving ? "Saving..." : "Create goal"}
              </button>
            </div>
          </div>

          {editingId ? (
            <div className="panel panel-pad stack">
              <div className="row">
                <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Edit goal</h2>
                <button className="btn btn-ghost" type="button" onClick={handleCancelEdit}>Cancel</button>
              </div>

              <div className="form-grid">
                <div className="stack">
                  <label className="item-meta">Goal name</label>
                  <input
                    className="field"
                    value={editForm.goalName}
                    onChange={(event) => updateEditField("goalName", event.target.value)}
                  />
                </div>
                <div className="stack">
                  <label className="item-meta">Target amount</label>
                  <input
                    className="field"
                    type="number"
                    value={editForm.targetAmount}
                    onChange={(event) => updateEditField("targetAmount", event.target.value)}
                  />
                </div>
                <div className="stack">
                  <label className="item-meta">Current amount</label>
                  <input
                    className="field"
                    type="number"
                    value={editForm.currentAmount}
                    onChange={(event) => updateEditField("currentAmount", event.target.value)}
                  />
                </div>
                <div className="stack">
                  <label className="item-meta">Target date</label>
                  <input
                    className="field"
                    type="date"
                    value={editForm.targetDate}
                    onChange={(event) => updateEditField("targetDate", event.target.value)}
                  />
                </div>
              </div>

              <div className="actions-wrap">
                <button className="btn btn-secondary" type="button" onClick={handleUpdateGoal} disabled={saving}>
                  {saving ? "Saving..." : "Save changes"}
                </button>
              </div>
            </div>
          ) : null}
        </div>

        <div className="panel panel-pad stack">
          <div className="row">
            <h2 style={{ margin: 0, fontSize: "1.1rem" }}>Goal tracker</h2>
            <span className="dim">{goals.length} goals</span>
          </div>

          {loading ? (
            <p className="note">Loading goals...</p>
          ) : sortedGoals.length === 0 ? (
            <p className="note">Create your first goal to start tracking progress.</p>
          ) : (
            <div className="goal-list">
              {sortedGoals.map((goal, index) => {
                const progress = Math.max(0, Math.min(100, Number(goal.progressPercentage || 0)));
                const goalId = goal.id;

                return (
                  <div
                    key={goalId}
                    className={`goal-card ${index % 2 === 0 ? "tone-blue" : "tone-neon"}`}
                  >
                    <div className="row">
                      <div>
                        <div className="value">{goal.goalName}</div>
                        <div className="item-meta">Target by {formatTargetDate(goal.targetDate)}</div>
                      </div>
                      <span className="value">{progress.toFixed(0)}%</span>
                    </div>

                    <div className="progress-track">
                      <div className="progress-fill" style={{ width: `${progress}%` }} />
                    </div>

                    <div className="goal-meta">
                      <span>{money(goal.currentAmount)} saved</span>
                      <span className="dim">of {money(goal.targetAmount)}</span>
                    </div>

                    <div className="actions-wrap">
                      <input
                        type="number"
                        className="field"
                        style={{ flex: 1, minWidth: "140px" }}
                        placeholder="Add amount"
                        value={goalContributionById[goalId] ?? ""}
                        onChange={(event) =>
                          setGoalContributionById((prev) => ({
                            ...prev,
                            [goalId]: event.target.value,
                          }))
                        }
                      />
                      <button
                        className="btn btn-secondary"
                        type="button"
                        onClick={() => handleAddMoney(goalId)}
                        disabled={saving}
                      >
                        Add money
                      </button>
                    </div>

                    <div className="actions-wrap">
                      <button className="btn btn-ghost" type="button" onClick={() => handleStartEdit(goal)}>
                        Edit
                      </button>
                      <button className="btn btn-danger" type="button" onClick={() => handleDeleteGoal(goalId)} disabled={saving}>
                        Delete
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
