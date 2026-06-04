import { useEffect, useRef } from 'react';
import { loadLeaflet } from '../../utils/leafletLoader';

export const MapPicker = ({ lat, lng, onChange }) => {
  const containerRef = useRef(null);
  const mapRef = useRef(null);
  const markerRef = useRef(null);

  useEffect(() => {
    let isMounted = true;
    loadLeaflet().then((L) => {
      if (!isMounted || !containerRef.current || mapRef.current) return;
      const defaultLat = lat || 43.8476;
      const defaultLng = lng || 18.3564;
      const map = L.map(containerRef.current).setView([defaultLat, defaultLng], 13);
      mapRef.current = map;
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap contributors",
        maxZoom: 19,
      }).addTo(map);

      if (lat && lng) {
        markerRef.current = L.marker([lat, lng]).addTo(map);
      }

      map.on("click", (e) => {
        const { lat: clickLat, lng: clickLng } = e.latlng;
        if (markerRef.current) {
          markerRef.current.setLatLng([clickLat, clickLng]);
        } else {
          markerRef.current = L.marker([clickLat, clickLng]).addTo(map);
        }
        onChange(clickLat, clickLng);
      });
    });
    return () => {
      isMounted = false;
      if (mapRef.current) { mapRef.current.remove(); mapRef.current = null; }
    };
  }, []);

  return (
    <div>
      <div ref={containerRef} className="map-container" style={{ height: 280 }} />
      <div className="map-hint">📍 Kliknite na mapu da označite lokaciju problema</div>
      {lat && lng && (
        <div className="map-coords">📌 {lat.toFixed(5)}, {lng.toFixed(5)}</div>
      )}
    </div>
  );
};
export default MapPicker;