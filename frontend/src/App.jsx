import { useState, useEffect, useCallback } from "react";

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
  updateStatus: (body) => api.request("/api/administration/status", { method: "POST", body: JSON.stringify(body) }),
};

// ─── STYLES ───────────────────────────────────────────────────────────────────
const css = `
  @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:ital,wght@0,300;0,400;0,500;1,300&display=swap');

  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg: #0d0f14;
    --surface: #161920;
    --surface2: #1e2230;
    --border: #2a2f40;
    --accent: #e8ff47;
    --accent2: #47c2ff;
    --red: #ff4d6d;
    --green: #4dffb0;
    --orange: #ffaa47;
    --text: #e8eaf0;
    --muted: #6b7080;
    --font-display: 'Syne', sans-serif;
    --font-body: 'DM Sans', sans-serif;
    --radius: 12px;
    --radius-sm: 6px;
    --transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  html, body, #root { height: 100%; background: var(--bg); color: var(--text); font-family: var(--font-body); }

  /* Layout */
  .app { display: flex; flex-direction: column; min-height: 100vh; }
  .nav { display: flex; align-items: center; gap: 8px; padding: 0 28px; height: 60px; background: var(--surface); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 100; }
  .nav-brand { font-family: var(--font-display); font-weight: 800; font-size: 18px; color: var(--accent); letter-spacing: -0.5px; margin-right: 12px; }
  .nav-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--accent); opacity: 0.5; }
  .nav-btn { padding: 6px 16px; border-radius: 20px; border: none; cursor: pointer; font-family: var(--font-body); font-size: 13px; font-weight: 500; transition: var(--transition); background: transparent; color: var(--muted); }
  .nav-btn:hover, .nav-btn.active { color: var(--text); background: var(--surface2); }
  .nav-spacer { flex: 1; }
  .nav-user { display: flex; align-items: center; gap: 10px; }
  .badge { padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; letter-spacing: 0.5px; text-transform: uppercase; }
  .badge-admin { background: rgba(232,255,71,0.15); color: var(--accent); }
  .badge-user { background: rgba(71,194,255,0.15); color: var(--accent2); }
  .main { flex: 1; padding: 32px 28px; max-width: 1200px; width: 100%; margin: 0 auto; }

  /* Page heading */
  .page-header { margin-bottom: 28px; }
  .page-title { font-family: var(--font-display); font-size: 28px; font-weight: 800; letter-spacing: -1px; }
  .page-sub { color: var(--muted); font-size: 14px; margin-top: 4px; }

  /* Cards */
  .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 20px; }
  .card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }

  /* Report card */
  .report-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 20px; cursor: pointer; transition: var(--transition); position: relative; overflow: hidden; }
  .report-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px; background: var(--border); transition: var(--transition); }
  .report-card:hover { border-color: var(--accent); transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.4); }
  .report-card:hover::before { background: var(--accent); }
  .report-title { font-family: var(--font-display); font-weight: 700; font-size: 16px; margin-bottom: 8px; line-height: 1.3; }
  .report-addr { font-size: 12px; color: var(--muted); display: flex; align-items: center; gap: 4px; margin-bottom: 10px; }
  .report-desc { font-size: 13px; color: var(--muted); line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 12px; }
  .report-meta { display: flex; gap: 8px; flex-wrap: wrap; }

  /* Status chips */
  .chip { padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; border: 1px solid; }
  .chip-pending { color: var(--orange); border-color: rgba(255,170,71,0.3); background: rgba(255,170,71,0.08); }
  .chip-progress { color: var(--accent2); border-color: rgba(71,194,255,0.3); background: rgba(71,194,255,0.08); }
  .chip-resolved { color: var(--green); border-color: rgba(77,255,176,0.3); background: rgba(77,255,176,0.08); }
  .chip-category { color: var(--muted); border-color: var(--border); background: transparent; }

  /* Buttons */
  .btn { display: inline-flex; align-items: center; gap: 6px; padding: 9px 20px; border-radius: var(--radius-sm); border: none; cursor: pointer; font-family: var(--font-body); font-size: 14px; font-weight: 500; transition: var(--transition); }
  .btn-primary { background: var(--accent); color: #0d0f14; }
  .btn-primary:hover { background: #d4ea00; transform: translateY(-1px); }
  .btn-ghost { background: transparent; color: var(--muted); border: 1px solid var(--border); }
  .btn-ghost:hover { color: var(--text); border-color: var(--text); }
  .btn-danger { background: rgba(255,77,109,0.15); color: var(--red); border: 1px solid rgba(255,77,109,0.3); }
  .btn-danger:hover { background: rgba(255,77,109,0.25); }
  .btn-sm { padding: 5px 12px; font-size: 12px; }
  .btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }

  /* Forms */
  .form-group { margin-bottom: 16px; }
  .form-label { display: block; font-size: 12px; font-weight: 500; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px; }
  .form-input, .form-textarea, .form-select { width: 100%; background: var(--bg); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; color: var(--text); font-family: var(--font-body); font-size: 14px; transition: var(--transition); outline: none; }
  .form-input:focus, .form-textarea:focus, .form-select:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(232,255,71,0.08); }
  .form-textarea { resize: vertical; min-height: 90px; }
  .form-select option { background: var(--surface); }

  /* Login */
  .login-wrap { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: var(--bg); }
  .login-card { width: 380px; background: var(--surface); border: 1px solid var(--border); border-radius: 20px; padding: 40px; }
  .login-logo { font-family: var(--font-display); font-weight: 800; font-size: 26px; color: var(--accent); margin-bottom: 4px; }
  .login-tagline { font-size: 13px; color: var(--muted); margin-bottom: 32px; }
  .demo-accounts { margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--border); }
  .demo-row { display: flex; gap: 8px; margin-top: 8px; }
  .demo-pill { flex: 1; background: var(--surface2); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; cursor: pointer; transition: var(--transition); }
  .demo-pill:hover { border-color: var(--accent); }
  .demo-pill-role { font-size: 10px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; }
  .demo-pill-cred { font-size: 13px; font-weight: 500; margin-top: 2px; }

  /* Modal */
  .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(4px); z-index: 200; display: flex; align-items: center; justify-content: center; padding: 24px; animation: fadeIn 0.15s ease; }
  .modal { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 28px; width: 100%; max-width: 560px; max-height: 85vh; overflow-y: auto; animation: slideUp 0.2s ease; }
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
  .modal-title { font-family: var(--font-display); font-weight: 700; font-size: 18px; }
  .modal-close { background: none; border: none; color: var(--muted); font-size: 22px; cursor: pointer; padding: 0 4px; transition: var(--transition); }
  .modal-close:hover { color: var(--text); }
  @keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
  @keyframes slideUp { from { opacity: 0; transform: translateY(16px) } to { opacity: 1; transform: translateY(0) } }

  /* Detail panel */
  .detail-wrap { display: grid; grid-template-columns: 1fr 340px; gap: 20px; align-items: start; }
  @media (max-width: 900px) { .detail-wrap { grid-template-columns: 1fr; } }
  .detail-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 24px; }
  .detail-title { font-family: var(--font-display); font-size: 22px; font-weight: 800; margin-bottom: 12px; }
  .detail-body { font-size: 14px; color: var(--muted); line-height: 1.7; margin-bottom: 16px; }
  .detail-kv { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 20px; }
  .kv-item label { font-size: 11px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; }
  .kv-item p { font-size: 14px; margin-top: 2px; }

  /* Comments */
  .comment-list { display: flex; flex-direction: column; gap: 12px; margin-top: 16px; }
  .comment-item { background: var(--surface2); border-radius: var(--radius-sm); padding: 12px 14px; }
  .comment-meta { font-size: 11px; color: var(--muted); margin-bottom: 4px; }
  .comment-text { font-size: 13px; line-height: 1.5; }

  /* Stats row */
  .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 28px; }
  @media (max-width: 700px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
  .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 18px 20px; }
  .stat-val { font-family: var(--font-display); font-size: 28px; font-weight: 800; }
  .stat-label { font-size: 12px; color: var(--muted); margin-top: 2px; }

  /* Table */
  .table-wrap { overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  th { text-align: left; padding: 10px 14px; color: var(--muted); font-weight: 500; font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid var(--border); }
  td { padding: 12px 14px; border-bottom: 1px solid rgba(42,47,64,0.5); }
  tr:hover td { background: rgba(255,255,255,0.02); }

  /* Alerts */
  .alert { padding: 12px 16px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 16px; }
  .alert-error { background: rgba(255,77,109,0.1); border: 1px solid rgba(255,77,109,0.3); color: var(--red); }
  .alert-success { background: rgba(77,255,176,0.1); border: 1px solid rgba(77,255,176,0.3); color: var(--green); }

  /* Empty state */
  .empty { text-align: center; padding: 60px 24px; color: var(--muted); }
  .empty-icon { font-size: 36px; margin-bottom: 12px; }
  .empty-text { font-size: 14px; }

  /* Spinner */
  .spinner { display: inline-block; width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--accent); border-radius: 50%; animation: spin 0.7s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg) } }
  .loading-full { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 80px; color: var(--muted); }

  /* Top bar actions */
  .toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
  .search-input { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 8px 14px; color: var(--text); font-family: var(--font-body); font-size: 13px; width: 240px; outline: none; transition: var(--transition); }
  .search-input:focus { border-color: var(--accent); }
`;

