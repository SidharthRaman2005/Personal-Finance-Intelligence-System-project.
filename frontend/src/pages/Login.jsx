import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!username || !password) {
      alert("Please enter username and password");
      return;
    }

    try {
      setLoading(true);

      const res = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      const data = await res.json();

      if (data.success === true && data.data?.token) {
        localStorage.setItem("username", data.data.username || username);
        localStorage.setItem("token", data.data.token);
        localStorage.setItem("tokenType", data.data.tokenType || "Bearer");

        if (typeof data.data.expiresInSeconds === "number") {
          const expiresAt = Date.now() + data.data.expiresInSeconds * 1000;
          localStorage.setItem("tokenExpiresAt", String(expiresAt));
        }

        navigate("/dashboard", { replace: true });
      } else {
        alert(data.message || "Invalid credentials");
      }
    } catch (err) {
      console.error("Login Error:", err);
      alert("Server error. Make sure backend is running.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <form className="panel panel-pad auth-card stack" onSubmit={handleLogin}>
        <p className="eyebrow">Finance App</p>
        <h1 className="title">Track every rupee with confidence.</h1>
        <p className="subtitle">Sign in to view your dashboard, add transactions, and analyze spending trends.</p>

        <input
          type="text"
          placeholder="Username"
          className="field"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
        />

        <input
          type="password"
          placeholder="Password"
          className="field"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
        />

        <button type="submit" disabled={loading} className="btn btn-primary">
          {loading ? "Logging in..." : "Login"}
        </button>

        <p className="auth-link-row">
          Don't have an account?{" "}
          <button type="button" className="auth-link" onClick={() => navigate("/signup")}>
            Sign up
          </button>
        </p>

        <p className="note">Designed for speed at scale: fewer clicks, fast route loads, and mobile-first interaction.</p>
      </form>
      </div>
  );
}

export default Login;