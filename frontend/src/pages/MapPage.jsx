import { useState, useEffect } from 'react';
import api from '../api/apiClient';
import ReportsMapView from '../components/maps/ReportsMapView';
import ReportDetail from '../components/reports/ReportDetail';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';
import { STATUS_COLORS } from '../utils/constants';

export const MapPage = ({ currentUser }) => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try { setReports(await api.getReports()); }
      catch (e) { setError(e.message); }
      finally { setLoading(false); }
    })();
  }, []);

  const reportsWithCoords = reports.filter(r => r.latitude && r.longitude);

  if (selected) return <ReportDetail report={selected} onBack={() => setSelected(null)} currentUser={currentUser} />;

  return (
    <div>
      <div className="page-header">
        <div className="page-title">🗺️ Mapa prijava</div>
        <div className="page-sub">Prikaz svih prijavljenih problema na interaktivnoj mapi</div>
      </div>

      <div className="map-legend">
        {Object.entries(STATUS_COLORS).map(([status, color]) => (
          <div key={status} className="map-legend-item">
            <div className="legend-dot" style={{ background: color, borderColor: color + "88" }} />
            <span>{status}</span>
          </div>
        ))}
        <span style={{ marginLeft: "auto", fontSize: 13, color: "var(--muted)" }}>
          {reportsWithCoords.length} od {reports.length} prijava ima lokaciju
        </span>
      </div>

      {error && <Alert>{error}</Alert>}
      {loading ? (
        <div className="loading-full"><Spinner /> Učitavam prijave...</div>
      ) : reportsWithCoords.length === 0 ? (
        <div>
          <div className="empty" style={{ paddingBottom: 16 }}>
            <div className="empty-icon">🗺️</div>
            <div className="empty-text">Nema prijava s označenom lokacijom.<br />Kreirajte novu prijavu i označite lokaciju na mapi.</div>
          </div>
          <div style={{ opacity: 0.5 }}>
            <ReportsMapView reports={[]} onSelectReport={setSelected} />
          </div>
        </div>
      ) : (
        <>
          <ReportsMapView reports={reports} onSelectReport={setSelected} />
          <div style={{ marginTop: 12, fontSize: 13, color: "var(--muted)" }}>
            💡 Kliknite na marker da vidite detalje prijave
          </div>
        </>
      )}
    </div>
  );
};
export default MapPage;