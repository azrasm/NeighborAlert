import { useState } from 'react';
import api from '../../api/apiClient';
import Spinner from '../common/Spinner';
import ReportDetail from '../reports/ReportDetail';
import Modal from '../common/Modal';

export const FlagsPanel = ({ flags, onRefresh, currentUser}) => {
  const [filter, setFilter] = useState("unreviewed");
  const [processing, setProcessing] = useState(null);
  const [selected, setSelected] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const displayed = filter === "all" ? flags : flags.filter(f => !f.reviewed);

  const handleReview = async (id) => {
    setProcessing(id);
    try { await api.reviewFlag(id, true); onRefresh(); }
    catch {}
    finally { setProcessing(null); }
  };

  const handleOpenReport = async (reportId) => {
    setProcessing(`open-${reportId}`); // Privremeni spinner samo za taj klik
    try {
      // Pretpostavka je da tvoj apiClient ima getReport ili getReportById
      const reportData = await api.getReport(reportId); 
      setSelected(reportData);
    } catch (error) {
      console.error("Greška pri učitavanju detalja prijave:", error);
    } finally {
      setProcessing(null);
    }
  };

  // Ako je prijava selektovana, odmah vrati ReportDetail
  if (selected) {
    return (
      <ReportDetail 
        report={selected} 
        onBack={() => { setSelected(null); onRefresh(); }} 
        currentUser={currentUser} 
      />
    );
  }

  //Funkcija za brisanje i automatski review
  const handleDeleteReport = async () => {
    if (!deleteConfirm) return;
    
    const flagId = deleteConfirm.id;
    const reportId = deleteConfirm.reportId;
    
    setDeleteConfirm(null); // Zatvori modal odmah
    setProcessing(flagId);
    try { 
      await api.deleteReport(reportId); 
      await api.reviewFlag(flagId, true); 
      onRefresh(); 
    }
    catch {}
    finally { setProcessing(null); }
  };

  return (
    <div>
      <div className="toolbar">
        <div style={{ fontFamily: "var(--font-display)", fontWeight: 700 }}>
          Prijave lažnog/neprikladnog sadržaja
          {flags.filter(f => !f.reviewed).length > 0 && (
            <span style={{ marginLeft: 10, background: "var(--red)", color: "#fff", borderRadius: 20, padding: "2px 10px", fontSize: 12, fontWeight: 700 }}>
              {flags.filter(f => !f.reviewed).length} novih
            </span>
          )}
        </div>
        <div style={{ display: "flex", gap: 6 }}>
          <button className={`btn btn-sm ${filter === "unreviewed" ? "btn-primary" : "btn-ghost"}`} onClick={() => setFilter("unreviewed")}>Nepregledane</button>
          <button className={`btn btn-sm ${filter === "all" ? "btn-primary" : "btn-ghost"}`} onClick={() => setFilter("all")}>Sve</button>
        </div>
      </div>
      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Prijava #</th>
                <th>Korisnik #</th>
                <th>Razlog</th>
                <th>Status</th>
                <th style={{ textAlign: "center", paddingRight: "12px" }}>Akcija</th>
              </tr>
            </thead>
            <tbody>
              {displayed.length === 0 ? (
                <tr><td colSpan={6} style={{ textAlign: "center", color: "var(--muted)", padding: 32 }}>
                  {filter === "unreviewed" ? "✅ Nema nepregledanih prijava" : "Nema prijava sadržaja"}
                </td></tr>
              ) : displayed.map(f => (
                <tr key={f.id}>
                  <td style={{ color: "var(--muted)" }}>#{f.id}</td>

                  {/* Klik na broj prijave sada okida handleOpenReport */}
                  <td>
                    <span 
                      onClick={() => handleOpenReport(f.reportId)} 
                      style={{ fontWeight: 600, color: "var(--accent-light)", cursor: "pointer", textDecoration: "underline" }}
                      title="Pogledaj detalje prijave"
                    >
                      {processing === `open-${f.reportId}` ? <Spinner /> : `#${f.reportId}`}
                    </span>
                  </td>

                  <td>#{f.userId}</td>
                  <td><div className="flag-reason" title={f.reason}>{f.reason}</div></td>
                  <td>
                    {f.reviewed
                      ? <span className="chip chip-resolved" style={{ fontSize: 11 }}>✅ Pregledano</span>
                      : <span className="chip chip-pending" style={{ fontSize: 11 }}>⏳ Na čekanju</span>
                    }
                  </td>
                  <td>
                    {!f.reviewed && (
                      <div style={{ display: "flex", gap: 6, justifyContent: "flex-end" }}>
                        <button
                          className="btn btn-sm btn-ghost"
                          onClick={() => handleReview(f.id)}
                          disabled={processing === f.id}
                          style={{ fontSize: 12 }}
                        >
                          {processing === f.id ? <Spinner /> : "✓ Označi pregledanim"}
                        </button>

                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => setDeleteConfirm(f)}
                          disabled={processing === f.id}
                          style={{ fontSize: 12 }}
                        >
                          {processing === f.id ? <Spinner /> : "✗ Obriši prijavu"}
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal za brisanje*/}
      {deleteConfirm && (
        <Modal title="Potvrdi brisanje" onClose={() => setDeleteConfirm(null)}>
          <p style={{ color: "var(--text-secondary)", marginBottom: 20 }}>
            Da li ste sigurni da želite obrisati prijavu <strong>#{deleteConfirm.reportId}</strong>?<br />
            <small style={{ color: 'var(--muted)' }}>Razlog prijave: {deleteConfirm.reason}</small>
          </p>
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
            <button className="btn btn-ghost" onClick={() => setDeleteConfirm(null)}>Odustani</button>
            <button className="btn btn-danger" onClick={handleDeleteReport}>Obriši</button>
          </div>
        </Modal>
      )}

    </div>
  );
};
export default FlagsPanel;