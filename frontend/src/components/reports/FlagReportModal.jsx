import { useState } from 'react';
import api from '../../api/apiClient';
import { FLAG_REASONS } from '../../utils/constants';
import Modal from '../common/Modal';
import Alert from '../common/Alert';
import Spinner from '../common/Spinner';

export const FlagReportModal = ({ report, currentUser, onClose, onFlagged }) => {
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const submit = async () => {
    if (!reason.trim()) { setError("Molimo navedite razlog."); return; }
    setLoading(true); setError("");
    try {
      await api.createFlag({
        reason: reason,
        reportId: report.id,
        userId: currentUser?.userId || 1,
      });
      setSuccess(true);
      setTimeout(() => { onFlagged(); onClose(); }, 1500);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  return (
    <Modal title="🚩 Prijavi lažni/neprikladni sadržaj" onClose={onClose}>
      {success ? (
        <Alert type="success">Prijava je uspješno poslata. Hvala što pomažete u održavanju kvaliteta platforme!</Alert>
      ) : (
        <>
          <p style={{ color: "var(--text-secondary)", fontSize: 14, marginBottom: 20, lineHeight: 1.6 }}>
            Prijavljujete objavu: <strong style={{ color: "var(--text)" }}>"{report.title}"</strong>
            <br />Vaša prijava će biti pregledana od strane administratora.
          </p>
          {error && <Alert>{error}</Alert>}
          <div className="form-group">
            <label className="form-label">Razlog prijave</label>
            <select className="form-select" value={reason} onChange={e => setReason(e.target.value)}>
              <option value="">-- Odaberite razlog --</option>
              {FLAG_REASONS.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          {reason === "Ostalo" && (
            <div className="form-group">
              <label className="form-label">Dodatno pojašnjenje</label>
              <textarea className="form-textarea" placeholder="Opišite problem..." style={{ minHeight: 80 }}
                onChange={e => setReason("Ostalo: " + e.target.value)} />
            </div>
          )}
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 8 }}>
            <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
            <button className="btn btn-danger" onClick={submit} disabled={loading || !reason}>
              {loading ? <Spinner /> : "🚩 Pošalji prijavu"}
            </button>
          </div>
        </>
      )}
    </Modal>
  );
};
export default FlagReportModal;