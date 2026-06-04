import { useState } from 'react';
import api from '../../api/apiClient';
import { CATEGORY_LABELS } from '../../utils/constants';
import Modal from '../common/Modal';
import Alert from '../common/Alert';
import Spinner from '../common/Spinner';

export const EditReportModal = ({ report, onClose, onSaved }) => {
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
      await api.updateReport(report.id, {
        title: form.title, description: form.description, address: form.address,
        userId: report.userId, categoryId: Number(form.categoryId),
        statusId: report.status?.id || report.statusId || 1,
      });
      onSaved();
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  return (
    <Modal title="Uredi prijavu" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <div className="form-group"><label className="form-label">Naslov</label><input className="form-input" value={form.title} onChange={e => setForm(f=>({...f,title:e.target.value}))} /></div>
      <div className="form-group"><label className="form-label">Opis</label><textarea className="form-textarea" value={form.description} onChange={e => setForm(f=>({...f,description:e.target.value}))} /></div>
      <div className="form-group"><label className="form-label">Adresa</label><input className="form-input" value={form.address} onChange={e => setForm(f=>({...f,address:e.target.value}))} /></div>
      <div className="form-group"><label className="form-label">Kategorija</label>
        <select className="form-select" value={form.categoryId} onChange={e => setForm(f=>({...f,categoryId:e.target.value}))}>
          {Object.entries(CATEGORY_LABELS).map(([id, name]) => <option key={id} value={id}>{name}</option>)}
        </select>
      </div>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
        <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading ? <Spinner /> : "Sačuvaj"}</button>
      </div>
    </Modal>
  );
};
export default EditReportModal;