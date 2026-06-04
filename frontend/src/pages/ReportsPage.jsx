import { useState, useEffect, useCallback } from 'react';
import api from '../api/apiClient';
import { getCategoryName, getStatusName } from '../utils/helpers';
import ReportDetail from '../components/reports/ReportDetail';
import CreateReportModal from '../components/reports/CreateReportModal';
import ReportCard from '../components/reports/ReportCard';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

export const ReportsPage = ({ currentUser }) => {
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
    const matchSearch = !search || r.title?.toLowerCase().includes(search.toLowerCase()) || r.address?.toLowerCase().includes(search.toLowerCase()) || r.description?.toLowerCase().includes(search.toLowerCase());
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
        <input className="search-input" style={{ flex: 1, minWidth: 200 }} placeholder="🔍  Pretraži po naslovu, adresi ili opisu..." value={search} onChange={e => setSearch(e.target.value)} />
        <select className="form-select" style={{ width: 180 }} value={filterCategory} onChange={e => setFilterCategory(e.target.value)}>
          {categories.map(c => <option key={c} value={c}>{c === "sve" ? "Sve kategorije" : c}</option>)}
        </select>
        <select className="form-select" style={{ width: 160 }} value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
          {statuses.map(s => <option key={s} value={s}>{s === "sve" ? "Svi statusi" : s}</option>)}
        </select>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ Nova prijava</button>
      </div>
      <div style={{ fontSize: 13, color: "var(--muted)", marginBottom: 16 }}>Prikazano {filtered.length} od {reports.length} prijava</div>
      {error && <Alert>{error}</Alert>}
      {loading ? (
        <div className="loading-full"><Spinner /> Učitavam prijave...</div>
      ) : filtered.length === 0 ? (
        <div className="empty"><div className="empty-icon">📋</div><div className="empty-text">Nema prijava za prikaz</div></div>
      ) : (
        <div className="card-grid">
          {filtered.map(r => (
            <ReportCard key={r.id} report={r} onClick={setSelected} />
          ))}
        </div>
      )}
      {showCreate && <CreateReportModal onClose={() => setShowCreate(false)} onCreated={load} currentUser={currentUser} />}
    </div>
  );
};
export default ReportsPage;