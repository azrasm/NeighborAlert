import { useState, useEffect, useCallback, createContext, useContext } from "react";
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation } from "react-router-dom";

// ─── API CONFIG ────────────────────────────────────────────────────────────────
const API_BASE = "http://localhost:8080"; // API Gateway port

const api = {
  async request(path, options = {}) {
    const token = localStorage.getItem("na_token");
    const headers = { "Content-Type": "application/json", ...options.headers };
    if (token) headers["Authorization"] = `Bearer ${token}`;
    const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
    if (res.status === 401) { localStorage.removeItem("na_token"); window.location.reload(); }
    if (!res.ok) { const err = await res.json().catch(() => ({})); throw new Error(err.message || `HTTP ${res.status}`); }
    if (res.status === 204) return null;
    return res.json();
  },
  // Auth
  login: (body) => api.request("/api/auth/login", { method: "POST", body: JSON.stringify(body) }),
  // Users
  getUsers: () => api.request("/api/users"),
  getUser: (id) => api.request(`/api/users/${id}`),
  // Reports
  getReports: () => api.request("/api/reports"),
  getReport: (id) => api.request(`/api/reports/${id}`),
  createReport: (body) => api.request("/api/reports", { method: "POST", body: JSON.stringify(body) }),
  deleteReport: (id) => api.request(`/api/reports/${id}`, { method: "DELETE" }),
  // Comments
  getCommentsByReport: (reportId) => api.request(`/api/comments/report/${reportId}`),
  addComment: (body) => api.request("/api/comments", { method: "POST", body: JSON.stringify(body) }),
  // Administration (ADMIN only)
  getAssignments: () => api.request("/api/administration/assignments"),
  assignReport: (body) => api.request("/api/administration/assign", { method: "POST", body: JSON.stringify(body) }),
  updateStatus: (body) => api.request("/api/administration/status/change", { method: "POST", body: JSON.stringify(body) }),
};

