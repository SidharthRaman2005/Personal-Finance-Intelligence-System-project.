import { Suspense, lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

const Login = lazy(() => import("./pages/Login"));
const Signup = lazy(() => import("./pages/Signup"));
const Dashboard = lazy(() => import("./pages/Dashboard"));
const AddExpense = lazy(() => import("./pages/AddExpense"));
const UploadCSV = lazy(() => import("./pages/UploadCSV"));
const Expenses = lazy(() => import("./pages/Expenses"));
const Analysis = lazy(() => import("./pages/Analysis"));
const AddIncome = lazy(() => import("./pages/AddIncome"));
const Details = lazy(() => import("./pages/Details"));
const Investments = lazy(() => import("./pages/Investments"));
const AddInvestment = lazy(() => import("./pages/AddInvestment"));
const InvestmentAnalysis = lazy(() => import("./pages/InvestmentAnalysis"));
const Goals = lazy(() => import("./pages/Goals"));
const MonthlyReport = lazy(() => import("./pages/MonthlyReport"));

function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token");
  const username = localStorage.getItem("username");
  const tokenExpiresAt = Number(localStorage.getItem("tokenExpiresAt") || "0");

  if (tokenExpiresAt && Date.now() > tokenExpiresAt) {
    localStorage.removeItem("token");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("tokenExpiresAt");
    localStorage.removeItem("username");
    return <Navigate to="/" replace />;
  }

  if (!token || !username) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function App() {
  return (
    <div className="app-root">
      <div className="app-shell">
        <Suspense fallback={<div className="screen-loader">Loading workspace...</div>}>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/add-expense" element={<ProtectedRoute><AddExpense /></ProtectedRoute>} />
            <Route path="/upload-csv" element={<ProtectedRoute><UploadCSV /></ProtectedRoute>} />
            <Route path="/expenses" element={<ProtectedRoute><Expenses /></ProtectedRoute>} />
            <Route path="/details" element={<ProtectedRoute><Details /></ProtectedRoute>} />
            <Route path="/analysis" element={<ProtectedRoute><Analysis /></ProtectedRoute>} />
            <Route path="/add-income" element={<ProtectedRoute><AddIncome /></ProtectedRoute>} />
            <Route path="/investments" element={<ProtectedRoute><Investments /></ProtectedRoute>} />
            <Route path="/add-investment" element={<ProtectedRoute><AddInvestment /></ProtectedRoute>} />
            <Route path="/investment-analysis" element={<ProtectedRoute><InvestmentAnalysis /></ProtectedRoute>} />
            <Route path="/goals" element={<ProtectedRoute><Goals /></ProtectedRoute>} />
            <Route path="/reports/monthly" element={<ProtectedRoute><MonthlyReport /></ProtectedRoute>} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </div>
    </div>
  );
}

export default App;