import { useState } from 'react';
import api from '../../api/apiClient';
import Modal from '../common/Modal';
import Alert from '../common/Alert';
import Spinner from '../common/Spinner';

export const AssignModal = ({ reports, onClose, onSaved }) => {
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
      <div className="form-group"><label className="form-label">Naziv službe</label><input className="form-input" value={form.service} onChange={e => setForm(f => ({ ...f, service: e.target.value }))} placeholder="npr. Komunalne usluge Sarajevo" /></div>
      <div className="form-group"><label className="form-label">Napomena (opcionalno)</label><input className="form-input" value={form.note} onChange={e => setForm(f => ({ ...f, note: e.target.value }))} placeholder="Dodatne napomene..." /></div>
      <div className="form-group"><label className="form-label">Admin ID</label><input className="form-input" type="number" value={form.adminId} onChange={e => setForm(f => ({ ...f, adminId: e.target.value }))} placeholder="ID admina" /></div>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
        <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading ? <Spinner /> : "Dodijeli"}</button>
      </div>
    </Modal>
  );
};
export default AssignModal;