// ─── STYLES ───────────────────────────────────────────────────────────────────
const css = `
  @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg: #1c2333;
    --surface: #263045;
    --surface2: #2e3a54;
    --border: #3a4a6b;
    --accent: #ff6b2b;
    --accent-light: #ff8c55;
    --accent2: #ffcc00;
    --red: #ff4757;
    --green: #2ed573;
    --orange: #ffa502;
    --text: #f1f4ff;
    --text-secondary: #c8d0e8;
    --muted: #7a8ab0;
    --font-display: 'Plus Jakarta Sans', sans-serif;
    --font-body: 'Plus Jakarta Sans', sans-serif;
    --radius: 14px;
    --radius-sm: 8px;
    --transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  html, body, #root { height: 100%; background: var(--bg); color: var(--text); font-family: var(--font-body); font-size: 15px; }

  /* Layout */
  .app { display: flex; flex-direction: column; min-height: 100vh; }
  .nav { display: flex; align-items: center; gap: 6px; padding: 0 32px; height: 64px; background: var(--surface); border-bottom: 2px solid var(--accent); position: sticky; top: 0; z-index: 100; box-shadow: 0 4px 20px rgba(0,0,0,0.3); }
  .nav-brand { font-family: var(--font-display); font-weight: 800; font-size: 20px; color: var(--accent-light); letter-spacing: -0.5px; margin-right: 16px; display: flex; align-items: center; gap: 8px; }
  .nav-brand::before { content: '🔔'; font-size: 18px; }
  .nav-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--accent); opacity: 0.6; }
  .nav-btn { padding: 7px 18px; border-radius: 20px; border: none; cursor: pointer; font-family: var(--font-body); font-size: 14px; font-weight: 600; transition: var(--transition); background: transparent; color: var(--muted); }
  .nav-btn:hover { color: var(--text); background: var(--surface2); }
  .nav-btn.active { color: var(--accent-light); background: rgba(255,107,43,0.15); }
  .nav-spacer { flex: 1; }
  .nav-user { display: flex; align-items: center; gap: 10px; }
  .badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase; }
  .badge-admin { background: rgba(255,107,43,0.2); color: var(--accent-light); border: 1px solid rgba(255,107,43,0.4); }
  .badge-user { background: rgba(255,204,0,0.15); color: var(--accent2); border: 1px solid rgba(255,204,0,0.3); }
  .main { flex: 1; padding: 36px 32px; max-width: 1240px; width: 100%; margin: 0 auto; }

  /* Page heading */
  .page-header { margin-bottom: 32px; }
  .page-title { font-family: var(--font-display); font-size: 32px; font-weight: 800; letter-spacing: -0.5px; color: var(--text); }
  .page-sub { color: var(--muted); font-size: 15px; margin-top: 6px; }

  /* Cards */
  .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 24px; }
  .card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 18px; }

  /* Report card */
  .report-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 22px; cursor: pointer; transition: var(--transition); position: relative; overflow: hidden; }
  .report-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: var(--border); transition: var(--transition); border-radius: 4px 0 0 4px; }
  .report-card:hover { border-color: var(--accent); transform: translateY(-3px); box-shadow: 0 10px 36px rgba(255,107,43,0.15); }
  .report-card:hover::before { background: var(--accent); }
  .report-title { font-family: var(--font-display); font-weight: 700; font-size: 17px; margin-bottom: 8px; line-height: 1.4; color: var(--text); }
  .report-addr { font-size: 13px; color: var(--muted); display: flex; align-items: center; gap: 4px; margin-bottom: 10px; }
  .report-desc { font-size: 14px; color: var(--text-secondary); line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 14px; }
  .report-meta { display: flex; gap: 8px; flex-wrap: wrap; }

  /* Status chips */
  .chip { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid; }
  .chip-pending { color: var(--orange); border-color: rgba(255,165,2,0.35); background: rgba(255,165,2,0.1); }
  .chip-progress { color: #5bc8ff; border-color: rgba(91,200,255,0.35); background: rgba(91,200,255,0.1); }
  .chip-resolved { color: var(--green); border-color: rgba(46,213,115,0.35); background: rgba(46,213,115,0.1); }
  .chip-category { color: var(--text-secondary); border-color: var(--border); background: rgba(255,255,255,0.04); }

  /* Buttons */
  .btn { display: inline-flex; align-items: center; gap: 6px; padding: 10px 22px; border-radius: var(--radius-sm); border: none; cursor: pointer; font-family: var(--font-body); font-size: 15px; font-weight: 600; transition: var(--transition); }
  .btn-primary { background: var(--accent); color: #fff; box-shadow: 0 4px 14px rgba(255,107,43,0.4); }
  .btn-primary:hover { background: #ff7d45; transform: translateY(-1px); box-shadow: 0 6px 20px rgba(255,107,43,0.5); }
  .btn-ghost { background: transparent; color: var(--text-secondary); border: 1px solid var(--border); }
  .btn-ghost:hover { color: var(--text); border-color: var(--accent-light); }
  .btn-danger { background: rgba(255,71,87,0.12); color: var(--red); border: 1px solid rgba(255,71,87,0.3); }
  .btn-danger:hover { background: rgba(255,71,87,0.22); }
  .btn-sm { padding: 6px 14px; font-size: 13px; }
  .btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; box-shadow: none; }

  /* Forms */
  .form-group { margin-bottom: 18px; }
  .form-label { display: block; font-size: 13px; font-weight: 600; color: var(--text-secondary); margin-bottom: 7px; }
  .form-input, .form-textarea, .form-select { width: 100%; background: var(--bg); border: 1.5px solid var(--border); border-radius: var(--radius-sm); padding: 11px 15px; color: var(--text); font-family: var(--font-body); font-size: 15px; transition: var(--transition); outline: none; }
  .form-input:focus, .form-textarea:focus, .form-select:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(255,107,43,0.15); }
  .form-textarea { resize: vertical; min-height: 100px; }
  .form-select option { background: var(--surface); }

  /* Login */
  .login-wrap { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: var(--bg); background-image: radial-gradient(ellipse at 60% 20%, rgba(255,107,43,0.07) 0%, transparent 60%), radial-gradient(ellipse at 20% 80%, rgba(255,204,0,0.05) 0%, transparent 50%); }
  .login-card { width: 420px; background: var(--surface); border: 1px solid var(--border); border-top: 3px solid var(--accent); border-radius: 24px; padding: 44px; box-shadow: 0 24px 64px rgba(0,0,0,0.4); }
  .login-logo { font-family: var(--font-display); font-weight: 800; font-size: 28px; color: var(--accent-light); margin-bottom: 6px; display: flex; align-items: center; gap: 10px; }
  .login-logo::before { content: '🔔'; font-size: 24px; }
  .login-tagline { font-size: 15px; color: var(--muted); margin-bottom: 36px; }
  .demo-accounts { margin-top: 24px; padding-top: 24px; border-top: 1px solid var(--border); }
  .demo-row { display: flex; gap: 10px; margin-top: 10px; }
  .demo-pill { flex: 1; background: var(--surface2); border: 1.5px solid var(--border); border-radius: var(--radius-sm); padding: 12px 16px; cursor: pointer; transition: var(--transition); }
  .demo-pill:hover { border-color: var(--accent); background: rgba(255,107,43,0.08); }
  .demo-pill-role { font-size: 11px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; }
  .demo-pill-cred { font-size: 14px; font-weight: 600; margin-top: 3px; color: var(--text); }

  /* Modal */
  .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.65); backdrop-filter: blur(6px); z-index: 200; display: flex; align-items: center; justify-content: center; padding: 24px; animation: fadeIn 0.15s ease; }
  .modal { background: var(--surface); border: 1px solid var(--border); border-top: 3px solid var(--accent); border-radius: 20px; padding: 32px; width: 100%; max-width: 580px; max-height: 85vh; overflow-y: auto; animation: slideUp 0.2s ease; box-shadow: 0 24px 64px rgba(0,0,0,0.5); }
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 28px; }
  .modal-title { font-family: var(--font-display); font-weight: 700; font-size: 20px; color: var(--text); }
  .modal-close { background: var(--surface2); border: 1px solid var(--border); color: var(--muted); font-size: 18px; cursor: pointer; padding: 4px 10px; border-radius: 8px; transition: var(--transition); }
  .modal-close:hover { color: var(--text); border-color: var(--accent); }
  @keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
  @keyframes slideUp { from { opacity: 0; transform: translateY(16px) } to { opacity: 1; transform: translateY(0) } }

  /* Detail panel */
  .detail-wrap { display: grid; grid-template-columns: 1fr 360px; gap: 20px; align-items: start; }
  @media (max-width: 900px) { .detail-wrap { grid-template-columns: 1fr; } }
  .detail-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 28px; }
  .detail-title { font-family: var(--font-display); font-size: 24px; font-weight: 800; margin-bottom: 14px; color: var(--text); }
  .detail-body { font-size: 15px; color: var(--text-secondary); line-height: 1.75; margin-bottom: 20px; }
  .detail-kv { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 22px; }
  .kv-item label { font-size: 12px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; }
  .kv-item p { font-size: 15px; margin-top: 3px; color: var(--text); }

  /* Comments */
  .comment-list { display: flex; flex-direction: column; gap: 12px; margin-top: 16px; }
  .comment-item { background: var(--surface2); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 14px 16px; }
  .comment-meta { font-size: 12px; color: var(--muted); margin-bottom: 6px; font-weight: 500; }
  .comment-text { font-size: 14px; line-height: 1.6; color: var(--text-secondary); }

  /* Stats row */
  .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 32px; }
  @media (max-width: 700px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
  .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 22px 24px; border-left: 4px solid var(--accent); }
  .stat-val { font-family: var(--font-display); font-size: 32px; font-weight: 800; }
  .stat-label { font-size: 13px; color: var(--muted); margin-top: 4px; font-weight: 500; }

  /* Table */
  .table-wrap { overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 14px; }
  th { text-align: left; padding: 12px 16px; color: var(--muted); font-weight: 600; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1.5px solid var(--border); }
  td { padding: 14px 16px; border-bottom: 1px solid rgba(58,74,107,0.5); color: var(--text-secondary); }
  tr:hover td { background: rgba(255,107,43,0.04); }

  /* Alerts */
  .alert { padding: 14px 18px; border-radius: var(--radius-sm); font-size: 14px; margin-bottom: 18px; font-weight: 500; }
  .alert-error { background: rgba(255,71,87,0.1); border: 1px solid rgba(255,71,87,0.3); color: var(--red); }
  .alert-success { background: rgba(46,213,115,0.1); border: 1px solid rgba(46,213,115,0.3); color: var(--green); }

  /* Empty state */
  .empty { text-align: center; padding: 70px 24px; color: var(--muted); }
  .empty-icon { font-size: 40px; margin-bottom: 14px; }
  .empty-text { font-size: 15px; }

  /* Spinner */
  .spinner { display: inline-block; width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--accent); border-radius: 50%; animation: spin 0.7s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg) } }
  .loading-full { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 80px; color: var(--muted); font-size: 15px; }

  /* Top bar actions */
  .toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
  .search-input { background: var(--surface); border: 1.5px solid var(--border); border-radius: var(--radius-sm); padding: 10px 16px; color: var(--text); font-family: var(--font-body); font-size: 14px; width: 280px; outline: none; transition: var(--transition); }
  .search-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(255,107,43,0.12); }
`;

