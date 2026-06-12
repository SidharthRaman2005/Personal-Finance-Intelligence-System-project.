import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function UploadCSV() {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleUpload = async () => {
    const username = localStorage.getItem("username");
    if (!username) {
      navigate("/", { replace: true });
      return;
    }

    if (!file) {
      alert("Please select a CSV file");
      return;
    }

    if (!file.name.endsWith(".csv")) {
      alert("Only CSV files are allowed");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setLoading(true);

      const res = await fetch("http://localhost:8080/api/bank/upload", {
        method: "POST",
        body: formData,
      });

      const data = await res.json();

      if (data.success) {
        alert("CSV uploaded successfully");
        navigate("/dashboard");
      } else {
        alert(data.message);
      }
    } catch (err) {
      console.error(err);
      alert("Upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="panel panel-pad auth-card stack">
        <p className="eyebrow">Import</p>
        <h1 className="title" style={{ fontSize: "1.8rem" }}>
          Upload bank statement
        </h1>
        <p className="subtitle">CSV import helps onboard historical data in seconds.</p>

        <input
          type="file"
          accept=".csv"
          onChange={(e) => setFile(e.target.files[0])}
          className="field"
        />

        {file ? <p className="note">Selected: {file.name}</p> : null}

        <div className="actions-wrap">
          <button onClick={() => navigate("/dashboard")} className="btn btn-ghost" type="button">
            Back
          </button>
          <button onClick={handleUpload} disabled={loading} className="btn btn-neon" type="button">
            {loading ? "Uploading..." : "Upload CSV"}
          </button>
        </div>
      </div>
    </div>
  );
}