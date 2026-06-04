let leafletPromise = null;

export const loadLeaflet = () => {
  if (leafletPromise) return leafletPromise;
  
  leafletPromise = new Promise((resolve) => {
    if (window.L) {
      resolve(window.L);
      return;
    }
    const script = document.createElement("script");
    script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
    script.onload = () => resolve(window.L);
    document.head.appendChild(script);
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
    document.head.appendChild(link);
  });
  
  return leafletPromise;
};