// ─── HELPERS ──────────────────────────────────────────────────────────────────
const CATEGORY_LABELS = { 1: "Oštećenje puta", 2: "Sigurnost", 3: "Komunalne usluge", 4: "Okoliš", 5: "Ostalo" };
const STATUS_LABELS = { 1: "Prijavljeno", 2: "U toku", 3: "Riješeno", 4: "Odbijeno" };
const STATUS_CHIP_MAP = { "Prijavljeno": "chip-pending", "U toku": "chip-progress", "Riješeno": "chip-resolved", "Odbijeno": "chip-pending" };

// Backend vraća category/status kao objekat {id, name} ili samo ID u categoryId/statusId
const getCategoryName = (r) => {
  if (r.category?.name) return r.category.name;
  const catId = r.categoryId || r.category?.id;
  if (catId) return CATEGORY_LABELS[Number(catId)] || `Kat. ${catId}`;
  return "Ostalo";
};
const getStatusName = (r) => {
  if (r.status?.name) return r.status.name;
  const statId = r.statusId || r.status?.id;
  if (statId) return STATUS_LABELS[Number(statId)] || `Status ${statId}`;
  return "Prijavljeno";
};
const getStatusId = (r) => r.status?.id || r.statusId || 1;
const getStatusChip = (r) => STATUS_CHIP_MAP[getStatusName(r)] || "chip-category";

function parseJwt(token) {
  try { return JSON.parse(atob(token.split('.')[1])); } catch { return null; }
}

// ─── COMPONENTS ───────────────────────────────────────────────────────────────

function Spinner() { return <span className="spinner" />; }

function Alert({ type = "error", children }) {
  return <div className={`alert alert-${type}`}>{children}</div>;
}

function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <span className="modal-title">{title}</span>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        {children}
      </div>
    </div>
  );
}

