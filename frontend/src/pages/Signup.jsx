import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = "http://localhost:8080/api/auth";

function Signup() {
	const navigate = useNavigate();

	const [username, setUsername] = useState("");
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");

	const [signupLoading, setSignupLoading] = useState(false);

	const normalizedEmail = useMemo(() => email.trim().toLowerCase(), [email]);

	const handleSignup = async (event) => {
		event.preventDefault();

		if (!username.trim() || !normalizedEmail || !password.trim()) {
			alert("Please enter username, email, and password");
			return;
		}

		try {
			setSignupLoading(true);

			const response = await fetch(`${API_BASE}/signup`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					username: username.trim(),
					email: normalizedEmail,
					password,
				}),
			});

			const data = await response.json();

			if (data.success) {
				alert("Signup successful. Please login.");
				navigate("/", { replace: true });
			} else {
				alert(data.message || "Signup failed");
			}
		} catch (error) {
			console.error("Signup Error:", error);
			alert("Server error. Please try again.");
		} finally {
			setSignupLoading(false);
		}
	};

	return (
		<div className="auth-shell">
			<form className="panel panel-pad auth-card stack" onSubmit={handleSignup}>
				<p className="eyebrow">Finance App</p>
				<h1 className="title">Create your account</h1>
				<p className="subtitle">Sign up with username, email, and password.</p>

				<input
					type="text"
					placeholder="Username"
					className="field"
					value={username}
					onChange={(event) => setUsername(event.target.value)}
					autoComplete="username"
				/>

				<input
					type="email"
					placeholder="Email"
					className="field"
					value={email}
					onChange={(event) => setEmail(event.target.value)}
					autoComplete="email"
				/>

				<input
					type="password"
					placeholder="Password"
					className="field"
					value={password}
					onChange={(event) => setPassword(event.target.value)}
					autoComplete="new-password"
				/>

				<button type="submit" disabled={signupLoading} className="btn btn-primary">
					{signupLoading ? "Creating account..." : "Create account"}
				</button>

				<p className="auth-link-row">
					Already have an account?{" "}
					<button type="button" className="auth-link" onClick={() => navigate("/")}>
						Login
					</button>
				</p>
			</form>
		</div>
	);
}

export default Signup;
