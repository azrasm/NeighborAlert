import { useState, useEffect, useCallback } from 'react';
import api from '../api/apiClient';
import { getStatusName, getCategoryName } from '../utils/helpers';
import ChangeStatusForm from '../components/reports/ChangeStatusForm';
import AssignModal from '../components/admin/AssignModal';
import FlagsPanel from '../components/admin/FlagsPanel';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

export const AdminPage = ({ currentUser }) => {
  const [assignments, setAssignments] = useState([]);
  const [reports, setReports] = useState([]);
  const [flags, setFlags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showAssign, setShowAssign] = useState(false);
  const [adminTab, setAdminTab] = useState("analitika");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [a, r, f] = await Promise.all([api.getAssignments(), api.getReports(), api.getFlags().catch(() => [])]);
      setAssignments(a); setReports(r); setFlags(f || []);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const statusCount = (name) => reports.filter(r => getStatusName(r) === name).length;
  const allCats = [...new Set(reports.map(r => getCategoryName(r)).filter(c => c && c !== "—"))];
  const catCount = (name) => reports.filter(r => getCategoryName(r) === name).length;
  const unreviewedFlags = flags.filter(f => !f.reviewed).length;

  const tabBtn = (id, label, badge) => (
    <button onClick={() => setAdminTab(id)} style={{
      padding: "8px 20px", border: "none", cursor: "pointer", fontFamily: "var(--font-body)",
      fontSize: 14, fontWeight: 600, borderRadius: "8px", transition: "var(--transition)",
      background: adminTab === id ? "var(--accent)" : "transparent",
      color: adminTab === id ? "#fff" : "var(--muted)",
      position: "relative",
    }}>
      {label}
      {badge > 0 && <span style={{ position: "absolute", top: 2, right: 2, background: "var(--red)", color: "#fff", borderRadius: "50%", width: 16, height: 16, fontSize: 10, fontWeight: 800, display: "flex", alignItems: "center", justifyContent: "center" }}>{badge}</span>}
    </button>
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Administracija</div>
        <div className="page-sub">Upravljanje prijavama, analitika i dodjela odgovornosti</div>
      </div>

      <div className="stats-row">
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--accent-light)" }}>{reports.length}</div><div className="stat-label">Ukupno prijava</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--orange)" }}>{statusCount("Prijavljeno")}</div><div className="stat-label">Prijavljeno</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--accent2)" }}>{statusCount("U toku")}</div><div className="stat-label">U toku</div></div>
        <div className="stat-card"><div className="stat-val" style={{ color: "var(--red)" }}>{unreviewedFlags}</div><div className="stat-label">Nepregledane prijave</div></div>
      </div>

      <div style={{ display: "flex", gap: 4, background: "var(--surface2)", borderRadius: 10, padding: 4, marginBottom: 24, width: "fit-content" }}>
        {tabBtn("analitika", "📊 Analitika")}
        {tabBtn("dodjele", "📋 Dodjele")}
        {tabBtn("prijave-sadrzaja", "🚩 Prijave sadržaja", unreviewedFlags)}
      </div>

      {error && <Alert>{error}</Alert>}

      {loading ? <div className="loading-full"><Spinner /></div> :
        adminTab === "analitika" ? (
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
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
                  <div style={{ fontSize: 14 }}>Riješeno: <strong style={{ color: "var(--green)" }}>{statusCount("Riješeno")}</strong></div>
                  <div style={{ fontSize: 14 }}>U toku: <strong style={{ color: "var(--accent2)" }}>{statusCount("U toku")}</strong></div>
                  <div style={{ fontSize: 14 }}>Prijavljeno: <strong style={{ color: "var(--orange)" }}>{statusCount("Prijavljeno")}</strong></div>
                </div>
              </div>
            </div>
            <div className="card">
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 16 }}>Prijave po kategoriji</div>
              <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                {allCats.map(cat => (
                  <div key={cat} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 12px", background: "var(--surface2)", borderRadius: 8 }}>
                    <span style={{ fontSize: 14 }}>{cat}</span>
                    <span style={{ fontFamily: "var(--font-display)", fontWeight: 800, fontSize: 20, color: "var(--accent-light)" }}>{catCount(cat)}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="card" style={{ gridColumn: "1 / -1" }}>
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 16 }}>Promjena statusa prijave</div>
              <ChangeStatusForm reports={reports} onSaved={load} currentUser={currentUser}/>
            </div>
          </div>
        ) : adminTab === "dodjele" ? (
          <div>
            <div className="toolbar">
              <div style={{ fontFamily: "var(--font-display)", fontWeight: 700 }}>Dodjele prijava</div>
              <button className="btn btn-primary" onClick={() => setShowAssign(true)}>+ Dodijeli prijavu</button>
            </div>
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>ID</th><th>Prijava</th><th>Služba</th><th>Admin ID</th><th>Napomena</th></tr>
                  </thead>
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
        ) : (
          <FlagsPanel flags={flags} onRefresh={load} />
        )
      }
    </div>
  );
};
export default AdminPage;