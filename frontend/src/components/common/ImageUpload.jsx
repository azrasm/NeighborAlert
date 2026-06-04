import { useState, useRef } from 'react';
import { fileToDataUrl } from '../../utils/helpers';

export const ImageUpload = ({ value, onChange }) => {
  const [drag, setDrag] = useState(false);
  const inputRef = useRef(null);

  const handleFile = async (file) => {
    if (!file) return;
    if (!file.type.startsWith("image/")) { alert("Molimo odaberite sliku."); return; }
    if (file.size > 5 * 1024 * 1024) { alert("Slika ne smije biti veća od 5MB."); return; }
    const dataUrl = await fileToDataUrl(file);
    onChange(dataUrl);
  };

  return (
    <div>
      <div
        className={`file-upload-area${drag ? " drag-over" : ""}`}
        onClick={() => inputRef.current?.click()}
        onDragOver={e => { e.preventDefault(); setDrag(true); }}
        onDragLeave={() => setDrag(false)}
        onDrop={e => { e.preventDefault(); setDrag(false); handleFile(e.dataTransfer.files[0]); }}
      >
        {value ? (
          <div>
            <img src={value} alt="preview" style={{ maxHeight: 120, borderRadius: 8, marginBottom: 8 }} />
            <div style={{ fontSize: 13 }}>Kliknite za promjenu slike</div>
          </div>
        ) : (
          <div>
            <div style={{ fontSize: 28, marginBottom: 8 }}>📷</div>
            <div style={{ fontWeight: 600, marginBottom: 4 }}>Dodajte fotografiju</div>
            <div style={{ fontSize: 12 }}>Kliknite ili prevucite sliku ovdje (opcionalno, max 5MB)</div>
          </div>
        )}
      </div>
      <input ref={inputRef} type="file" accept="image/*" style={{ display: "none" }}
        onChange={e => handleFile(e.target.files[0])} />
      {value && (
        <button className="btn btn-ghost btn-sm" style={{ marginTop: 8 }} onClick={() => onChange(null)}>
          ✕ Ukloni sliku
        </button>
      )}
    </div>
  );
};
export default ImageUpload;