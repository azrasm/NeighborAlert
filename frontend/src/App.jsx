import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import './styles/global.css';

import LoginPage from './pages/LoginPage';
import ReportsPage from './pages/ReportsPage';
import MapPage from './pages/MapPage';
import MyReportsPage from './pages/MyReportsPage';
import UsersPage from './pages/UsersPage';
import AdminPage from './pages/AdminPage';
import ProfilePage from './pages/ProfilePage';
import Navbar from './components/layout/Navbar';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { authData } = useAuth();
  if (!authData) return <Navigate to="/login" replace />;
  if (requireAdmin && authData.role !== "ADMIN") {
    return (
      <main className="main">
        <div className="empty">
          <div className="empty-icon">🔒</div>
          <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 8 }}>Pristup odbijen</div>
          <div className="empty-text">Nemate ovlaštenje za pristup ovoj stranici.</div>
        </div>
      </main>
    );
  }
  return children;
};

const AppContent = () => {
  const { authData, login, logout, currentUser } = useAuth();

  if (!authData) {
    return <LoginPage onLogin={login} />;
  }

  return (
    <div className="app">
      <Navbar />
      <main className="main">
        <Routes>
          <Route path="/reports" element={<ReportsPage currentUser={currentUser} />} />
          <Route path="/mapa" element={<MapPage currentUser={currentUser} />} />
          <Route path="/moje-prijave" element={<MyReportsPage currentUser={currentUser} />} />
          <Route path="/users" element={<ProtectedRoute requireAdmin><UsersPage /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute requireAdmin><AdminPage currentUser={currentUser} /></ProtectedRoute>} />
          <Route path="/profil" element={<ProfilePage authData={authData} onLogout={logout} />} />
          <Route path="*" element={<Navigate to="/reports" replace />} />
        </Routes>
      </main>
    </div>
  );
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;