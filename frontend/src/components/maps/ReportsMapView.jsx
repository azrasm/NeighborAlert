import { useEffect, useRef } from 'react';
import { loadLeaflet } from '../../utils/leafletLoader';
import { getStatusName, getCategoryName } from '../../utils/helpers';
import { STATUS_COLORS } from '../../utils/constants';

export const ReportsMapView = ({ reports, onSelectReport }) => {
  const containerRef = useRef(null);
  const mapRef = useRef(null);

  useEffect(() => {
    let isMounted = true;
    loadLeaflet().then((L) => {
      if (!isMounted || !containerRef.current || mapRef.current) return;
      const map = L.map(containerRef.current).setView([43.8476, 18.3564], 8);
      mapRef.current = map;
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "© OpenStreetMap contributors",
        maxZoom: 19,
      }).addTo(map);

      const reportsWithCoords = reports.filter(r => r.latitude && r.longitude);

      reportsWithCoords.forEach((r) => {
        const statusName = getStatusName(r);
        const color = STATUS_COLORS[statusName] || "#7a8ab0";
        const icon = L.divIcon({
          className: "",
          html: `<div style="width:14px;height:14px;border-radius:50%;background:${color};border:2px solid rgba(0,0,0,0.3);box-shadow:0 2px 6px rgba(0,0,0,0.4)"></div>`,
          iconSize: [14, 14],
          iconAnchor: [7, 7],
        });
        const marker = L.marker([r.latitude, r.longitude], { icon }).addTo(map);
        const catName = getCategoryName(r);
        const chipStyle = `background:${color}22;color:${color};border:1px solid ${color}55;border-radius:10px;padding:2px 8px;font-size:11px;font-weight:600;display:inline-block;margin-bottom:4px;`;
        marker.bindPopup(`
          <div style="min-width:180px;">
            <div class="map-popup-title">${r.title}</div>
            <div class="map-popup-addr">📍 ${r.address}</div>
            <span style="${chipStyle}">${statusName}</span>
            <br/><span style="font-size:11px;color:#7a8ab0">${catName}</span>
          </div>
        `);
        marker.on("click", () => onSelectReport && onSelectReport(r));
      });

      if (reportsWithCoords.length > 0) {
        const bounds = L.latLngBounds(reportsWithCoords.map(r => [r.latitude, r.longitude]));
        map.fitBounds(bounds, { padding: [40, 40], maxZoom: 14 });
      }
    });
    return () => {
      isMounted = false;
      if (mapRef.current) { mapRef.current.remove(); mapRef.current = null; }
    };
  }, [reports]);

  return <div ref={containerRef} className="map-container-full" />;
};
export default ReportsMapView;