import { useState } from 'react';
import api from '../../api/apiClient';
import Spinner from '../common/Spinner';

export const FlagsPanel = ({ flags, onRefresh }) => {
  const [filter, setFilter] = useState("unreviewed");
  const [processing, setProcessing] = useState(null);

  const displayed = filter === "all" ? flags : flags.filter(f => !f.reviewed);

  const handleReview = async (id) => {
    setProcessing(id);
    try { await api.reviewFlag(id, true); onRefresh(); }
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
                <th>Akcija</th>
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
                  <td><span style={{ fontWeight: 600, color: "var(--accent-light)" }}>#{f.reportId}</span></td>
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
                      <button
                        className="btn btn-sm btn-ghost"
                        onClick={() => handleReview(f.id)}
                        disabled={processing === f.id}
                        style={{ fontSize: 12 }}
                      >
                        {processing === f.id ? <Spinner /> : "✓ Označi pregledanim"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
export default FlagsPanel;