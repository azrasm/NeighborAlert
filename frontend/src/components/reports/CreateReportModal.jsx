import { useState } from 'react';
import api from '../../api/apiClient';
import { CATEGORY_LABELS } from '../../utils/constants';
import Modal from '../common/Modal';
import Alert from '../common/Alert';
import Spinner from '../common/Spinner';
import { MapPicker } from '../maps/MapPicker';
import ImageUpload from '../common/ImageUpload';

export const CreateReportModal = ({ onClose, onCreated, currentUser }) => {
  const [form, setForm] = useState({ title: "", description: "", address: "", categoryId: 1, statusId: 1, userId: currentUser?.userId || 1 });
  const [lat, setLat] = useState(null);
  const [lng, setLng] = useState(null);
  const [imageDataUrl, setImageDataUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    if (!form.title || !form.description || !form.address) { setError("Naslov, opis i adresa su obavezni."); return; }
    setLoading(true); setError("");
    try {
      const payload = {
        title: form.title,
        description: form.description,
        address: form.address,
        userId: Number(form.userId),
        categoryId: Number(form.categoryId),
        statusId: 1,
        latitude: lat || null,
        longitude: lng || null,
        mediaUrls: imageDataUrl ? [imageDataUrl] : [],
      };
      await api.createReport(payload);
      onCreated(); onClose();
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  };

  const field = (key) => ({ value: form[key], onChange: e => setForm(f => ({ ...f, [key]: e.target.value })) });

  return (
    <Modal title="Nova prijava" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <div className="form-group"><label className="form-label">Naslov *</label><input className="form-input" {...field("title")} placeholder="Kratak opis problema" /></div>
      <div className="form-group"><label className="form-label">Opis *</label><textarea className="form-textarea" {...field("description")} placeholder="Detaljan opis..." /></div>
      <div className="form-group"><label className="form-label">Adresa *</label><input className="form-input" {...field("address")} placeholder="Ulica i broj" /></div>
      <div className="form-group">
        <label className="form-label">Kategorija</label>
        <select className="form-select" {...field("categoryId")}>
          {Object.entries(CATEGORY_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">📍 Lokacija na mapi <span style={{ color: "var(--muted)", fontWeight: 400 }}>(opcionalno)</span></label>
        <MapPicker lat={lat} lng={lng} onChange={(la, lo) => { setLat(la); setLng(lo); }} />
      </div>

      <div className="form-group">
        <label className="form-label">📷 Fotografija problema <span style={{ color: "var(--muted)", fontWeight: 400 }}>(opcionalno)</span></label>
        <ImageUpload value={imageDataUrl} onChange={setImageDataUrl} />
      </div>

      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
        <button className="btn btn-ghost" onClick={onClose}>Odustani</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>{loading ? <Spinner /> : "Dodaj prijavu"}</button>
      </div>
    </Modal>
  );
};
export default CreateReportModal;