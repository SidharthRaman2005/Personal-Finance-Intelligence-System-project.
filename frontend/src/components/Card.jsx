export default function Card({ title, value, color = "", children }) {
  return (
    <div className={`metric-card ${color}`}>
      <div className="metric-title">{title}</div>
      <div className="metric-value">{value}</div>
      {children}
    </div>
  );
}