// ─── HELPERS ──────────────────────────────────────────────────────────────────
const CATEGORY_LABELS = { 1: "Infrastruktura", 2: "Sigurnost", 3: "Komunalne usluge", 4: "Okoliš", 5: "Ostalo" };
const STATUS_LABELS = { 1: "Na čekanju", 2: "U toku", 3: "Riješeno", 4: "Odbijeno" };
const getStatusChip = (statusId) => {
  const map = { 1: "chip-pending", 2: "chip-progress", 3: "chip-resolved", 4: "chip-pending" };
  return map[statusId] || "chip-category";
};

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

// ── LOGIN PAGE ────────────────────────────────────────────────────────────────
function LoginPage({ onLogin }) {
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    if (!form.username || !form.password) { setError("Unesite username i lozinku."); return; }
    setLoading(true); setError("");
    try {
      const data = await api.login(form);
      localStorage.setItem("na_token", data.token);
      onLogin(data);
    } catch (e) { setError(e.message || "Neispravni podaci."); }
    finally { setLoading(false); }
  };

  const fill = (username, password) => setForm({ username, password });

  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="login-logo">NeighborAlert</div>
        <div className="login-tagline">Komunalna platforma za prijavu problema</div>

        {error && <Alert>{error}</Alert>}

        <div className="form-group">
          <label className="form-label">Username</label>
          <input className="form-input" value={form.username}
            onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
            onKeyDown={e => e.key === "Enter" && submit()} placeholder="npr. admin" />
        </div>
        <div className="form-group">
          <label className="form-label">Lozinka</label>
          <input className="form-input" type="password" value={form.password}
            onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
            onKeyDown={e => e.key === "Enter" && submit()} placeholder="••••••••" />
        </div>
        <button className="btn btn-primary" style={{ width: "100%" }} onClick={submit} disabled={loading}>
          {loading ? <><Spinner /> Prijava...</> : "Prijavi se"}
        </button>

        <div className="demo-accounts">
          <div style={{ fontSize: 11, color: "var(--muted)", textTransform: "uppercase", letterSpacing: "0.5px" }}>Demo nalozi</div>
          <div className="demo-row">
            <div className="demo-pill" onClick={() => fill("admin", "admin123")}>
              <div className="demo-pill-role">Admin</div>
              <div className="demo-pill-cred">admin / admin123</div>
            </div>
            <div className="demo-pill" onClick={() => fill("user1", "user123")}>
              <div className="demo-pill-role">Korisnik</div>
              <div className="demo-pill-cred">user1 / user123</div>
            </div>
          </div>
        </div>
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
  const [showCreate, setShowCreate] = useState(false);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try { setReports(await api.getReports()); }
    catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filtered = reports.filter(r =>
    r.title?.toLowerCase().includes(search.toLowerCase()) ||
    r.address?.toLowerCase().includes(search.toLowerCase())
  );

  if (selected) return <ReportDetail report={selected} onBack={() => { setSelected(null); load(); }} currentUser={currentUser} />;

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Prijave</div>
        <div className="page-sub">Sve aktivne komunalne prijave u sistemu</div>
      </div>

      <div className="toolbar">
        <input className="search-input" placeholder="🔍  Pretraži po naslovu ili adresi..."
          value={search} onChange={e => setSearch(e.target.value)} />
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ Nova prijava</button>
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
                <span className={`chip ${getStatusChip(r.statusId)}`}>{STATUS_LABELS[r.statusId] || `Status ${r.statusId}`}</span>
                <span className="chip chip-category">{CATEGORY_LABELS[r.categoryId] || `Kat. ${r.categoryId}`}</span>
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
              <span className={`chip ${getStatusChip(report.statusId)}`}>{STATUS_LABELS[report.statusId] || `Status ${report.statusId}`}</span>
              <span className="chip chip-category">{CATEGORY_LABELS[report.categoryId] || `Kat. ${report.categoryId}`}</span>
            </div>
            <div className="detail-body">{report.description}</div>
            <div className="detail-kv">
              <div className="kv-item"><label>Adresa</label><p>📍 {report.address}</p></div>
              <div className="kv-item"><label>ID Korisnika</label><p>#{report.userId}</p></div>
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
                ["Kategorija", CATEGORY_LABELS[report.categoryId] || "-"],
                ["Status", STATUS_LABELS[report.statusId] || "-"],
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
function AdminPage() {
  const [assignments, setAssignments] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showAssign, setShowAssign] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [a, r] = await Promise.all([api.getAssignments(), api.getReports()]);
      setAssignments(a); setReports(r);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const statusCount = (sid) => reports.filter(r => r.statusId === sid).length;

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Administracija</div>
        <div className="page-sub">Upravljanje prijavama i dodjela odgovornosti</div>
      </div>

      <div className="stats-row">
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--accent)" }}>{reports.length}</div><div className="stat-label">Ukupno prijava</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--orange)" }}>{statusCount(1)}</div><div className="stat-label">Na čekanju</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--accent2)" }}>{statusCount(2)}</div><div className="stat-label">U toku</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--green)" }}>{statusCount(3)}</div><div className="stat-label">Riješeno</div></div>
      </div>

      {error && <Alert>{error}</Alert>}

      <div className="toolbar">
        <div style={{ fontFamily: "var(--font-display)", fontWeight: 700 }}>Dodjele prijava</div>
        <button className="btn btn-primary" onClick={() => setShowAssign(true)}>+ Dodijeli prijavu</button>
      </div>

      {loading ? <div className="loading-full"><Spinner /></div> : (
        <div className="card">
          <div className="table-wrap">
            <table>
              <thead><tr><th>ID</th><th>Prijava ID</th><th>Admin ID</th><th>Status</th></tr></thead>
              <tbody>
                {assignments.length === 0 ? (
                  <tr><td colSpan={4} style={{ textAlign: "center", color: "var(--muted)", padding: 32 }}>Nema dodjela</td></tr>
                ) : assignments.map(a => (
                  <tr key={a.id}>
                    <td style={{ color: "var(--muted)" }}>#{a.id}</td>
                    <td>Prijava #{a.reportId}</td>
                    <td>Admin #{a.adminId}</td>
                    <td><span className={`chip ${getStatusChip(a.statusId)}`}>{STATUS_LABELS[a.statusId] || "-"}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showAssign && <AssignModal reports={reports} onClose={() => setShowAssign(false)} onSaved={load} />}
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

// ─── ROOT APP ─────────────────────────────────────────────────────────────────
export default function App() {
  const [authData, setAuthData] = useState(() => {
    const t = localStorage.getItem("na_token");
    if (!t) return null;
    const p = parseJwt(t);
    return p ? { token: t, username: p.sub, role: p.role || p.roles?.[0] } : null;
  });
  const [page, setPage] = useState("reports");

  const handleLogin = (data) => setAuthData(data);
  const handleLogout = () => { localStorage.removeItem("na_token"); setAuthData(null); setPage("reports"); };

  const isAdmin = authData?.role === "ADMIN";
  const currentUser = authData ? { ...authData, userId: parseJwt(authData.token)?.userId || 1 } : null;

  const pages = [
    { id: "reports", label: "Prijave" },
    ...(isAdmin ? [{ id: "users", label: "Korisnici" }] : []),
    ...(isAdmin ? [{ id: "admin", label: "Administracija" }] : []),
    { id: "profile", label: "Profil" },
  ];

  if (!authData) return (
    <>
      <style>{css}</style>
      <LoginPage onLogin={handleLogin} />
    </>
  );

  return (
    <>
      <style>{css}</style>
      <div className="app">
        <nav className="nav">
          <span className="nav-brand">NeighborAlert</span>
          <span className="nav-dot" />
          {pages.map(p => (
            <button key={p.id} className={`nav-btn ${page === p.id ? "active" : ""}`} onClick={() => setPage(p.id)}>{p.label}</button>
          ))}
          <div className="nav-spacer" />
          <div className="nav-user">
            <span className={`badge ${isAdmin ? "badge-admin" : "badge-user"}`}>{authData.role || "USER"}</span>
            <span style={{ fontSize: 13, color: "var(--muted)" }}>{authData.username}</span>
          </div>
        </nav>
        <main className="main">
          {page === "reports" && <ReportsPage currentUser={currentUser} />}
          {page === "users" && <UsersPage />}
          {page === "admin" && isAdmin && <AdminPage />}
          {page === "profile" && <ProfilePage authData={authData} onLogout={handleLogout} />}
        </main>
      </div>
    </>
  );
}