// ── LOGIN / REGISTER PAGE ─────────────────────────────────────────────────────
function LoginPage({ onLogin }) {
  const [tab, setTab] = useState("login"); // "login" | "register"
  const [loginForm, setLoginForm] = useState({ username: "", password: "" });
  const [regForm, setRegForm] = useState({ username: "", email: "", password: "", confirm: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const switchTab = (t) => { setTab(t); setError(""); setSuccess(""); };

  const submitLogin = async () => {
    if (!loginForm.username || !loginForm.password) { setError("Unesite username i lozinku."); return; }
    setLoading(true); setError("");
    try {
      const data = await api.login(loginForm);
      localStorage.setItem("na_token", data.token);
      onLogin(data);
    } catch (e) { setError(e.message || "Neispravni podaci."); }
    finally { setLoading(false); }
  };

  const submitRegister = async () => {
    if (!regForm.username || !regForm.email || !regForm.password) { setError("Sva polja su obavezna."); return; }
    if (regForm.password !== regForm.confirm) { setError("Lozinke se ne poklapaju."); return; }
    if (regForm.password.length < 6) { setError("Lozinka mora imati najmanje 6 karaktera."); return; }
    setLoading(true); setError("");
    try {
      const data = await api.request("/api/auth/register", { method: "POST", body: JSON.stringify({ username: regForm.username, email: regForm.email, password: regForm.password }) });
      localStorage.setItem("na_token", data.token);
      onLogin(data);
    } catch (e) { setError(e.message || "Registracija nije uspjela."); }
    finally { setLoading(false); }
  };

  const tabStyle = (t) => ({
    flex: 1, padding: "10px", border: "none", cursor: "pointer",
    fontFamily: "var(--font-body)", fontSize: "15px", fontWeight: 600,
    borderRadius: "10px", transition: "var(--transition)",
    background: tab === t ? "var(--accent)" : "transparent",
    color: tab === t ? "#fff" : "var(--muted)",
  });

  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="login-logo">NeighborAlert</div>
        <div className="login-tagline">Komunalna platforma za prijavu problema</div>

        {/* Tab switcher */}
        <div style={{ display: "flex", gap: "4px", background: "var(--surface2)", borderRadius: "12px", padding: "4px", marginBottom: "28px" }}>
          <button style={tabStyle("login")} onClick={() => switchTab("login")}>Prijava</button>
          <button style={tabStyle("register")} onClick={() => switchTab("register")}>Registracija</button>
        </div>

        {error && <Alert>{error}</Alert>}
        {success && <Alert type="success">{success}</Alert>}

        {tab === "login" ? (
          <>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input className="form-input" value={loginForm.username}
                onChange={e => setLoginForm(f => ({ ...f, username: e.target.value }))}
                onKeyDown={e => e.key === "Enter" && submitLogin()} placeholder="Vaš username" />
            </div>
            <div className="form-group">
              <label className="form-label">Lozinka</label>
              <input className="form-input" type="password" value={loginForm.password}
                onChange={e => setLoginForm(f => ({ ...f, password: e.target.value }))}
                onKeyDown={e => e.key === "Enter" && submitLogin()} placeholder="••••••••" />
            </div>
            <button className="btn btn-primary" style={{ width: "100%" }} onClick={submitLogin} disabled={loading}>
              {loading ? <><Spinner /> Prijava...</> : "Prijavi se"}
            </button>
            <div style={{ textAlign: "center", marginTop: "16px", fontSize: "14px", color: "var(--muted)" }}>
              Nemate nalog?{" "}
              <span style={{ color: "var(--accent-light)", cursor: "pointer", fontWeight: 600 }} onClick={() => switchTab("register")}>
                Registrujte se
              </span>
            </div>
          </>
        ) : (
          <>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input className="form-input" value={regForm.username}
                onChange={e => setRegForm(f => ({ ...f, username: e.target.value }))}
                placeholder="Odaberite username" />
            </div>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input className="form-input" type="email" value={regForm.email}
                onChange={e => setRegForm(f => ({ ...f, email: e.target.value }))}
                placeholder="vas@email.com" />
            </div>
            <div className="form-group">
              <label className="form-label">Lozinka</label>
              <input className="form-input" type="password" value={regForm.password}
                onChange={e => setRegForm(f => ({ ...f, password: e.target.value }))}
                placeholder="Najmanje 6 karaktera" />
            </div>
            <div className="form-group">
              <label className="form-label">Potvrdi lozinku</label>
              <input className="form-input" type="password" value={regForm.confirm}
                onChange={e => setRegForm(f => ({ ...f, confirm: e.target.value }))}
                onKeyDown={e => e.key === "Enter" && submitRegister()}
                placeholder="Ponovite lozinku" />
            </div>
            <button className="btn btn-primary" style={{ width: "100%" }} onClick={submitRegister} disabled={loading}>
              {loading ? <><Spinner /> Registracija...</> : "Registruj se"}
            </button>
            <div style={{ textAlign: "center", marginTop: "16px", fontSize: "14px", color: "var(--muted)" }}>
              Već imate nalog?{" "}
              <span style={{ color: "var(--accent-light)", cursor: "pointer", fontWeight: 600 }} onClick={() => switchTab("login")}>
                Prijavite se
              </span>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

// ── REPORTS PAGE ──────────────────────────────────────────────────────────────
function ReportsPage({ currentUser }) {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filterCategory, setFilterCategory] = useState("sve");
  const [filterStatus, setFilterStatus] = useState("sve");
  const [showCreate, setShowCreate] = useState(false);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try { setReports(await api.getReports()); }
    catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const categories = ["sve", ...new Set(reports.map(r => getCategoryName(r)).filter(c => c && c !== "—"))];
  const statuses = ["sve", ...new Set(reports.map(r => getStatusName(r)).filter(s => s && s !== "—"))];

  const filtered = reports.filter(r => {
    const matchSearch = !search ||
      r.title?.toLowerCase().includes(search.toLowerCase()) ||
      r.address?.toLowerCase().includes(search.toLowerCase()) ||
      r.description?.toLowerCase().includes(search.toLowerCase());
    const matchCat = filterCategory === "sve" || getCategoryName(r) === filterCategory;
    const matchStatus = filterStatus === "sve" || getStatusName(r) === filterStatus;
    return matchSearch && matchCat && matchStatus;
  });

  if (selected) return <ReportDetail report={selected} onBack={() => { setSelected(null); load(); }} currentUser={currentUser} />;

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Prijave</div>
        <div className="page-sub">Sve aktivne komunalne prijave u sistemu</div>
      </div>

      <div style={{ display: "flex", gap: 10, marginBottom: 8, flexWrap: "wrap", alignItems: "center" }}>
        <input className="search-input" style={{ flex: 1, minWidth: 200 }}
          placeholder="🔍  Pretraži po naslovu, adresi ili opisu..."
          value={search} onChange={e => setSearch(e.target.value)} />
        <select className="form-select" style={{ width: 180 }} value={filterCategory} onChange={e => setFilterCategory(e.target.value)}>
          {categories.map(c => <option key={c} value={c}>{c === "sve" ? "Sve kategorije" : c}</option>)}
        </select>
        <select className="form-select" style={{ width: 160 }} value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
          {statuses.map(s => <option key={s} value={s}>{s === "sve" ? "Svi statusi" : s}</option>)}
        </select>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ Nova prijava</button>
      </div>

      <div style={{ fontSize: 13, color: "var(--muted)", marginBottom: 16 }}>
        Prikazano {filtered.length} od {reports.length} prijava
      </div>

      {error && <Alert>{error}</Alert>}

      {loading ? (
        <div className="loading-full"><Spinner /> Učitavam prijave...</div>
      ) : filtered.length === 0 ? (
        <div className="empty"><div className="empty-icon">📋</div><div className="empty-text">Nema prijava za prikaz</div></div>
      ) : (
        <div className="card-grid">
          {filtered.map(r => (
            <div key={r.id} className="report-card" onClick={() => setSelected(r)}>
              <div className="report-title">{r.title}</div>
              <div className="report-addr">📍 {r.address}</div>
              <div className="report-desc">{r.description}</div>
              <div className="report-meta">
                <span className={`chip ${getStatusChip(r)}`}>{getStatusName(r)}</span>
                <span className="chip chip-category">{getCategoryName(r)}</span>
                {r.upvoteCount > 0 && <span className="chip chip-category">👍 {r.upvoteCount}</span>}
              </div>
            </div>
          ))}
        </div>
      )}

      {showCreate && <CreateReportModal onClose={() => setShowCreate(false)} onCreated={load} currentUser={currentUser} />}
    </div>
  );
}

function CreateReportModal({ onClose, onCreated, currentUser }) {
  const [form, setForm] = useState({ title: "", description: "", address: "", categoryId: 1, statusId: 1, userId: currentUser?.userId || 1 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    if (!form.title || !form.description || !form.address) { setError("Sva polja su obavezna."); return; }
    setLoading(true); setError("");
    try {
      await api.createReport({
        title: form.title,
        description: form.description,
        address: form.address,
        userId: Number(form.userId),
        categoryId: Number(form.categoryId),
        statusId: 1
      });
      onCreated(); onClose();
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  const field = (key) => ({ value: form[key], onChange: e => setForm(f => ({ ...f, [key]: e.target.value })) });

  return (
    <Modal title="Nova prijava" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <div className="form-group"><label className="form-label">Naslov</label><input className="form-input" {...field("title")} placeholder="Kratak opis problema" /></div>
      <div className="form-group"><label className="form-label">Opis</label><textarea className="form-textarea" {...field("description")} placeholder="Detaljan opis..." /></div>
      <div className="form-group"><label className="form-label">Adresa</label><input className="form-input" {...field("address")} placeholder="Ulica i broj" /></div>
      <div className="form-group">
        <label className="form-label">Kategorija</label>
        <select className="form-select" {...field("categoryId")}>
          {Object.entries(CATEGORY_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
        <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading ? <Spinner /> : "Dodaj prijavu"}</button>
      </div>
    </Modal>
  );
}

// ── REPORT DETAIL ─────────────────────────────────────────────────────────────
function ReportDetail({ report, onBack, currentUser }) {
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [newComment, setNewComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const { authData } = useAuth();
  const upvoteKey = `upvoted_${authData?.username}_${report.id}`;
  const [upvoted, setUpvoted] = useState(() => localStorage.getItem(upvoteKey) === "1");
  const [upvoteCount, setUpvoteCount] = useState(report.upvoteCount || 0);
  const [upvoting, setUpvoting] = useState(false);

  useEffect(() => {
    (async () => {
      try { setComments(await api.getCommentsByReport(report.id)); }
      catch { setComments([]); }
      finally { setCommentsLoading(false); }
    })();
  }, [report.id]);

  const postComment = async () => {
    if (!newComment.trim()) return;
    setSubmitting(true); setError("");
    try {
      const c = await api.addComment({ reportId: report.id, userId: currentUser?.userId || 1, text: newComment });
      setComments(prev => [...prev, c]); setNewComment("");
    } catch (e) { setError(e.message); }
    finally { setSubmitting(false); }
  };

  const handleUpvote = async () => {
    if (upvoted || upvoting) return;
    setUpvoting(true);
    try {
      await api.request("/api/upvotes", { method: "POST", body: JSON.stringify({ reportId: report.id, userId: currentUser?.userId || 1 }) });
    } catch { /* ignorišemo grešku ako endpoint ne postoji */ }
    finally {
      localStorage.setItem(upvoteKey, "1");
      setUpvoted(true);
      setUpvoteCount(c => c + 1);
      setUpvoting(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 20 }}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}>← Nazad</button>
      </div>
      <div className="detail-wrap">
        <div>
          <div className="detail-card" style={{ marginBottom: 16 }}>
            <div className="detail-title">{report.title}</div>
            <div className="report-meta" style={{ marginBottom: 14 }}>
              <span className={`chip ${getStatusChip(report)}`}>{getStatusName(report)}</span>
              <span className="chip chip-category">{getCategoryName(report)}</span>
            </div>
            <div className="detail-body">{report.description}</div>
            <div className="detail-kv">
              <div className="kv-item"><label>Adresa</label><p>📍 {report.address}</p></div>
              <div className="kv-item"><label>ID Korisnika</label><p>#{report.userId}</p></div>
            </div>
            <div style={{ paddingTop: 16, borderTop: "1px solid var(--border)", display: "flex", alignItems: "center", gap: 12 }}>
              <button
                className="btn btn-sm"
                onClick={handleUpvote}
                disabled={upvoted || upvoting}
                style={{ background: upvoted ? "rgba(46,213,115,0.15)" : "rgba(255,107,43,0.15)", color: upvoted ? "var(--green)" : "var(--accent-light)", border: `1px solid ${upvoted ? "rgba(46,213,115,0.3)" : "rgba(255,107,43,0.3)"}` }}
              >
                {upvoting ? <Spinner /> : upvoted ? "✅ Označili ste" : "👍 I mene pogađa"}
              </button>
              <span style={{ fontSize: 13, color: "var(--muted)" }}>
                {upvoteCount > 0 ? `${upvoteCount} ${upvoteCount === 1 ? "korisnik ima" : "korisnika ima"} isti problem` : "Budite prvi koji će označiti ovaj problem"}
              </span>
            </div>
          </div>

          <div className="detail-card">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 14 }}>Komentari ({comments.length})</div>
            {error && <Alert>{error}</Alert>}
            <div style={{ display: "flex", gap: 8 }}>
              <input className="form-input" style={{ flex: 1 }} value={newComment} onChange={e => setNewComment(e.target.value)}
                placeholder="Dodaj komentar..." onKeyDown={e => e.key === "Enter" && postComment()} />
              <button className="btn btn-primary btn-sm" onClick={postComment} disabled={submitting}>{submitting ? <Spinner /> : "Pošalji"}</button>
            </div>
            {commentsLoading ? <div style={{ textAlign: "center", padding: 20, color: "var(--muted)" }}><Spinner /></div> :
              comments.length === 0 ? <div className="empty" style={{ padding: "24px 0" }}><div className="empty-text">Nema komentara</div></div> :
              <div className="comment-list">
                {comments.map((c, i) => (
                  <div key={c.id || i} className="comment-item">
                    <div className="comment-meta">Korisnik #{c.userId} · {c.createdAt ? new Date(c.createdAt).toLocaleDateString("bs") : "upravo"}</div>
                    <div className="comment-text">{c.text || c.content}</div>
                  </div>
                ))}
              </div>
            }
          </div>
        </div>

        <div>
          <div className="detail-card">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 14, fontSize: 14 }}>Detalji prijave</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {[["ID", `#${report.id}`], ["Naslov", report.title], ["Adresa", report.address],
                ["Kategorija", getCategoryName(report)],
                ["Status", getStatusName(report)],
                ["Korisnik", `#${report.userId}`]].map(([l, v]) => (
                <div key={l} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, paddingBottom: 8, borderBottom: "1px solid var(--border)" }}>
                  <span style={{ color: "var(--muted)" }}>{l}</span>
                  <span style={{ fontWeight: 500 }}>{v}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── MY REPORTS PAGE ───────────────────────────────────────────────────────────
function MyReportsPage({ currentUser }) {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selected, setSelected] = useState(null);
  const [editReport, setEditReport] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const all = await api.getReports();
      setReports(all.filter(r => Number(r.userId) === Number(currentUser?.userId)));
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, [currentUser]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async (id) => {
    try { await api.deleteReport(id); setDeleteConfirm(null); load(); }
    catch (e) { setError(e.message); }
  };

  if (selected) return <ReportDetail report={selected} onBack={() => { setSelected(null); load(); }} currentUser={currentUser} />;

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Moje prijave</div>
        <div className="page-sub">Prijave koje ste vi kreirali</div>
      </div>
      {error && <Alert>{error}</Alert>}
      {loading ? <div className="loading-full"><Spinner /></div> :
        reports.length === 0 ? (
          <div className="empty"><div className="empty-icon">📝</div><div className="empty-text">Nemate nijednu prijavu</div></div>
        ) : (
          <div className="card-grid">
            {reports.map(r => (
              <div key={r.id} className="report-card">
                <div className="report-title" style={{ cursor: "pointer" }} onClick={() => setSelected(r)}>{r.title}</div>
                <div className="report-addr">📍 {r.address}</div>
                <div className="report-desc">{r.description}</div>
                <div className="report-meta" style={{ marginBottom: 12 }}>
                  <span className={`chip ${getStatusChip(r)}`}>{getStatusName(r)}</span>
                  <span className="chip chip-category">{getCategoryName(r)}</span>
                </div>
                <div style={{ display: "flex", gap: 8 }}>
                  <button className="btn btn-ghost btn-sm" onClick={() => setEditReport(r)}>✏️ Uredi</button>
                  <button className="btn btn-danger btn-sm" onClick={() => setDeleteConfirm(r)}>🗑 Obriši</button>
                </div>
              </div>
            ))}
          </div>
        )
      }
      {editReport && <EditReportModal report={editReport} onClose={() => setEditReport(null)} onSaved={() => { setEditReport(null); load(); }} />}
      {deleteConfirm && (
        <Modal title="Potvrdi brisanje" onClose={() => setDeleteConfirm(null)}>
          <p style={{ color: "var(--text-secondary)", marginBottom: 20 }}>
            Da li ste sigurni da želite obrisati prijavu <strong>"{deleteConfirm.title}"</strong>? Ova akcija se ne može poništiti.
          </p>
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <button className="btn btn-ghost" onClick={() => setDeleteConfirm(null)}>Odustani</button>
            <button className="btn btn-danger" onClick={() => handleDelete(deleteConfirm.id)}>Obriši</button>
          </div>
        </Modal>
      )}
    </div>
  );
}

function EditReportModal({ report, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: report.title || "",
    description: report.description || "",
    address: report.address || "",
    categoryId: report.category?.id || report.categoryId || 1,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    if (!form.title || !form.description || !form.address) { setError("Sva polja su obavezna."); return; }
    setLoading(true); setError("");
    try {
      await api.request(`/api/reports/${report.id}`, {
        method: "PUT",
        body: JSON.stringify({
          title: form.title,
          description: form.description,
          address: form.address,
          userId: report.userId,
          categoryId: Number(form.categoryId),
          statusId: report.status?.id || report.statusId || 1,
        })
      });
      onSaved();
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  const cats = Object.entries(CATEGORY_LABELS).map(([id, name]) => ({ id: Number(id), name }));

  return (
    <Modal title="Uredi prijavu" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <div className="form-group"><label className="form-label">Naslov</label>
        <input className="form-input" value={form.title} onChange={e => setForm(f=>({...f,title:e.target.value}))} /></div>
      <div className="form-group"><label className="form-label">Opis</label>
        <textarea className="form-textarea" value={form.description} onChange={e => setForm(f=>({...f,description:e.target.value}))} /></div>
      <div className="form-group"><label className="form-label">Adresa</label>
        <input className="form-input" value={form.address} onChange={e => setForm(f=>({...f,address:e.target.value}))} /></div>
      <div className="form-group"><label className="form-label">Kategorija</label>
        <select className="form-select" value={form.categoryId} onChange={e => setForm(f=>({...f,categoryId:e.target.value}))}>
          {cats.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
        <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading ? <Spinner /> : "Sačuvaj"}</button>
      </div>
    </Modal>
  );
}

// ── USERS PAGE ────────────────────────────────────────────────────────────────
function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try { setUsers(await api.getUsers()); }
      catch (e) { setError(e.message); }
      finally { setLoading(false); }
    })();
  }, []);

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Korisnici</div>
        <div className="page-sub">Registrirani korisnici platforme</div>
      </div>
      {error && <Alert>{error}</Alert>}
      {loading ? <div className="loading-full"><Spinner /> Učitavam korisnike...</div> : (
        <div className="card">
          <div className="table-wrap">
            <table>
              <thead><tr><th>ID</th><th>Username</th><th>Ime</th><th>E-mail</th><th>Rola</th><th>Skor</th></tr></thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id}>
                    <td style={{ color: "var(--muted)" }}>#{u.id}</td>
                    <td style={{ fontWeight: 500 }}>{u.username}</td>
                    <td>{u.firstName || u.name || "—"} {u.lastName || ""}</td>
                    <td style={{ color: "var(--muted)" }}>{u.email || "—"}</td>
                    <td><span className={`badge ${u.role === "ADMIN" ? "badge-admin" : "badge-user"}`}>{u.role || "USER"}</span></td>
                    <td>{u.userScore ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

// ── ADMIN PAGE ────────────────────────────────────────────────────────────────
function AdminPage({currentUser}) {
  const [assignments, setAssignments] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showAssign, setShowAssign] = useState(false);
  const [adminTab, setAdminTab] = useState("analitika"); // "analitika" | "dodjele"

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [a, r] = await Promise.all([api.getAssignments(), api.getReports()]);
      setAssignments(a); setReports(r);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  // Analitika
  const statusCount = (name) => reports.filter(r => getStatusName(r) === name).length;
  const catCount = (name) => reports.filter(r => getCategoryName(r) === name).length;
  const allCats = [...new Set(reports.map(r => getCategoryName(r)).filter(c => c && c !== "—"))];
  const allStatuses = [...new Set(reports.map(r => getStatusName(r)).filter(s => s && s !== "—"))];
  const maxCatCount = Math.max(...allCats.map(c => catCount(c)), 1);
  const maxStatCount = Math.max(...allStatuses.map(s => statusCount(s)), 1);

  const tabBtn = (id, label) => (
    <button onClick={() => setAdminTab(id)} style={{
      padding: "8px 20px", border: "none", cursor: "pointer", fontFamily: "var(--font-body)",
      fontSize: 14, fontWeight: 600, borderRadius: "8px", transition: "var(--transition)",
      background: adminTab === id ? "var(--accent)" : "transparent",
      color: adminTab === id ? "#fff" : "var(--muted)",
    }}>{label}</button>
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Administracija</div>
        <div className="page-sub">Upravljanje prijavama, analitika i dodjela odgovornosti</div>
      </div>

      {/* Stat cards */}
      <div className="stats-row">
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--accent-light)" }}>{reports.length}</div><div className="stat-label">Ukupno prijava</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--orange)" }}>{statusCount("Prijavljeno")}</div><div className="stat-label">Prijavljeno</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--accent2)" }}>{statusCount("U toku")}</div><div className="stat-label">U toku</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--green)" }}>{statusCount("Riješeno")}</div><div className="stat-label">Riješeno</div></div>
      </div>

      {/* Tab switcher */}
      <div style={{ display: "flex", gap: 4, background: "var(--surface2)", borderRadius: 10, padding: 4, marginBottom: 24, width: "fit-content" }}>
        {tabBtn("analitika", "📊 Analitika")}
        {tabBtn("dodjele", "📋 Dodjele")}
      </div>

      {error && <Alert>{error}</Alert>}

      {loading ? <div className="loading-full"><Spinner /></div> : adminTab === "analitika" ? (
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
          {/* Postotak riješenosti */}
          <div className="card">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 16 }}>Postotak riješenosti</div>
            <div style={{ display: "flex", alignItems: "center", gap: 20 }}>
              <div style={{ position: "relative", width: 110, height: 110 }}>
                <svg viewBox="0 0 36 36" style={{ width: 110, height: 110, transform: "rotate(-90deg)" }}>
                  <circle cx="18" cy="18" r="15.9" fill="none" stroke="var(--surface2)" strokeWidth="3" />
                  <circle cx="18" cy="18" r="15.9" fill="none" stroke="var(--green)" strokeWidth="3"
                    strokeDasharray={`${reports.length ? (statusCount("Riješeno") / reports.length * 100) : 0} 100`}
                    strokeLinecap="round" />
                </svg>
                <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 20, color: "var(--green)" }}>
                  {reports.length ? Math.round(statusCount("Riješeno") / reports.length * 100) : 0}%
                </div>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <div style={{ fontSize: 14, color: "var(--text-secondary)" }}>Riješeno: <strong style={{ color: "var(--green)" }}>{statusCount("Riješeno")}</strong></div>
                <div style={{ fontSize: 14, color: "var(--text-secondary)" }}>U toku: <strong style={{ color: "var(--accent2)" }}>{statusCount("U toku")}</strong></div>
                <div style={{ fontSize: 14, color: "var(--text-secondary)" }}>Prijavljeno: <strong style={{ color: "var(--orange)" }}>{statusCount("Prijavljeno")}</strong></div>
                <div style={{ fontSize: 14, color: "var(--text-secondary)" }}>Ukupno: <strong>{reports.length}</strong></div>
              </div>
            </div>
          </div>

          {/* Prijave po kategoriji - samo brojevi */}
          <div className="card">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 16 }}>Prijave po kategoriji</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {allCats.map(cat => (
                <div key={cat} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 12px", background: "var(--surface2)", borderRadius: 8 }}>
                  <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>{cat}</span>
                  <span style={{ fontFamily: "var(--font-display)", fontWeight: 800, fontSize: 20, color: "var(--accent-light)" }}>{catCount(cat)}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Promjena statusa */}
          <div className="card" style={{ gridColumn: "1 / -1" }}>
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 16 }}>Promjena statusa prijave</div>
            <ChangeStatusForm reports={reports} onSaved={load} currentUser={currentUser}/>
          </div>
        </div>
      ) : (
        <div>
          <div className="toolbar">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700 }}>Dodjele prijava</div>
            <button className="btn btn-primary" onClick={() => setShowAssign(true)}>+ Dodijeli prijavu</button>
          </div>
          <div className="card">
            <div className="table-wrap">
              <table>
                <thead><tr><th>ID</th><th>Prijava</th><th>Služba</th><th>Admin ID</th><th>Napomena</th></tr></thead>
                <tbody>
                  {assignments.length === 0 ? (
                    <tr><td colSpan={5} style={{ textAlign: "center", color: "var(--muted)", padding: 32 }}>Nema dodjela</td></tr>
                  ) : assignments.map(a => (
                    <tr key={a.id}>
                      <td style={{ color: "var(--muted)" }}>#{a.id}</td>
                      <td>Prijava #{a.reportId}</td>
                      <td>{a.service || "—"}</td>
                      <td>Admin #{a.adminId}</td>
                      <td style={{ color: "var(--muted)" }}>{a.note || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          {showAssign && <AssignModal reports={reports} onClose={() => setShowAssign(false)} onSaved={load} />}
        </div>
      )}
    </div>
  );
}

