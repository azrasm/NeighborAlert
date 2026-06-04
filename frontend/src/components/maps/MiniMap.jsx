import { useEffect, useRef } from 'react';
import { loadLeaflet } from '../../utils/leafletLoader';

export const MiniMap = ({ lat, lng }) => {
  const containerRef = useRef(null);
  const mapRef = useRef(null);

  useEffect(() => {
    let isMounted = true;
    loadLeaflet().then((L) => {
      if (!isMounted || !containerRef.current || mapRef.current) return;
      const map = L.map(containerRef.current, { zoomControl: true, scrollWheelZoom: false }).setView([lat, lng], 15);
      mapRef.current = map;
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap", maxZoom: 19
      }).addTo(map);
      L.marker([lat, lng]).addTo(map);
    });
    return () => {
      isMounted = false;
      if (mapRef.current) { mapRef.current.remove(); mapRef.current = null; }
    };
  }, [lat, lng]);

  return <div ref={containerRef} style={{ width: "100%", height: 180, borderRadius: 10, overflow: "hidden", border: "1px solid var(--border)" }} />;
};
export default MiniMap;