import { useState } from 'react';
import api from '../../api/apiClient';
import { STATUS_LABELS } from '../../utils/constants';
import Alert from '../common/Alert';
import Spinner from '../common/Spinner';

export const ChangeStatusForm = ({ reports, onSaved, currentUser }) => {
  const [reportId, setReportId] = useState(reports[0]?.id || "");
  const [statusId, setStatusId] = useState(1);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");

  const submit = async () => {
    if (!reportId) return;

    setLoading(true);
    setMsg("");
    setError("");

    const r = reports.find(r => r.id === Number(reportId));

    try {
        await api.updateStatus({
          reportId: Number(reportId),
          adminId: currentUser?.userId,
          newStatus: STATUS_LABELS[Number(statusId)],
          comment: "Status promijenjen preko admin panela",
        });

      setMsg("Status uspješno promijenjen!");
      onSaved();

    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
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
};
export default ChangeStatusForm;