function ChangeStatusForm({ reports, onSaved, currentUser}) {
  const [reportId, setReportId] = useState(reports[0]?.id || "");
  const [statusId, setStatusId] = useState(1);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const submit = async () => {
    if (!reportId) return;
    setLoading(true); setMsg(""); setError("");
    const r = reports.find(r => r.id === Number(reportId));
    try {
      await api.updateStatus({
      reportId: Number(reportId),
      adminId: currentUser?.userId,
      newStatus: STATUS_LABELS[Number(statusId)],
      comment: "Status promijenjen preko admin panela"
    });
      setMsg("Status uspješno promijenjen!");
      onSaved();
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr auto", gap: 12, alignItems: "end" }}>
      <div className="form-group" style={{ marginBottom: 0 }}>
        <label className="form-label">Prijava</label>
        <select className="form-select" value={reportId} onChange={e => setReportId(e.target.value)}>
          {reports.map(r => <option key={r.id} value={r.id}>#{r.id} — {r.title}</option>)}
        </select>
      </div>
      <div className="form-group" style={{ marginBottom: 0 }}>
        <label className="form-label">Novi status</label>
        <select className="form-select" value={statusId} onChange={e => setStatusId(e.target.value)}>
          {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>
      <button className="btn btn-primary" onClick={submit} disabled={loading} style={{ marginBottom: 0 }}>
        {loading ? <Spinner /> : "Promijeni"}
      </button>
      {msg && <div style={{ gridColumn: "1/-1" }}><Alert type="success">{msg}</Alert></div>}
      {error && <div style={{ gridColumn: "1/-1" }}><Alert>{error}</Alert></div>}
    </div>
  );
}

function AssignModal({ reports, onClose, onSaved }) {
  const [form, setForm] = useState({ reportId: reports[0]?.id || "", adminId: "", service: "", note: "", statusId: 1 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    if (!form.reportId || !form.adminId || !form.service.trim()) { setError("Sva polja su obavezna."); return; }
    setLoading(true); setError("");
    try {
      await api.assignReport({ reportId: Number(form.reportId), adminId: Number(form.adminId), service: form.service, note: form.note, statusId: Number(form.statusId) });
      onSaved(); onClose();
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  return (
    <Modal title="Dodijeli prijavu nadležnoj službi" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <div className="form-group">
        <label className="form-label">Prijava</label>
        <select className="form-select" value={form.reportId} onChange={e => setForm(f => ({ ...f, reportId: e.target.value }))}>
          {reports.map(r => <option key={r.id} value={r.id}>#{r.id} — {r.title}</option>)}
        </select>
      </div>
      <div className="form-group">
        <label className="form-label">Naziv službe</label>
        <input className="form-input" value={form.service} onChange={e => setForm(f => ({ ...f, service: e.target.value }))} placeholder="npr. Komunalne usluge Sarajevo" />
      </div>
      <div className="form-group">
        <label className="form-label">Napomena (opcionalno)</label>
        <input className="form-input" value={form.note} onChange={e => setForm(f => ({ ...f, note: e.target.value }))} placeholder="Dodatne napomene..." />
      </div>
      <div className="form-group">
        <label className="form-label">Admin ID</label>
        <input className="form-input" type="number" value={form.adminId} onChange={e => setForm(f => ({ ...f, adminId: e.target.value }))} placeholder="ID admina" />
      </div>
      <div className="form-group">
        <label className="form-label">Novi status</label>
        <select className="form-select" value={form.statusId} onChange={e => setForm(f => ({ ...f, statusId: e.target.value }))}>
          {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
        <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading ? <Spinner /> : "Dodijeli"}</button>
      </div>
    </Modal>
  );
}

// ── PROFILE PAGE ──────────────────────────────────────────────────────────────
function ProfilePage({ authData, onLogout }) {
  return (
    <div style={{ maxWidth: 520 }}>
      <div className="page-header">
        <div className="page-title">Moj profil</div>
        <div className="page-sub">Informacije o trenutnoj sesiji</div>
      </div>
      <div className="card">
        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          {[["Username", authData?.username], ["Rola", authData?.role], ["Token tip", authData?.tokenType || "Bearer"],
            ["Token ističe za", authData?.expiresIn ? `${authData.expiresIn}s` : "—"]].map(([l, v]) => (
            <div key={l} style={{ display: "flex", justifyContent: "space-between", paddingBottom: 14, borderBottom: "1px solid var(--border)", fontSize: 14 }}>
              <span style={{ color: "var(--muted)" }}>{l}</span>
              <span style={{ fontWeight: 500 }}>
                {l === "Rola" ? <span className={`badge ${v === "ADMIN" ? "badge-admin" : "badge-user"}`}>{v}</span> : v || "—"}
              </span>
            </div>
          ))}
        </div>
        <div style={{ marginTop: 20 }}>
          <button className="btn btn-danger" onClick={onLogout}>Odjavi se</button>
        </div>
      </div>
    </div>
  );
}

// ─── AUTH CONTEXT ─────────────────────────────────────────────────────────────

const AuthContext = createContext(null);
const useAuth = () => useContext(AuthContext);

// ─── PROTECTED ROUTE ──────────────────────────────────────────────────────────
function ProtectedRoute({ children, requireAdmin = false }) {
  const { authData } = useAuth();
  const location = useLocation();

  if (!authData) {
    // Spremi URL koji je korisnik pokušao posjetiti
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requireAdmin && authData.role !== "ADMIN") {
    return (
      <div className="main">
        <div className="empty">
          <div className="empty-icon">🔒</div>
          <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 8, color: "var(--text)" }}>Pristup odbijen</div>
          <div className="empty-text">Nemate ovlaštenje za pristup ovoj stranici.</div>
        </div>
      </div>
    );
  }

  return children;
}

// ─── NAVBAR ───────────────────────────────────────────────────────────────────
function Navbar() {
  const { authData, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isAdmin = authData?.role === "ADMIN";

  const pages = [
    { path: "/reports", label: "Prijave" },
    { path: "/moje-prijave", label: "Moje prijave" },
    ...(isAdmin ? [{ path: "/users", label: "Korisnici" }] : []),
    ...(isAdmin ? [{ path: "/admin", label: "Administracija" }] : []),
    { path: "/profil", label: "Profil" },
  ];

  return (
    <nav className="nav">
      <span className="nav-brand" style={{ cursor: "pointer" }} onClick={() => navigate("/reports")}>
        NeighborAlert
      </span>
      <span className="nav-dot" />
      {pages.map(p => (
        <button key={p.path}
          className={`nav-btn ${location.pathname === p.path ? "active" : ""}`}
          onClick={() => navigate(p.path)}>
          {p.label}
        </button>
      ))}
      <div className="nav-spacer" />
      <div className="nav-user">
        <span className={`badge ${isAdmin ? "badge-admin" : "badge-user"}`}>{authData?.role || "USER"}</span>
        <span style={{ fontSize: 13, color: "var(--muted)" }}>{authData?.username}</span>
        <button className="btn btn-ghost btn-sm" onClick={logout}>Odjava</button>
      </div>
    </nav>
  );
}

// ─── ROOT APP ─────────────────────────────────────────────────────────────────
export default function App() {
  const [authData, setAuthData] = useState(() => {
    const t = localStorage.getItem("na_token");
    if (!t) return null;
    const p = parseJwt(t);
    if (!p) return null;
    // Provjeri da li je token istekao
    if (p.exp && p.exp * 1000 < Date.now()) {
      localStorage.removeItem("na_token");
      return null;
    }
    return { token: t, username: p.sub, role: p.role || p.roles?.[0] };
  });

  const login = (data) => setAuthData(data);
  const logout = () => { localStorage.removeItem("na_token"); setAuthData(null); };
  const jwtPayload = parseJwt(authData?.token);
  const currentUser = authData ? { ...authData, userId: jwtPayload?.userId ? Number(jwtPayload.userId) : null } : null;

  return (
    <AuthContext.Provider value={{ authData, login, logout }}>
      <style>{css}</style>
      <BrowserRouter>
        <Routes>
          {/* Javne rute */}
          <Route path="/login" element={
            authData ? <Navigate to="/reports" replace /> : <LoginPage onLogin={login} />
          } />

          {/* Zaštićene rute */}
          <Route path="/*" element={
            <ProtectedRoute>
              <div className="app">
                <Navbar />
                <main className="main">
                  <Routes>
                    <Route path="/reports" element={<ReportsPage currentUser={currentUser} />} />
                    <Route path="/moje-prijave" element={<MyReportsPage currentUser={currentUser} />} />
                    <Route path="/users" element={
                      <ProtectedRoute requireAdmin>
                        <UsersPage />
                      </ProtectedRoute>
                    } />
                    <Route path="/admin" element={
                      <ProtectedRoute requireAdmin>
                        <AdminPage currentUser={currentUser} />
                      </ProtectedRoute>
                    } />
                    <Route path="/profil" element={<ProfilePage authData={authData} onLogout={logout} />} />
                    <Route path="*" element={<Navigate to="/reports" replace />} />
                  </Routes>
                </main>
              </div>
            </ProtectedRoute>
          } />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to={authData ? "/reports" : "/login"} replace />} />
        </Routes>
      </BrowserRouter>
    </AuthContext.Provider>
  );
}