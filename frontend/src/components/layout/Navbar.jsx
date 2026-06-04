import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import NotificationBell from './NotificationBell';

export const Navbar = () => {
  const { authData, logout, currentUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isAdmin = authData?.role === "ADMIN";

  const pages = [
    { path: "/reports", label: "Prijave" },
    { path: "/mapa", label: "🗺️ Mapa" },
    { path: "/moje-prijave", label: "Moje prijave" },
    ...(isAdmin ? [{ path: "/users", label: "Korisnici" }] : []),
    ...(isAdmin ? [{ path: "/admin", label: "Administracija" }] : []),
    { path: "/profil", label: "Profil" },
  ];

  return (
    <nav className="nav">
      <div className="nav-brand" style={{ cursor: "pointer" }} onClick={() => navigate("/reports")}>
        NeighborAlert
      </div>
      <div className="nav-dot" />
      {pages.map(p => (
        <button 
          key={p.path} 
          className={`nav-btn ${location.pathname === p.path ? "active" : ""}`} 
          onClick={() => navigate(p.path)}
        >
          {p.label}
        </button>
      ))}
      <div className="nav-spacer" />
      <div className="nav-user">
        <NotificationBell currentUser={currentUser} />
        <span className={`badge ${isAdmin ? "badge-admin" : "badge-user"}`}>
          {authData?.role || "USER"}
        </span>
        <span style={{ fontSize: 13, color: "var(--muted)" }}>{authData?.username}</span>
        <button className="btn btn-ghost btn-sm" onClick={logout}>Odjava</button>
      </div>
    </nav>
  );
};

export default Navbar;