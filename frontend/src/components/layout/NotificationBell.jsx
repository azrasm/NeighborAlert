import { useState, useEffect } from 'react';
import api from '../../api/apiClient';
import '../../styles/NotificationPanel.css';

export const NotificationBell = ({ currentUser }) => {
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    if (!currentUser?.userId) return;
    const fetch = async () => {
      try { 
        setNotifications(await api.getNotifications(currentUser.userId)); 
      } catch {}
    };
    fetch();
    const iv = setInterval(fetch, 120000);
    return () => clearInterval(iv);
  }, [currentUser?.userId]);

  const unreadCount = notifications.filter(n => !n.read).length;

  // Marks a single notification as read and removes it from the list instantly
  const handleMarkAsRead = async (id) => {
    try {
      await api.markAsRead(id);
      // Filters it out immediately so it disappears from the view
      setNotifications(prev => prev.filter(n => n.id !== id));
    } catch {}
  };

  // Marks all notifications as read / clears the panel
  const handleDismissAll = async () => {
    try {
      await api.markAsReadAll(currentUser.userId);
      setNotifications([]);
    } catch {}
  };

  return (
    <>
      {/* Bell Button inside Navbar */}
      <button className="nav-btn active" onClick={() => setIsOpen(true)} style={{ position: 'relative' }}>
        🔔 Obavještenja
        {unreadCount > 0 && (
          <span style={{
            position: "absolute", 
            top: -2, 
            right: -2, 
            background: "var(--red)", 
            color: "white", 
            borderRadius: "50%", 
            padding: "2px 6px", 
            fontSize: "10px", 
            fontWeight: "bold" 
          }}>
            {unreadCount}
          </span>
        )}
      </button>

      {/* Backdrop */}
      {isOpen && (
        <div className="noti-backdrop" onClick={() => setIsOpen(false)} />
      )}

      {/* Side Panel Drawer */}
      <div className={`noti-panel ${isOpen ? 'open' : ''}`}>
        <div className="noti-header">
          <span className="noti-title">Obavještenja</span>
          <button className="modal-close" onClick={() => setIsOpen(false)}>&times;</button>
        </div>

        {notifications.length > 0 ? (
          <>
            <div className="noti-actions">
              <button className="btn btn-ghost btn-sm" onClick={handleDismissAll}>
                Označi sve kao pročitano
              </button>
            </div>
            
            <ul className="noti-list">
              {notifications.map((n) => (
                <li 
                  key={n.id} 
                  className="noti-item"
                  style={{ background: n.read ? "transparent" : "rgba(255, 107, 43, 0.03)" }}
                >
                  <div className="noti-content">
                    <div style={{ fontWeight: n.read ? "500" : "700", marginBottom: "4px", color: "var(--text)" }}>
                      {n.title}
                    </div>
                    <p className="noti-text">{n.message}</p>
                    
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginTop: '8px' }}>
                      <span className="noti-time">
                        {new Date(n.createdAt).toLocaleDateString('bs-BA')}
                      </span>
                      
                      {/* Explicit Text Action Button instead of whole-item click */}
                      {!n.read && (
                        <button 
                          onClick={() => handleMarkAsRead(n.id)}
                          style={{
                            background: 'none',
                            border: 'none',
                            color: 'var(--accent-light)',
                            fontSize: '12px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            padding: 0,
                            textDecoration: 'underline'
                          }}
                        >
                          Označi kao pročitano
                        </button>
                      )}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        ) : (
          <div className="empty">
            <div className="empty-icon">🔔</div>
            <p className="empty-text">Nema novih obavještenja</p>
          </div>
        )}
      </div>
    </>
  );
};

export default NotificationBell;