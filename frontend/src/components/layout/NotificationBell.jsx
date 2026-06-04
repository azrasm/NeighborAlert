import { useState, useEffect } from 'react';
import api from '../../api/apiClient';

export const NotificationBell = ({ currentUser }) => {
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    if (!currentUser?.userId) return;
    const fetch = async () => {
      try { setNotifications(await api.getNotifications(currentUser.userId)); }
      catch {}
    };
    fetch();
    const iv = setInterval(fetch, 120000);
    return () => clearInterval(iv);
  }, [currentUser?.userId]);

  const unreadCount = notifications.filter(n => !n.read).length;
  const handleMarkAsRead = async (id) => {
    try {
      await api.markAsRead(id);
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n));
    } catch {}
  };

  return (
    <div style={{ position: "relative", display: "inline-block" }}>
      <button onClick={() => setIsOpen(!isOpen)} style={{ background: "none", border: "none", cursor: "pointer", fontSize: "20px", position: "relative" }}>
        🔔
        {unreadCount > 0 && (
          <span style={{ position: "absolute", top: -2, right: -2, background: "var(--red)", color: "white", borderRadius: "50%", padding: "2px 6px", fontSize: "10px", fontWeight: "bold" }}>
            {unreadCount}
          </span>
        )}
      </button>
      {isOpen && (
        <div style={{ position: "absolute", right: 0, top: "30px", background: "var(--surface2)", boxShadow: "0px 4px 12px rgba(0,0,0,0.15)", borderRadius: "8px", width: "300px", zIndex: 100, maxHeight: "400px", overflowY: "auto", border: "1px solid var(--border)" }}>
          <div style={{ padding: "12px", fontWeight: "bold", borderBottom: "1px solid var(--border)" }}>Obavještenja</div>
          {notifications.length === 0 ? (
            <div style={{ padding: "16px", textAlign: "center", color: "var(--muted)" }}>Nema novih obavještenja</div>
          ) : notifications.map(n => (
            <div key={n.id} onClick={() => handleMarkAsRead(n.id)} style={{ padding: "12px", borderBottom: "1px solid var(--border)", cursor: "pointer", background: n.read ? "transparent" : "rgba(0,123,255,0.05)", fontSize: "13px" }}>
              <div style={{ fontWeight: n.read ? "normal" : "bold", marginBottom: "4px" }}>{n.title}</div>
              <div style={{ color: "var(--text-secondary)" }}>{n.message}</div>
              <div style={{ fontSize: "11px", color: "var(--muted)", marginTop: "4px" }}>{new Date(n.createdAt).toLocaleDateString()}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
export default NotificationBell;