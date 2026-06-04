import { useState, useEffect, useCallback } from 'react';
import api from '../api/apiClient';
import { getStatusName, getCategoryName, getStatusChip } from '../utils/helpers';
import ReportDetail from '../components/reports/ReportDetail';
import EditReportModal from '../components/reports/EditReportModal';
import Modal from '../components/common/Modal';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

export const MyReportsPage = ({ currentUser }) => {
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
                {r.mediaUrls && r.mediaUrls.length > 0 && (
                  <img src={r.mediaUrls[0]} alt="" style={{ width: "100%", height: 120, objectFit: "cover", borderRadius: 8, marginBottom: 10 }} />
                )}
                <div className="report-title" style={{ cursor: "pointer" }} onClick={() => setSelected(r)}>{r.title}</div>
                <div className="report-addr">📍 {r.address}{r.latitude ? " 🗺️" : ""}</div>
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
            Da li ste sigurni da želite obrisati prijavu <strong>"{deleteConfirm.title}"</strong>?
          </p>
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <button className="btn btn-ghost" onClick={() => setDeleteConfirm(null)}>Odustani</button>
            <button className="btn btn-danger" onClick={() => handleDelete(deleteConfirm.id)}>Obriši</button>
          </div>
        </Modal>
      )}
    </div>
  );
};
export default MyReportsPage;