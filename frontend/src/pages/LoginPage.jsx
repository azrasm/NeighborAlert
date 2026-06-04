import { useState } from 'react';
import api from '../api/apiClient';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

export const LoginPage = ({ onLogin }) => {
  const [tab, setTab] = useState("login");
  const [loginForm, setLoginForm] = useState({ username: "", password: "" });
  const [regForm, setRegForm] = useState({ username: "", email: "", password: "", confirm: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const switchTab = (t) => { setTab(t); setError(""); };

  const submitLogin = async () => {
    if (!loginForm.username || !loginForm.password) { setError("Unesite username i lozinku."); return; }
    setLoading(true); setError("");
    try {
      const data = await api.login(loginForm);
      localStorage.setItem("na_token", data.token);
      onLogin(data);
    } catch (e) { setError(e.message || "Neispravni podaci."); }
    finally { setLoading(false); }
  };

  const submitRegister = async () => {
    if (!regForm.username || !regForm.email || !regForm.password) { setError("Sva polja su obavezna."); return; }
    if (regForm.password !== regForm.confirm) { setError("Lozinke se ne poklapaju."); return; }
    if (regForm.password.length < 6) { setError("Lozinka mora imati najmanje 6 karaktera."); return; }
    setLoading(true); setError("");
    try {
      const data = await api.register(regForm);
      localStorage.setItem("na_token", data.token);
      onLogin(data);
    } catch (e) { setError(e.message || "Registracija nije uspjela."); }
    finally { setLoading(false); }
  };

  const tabStyle = (t) => ({
    flex: 1, padding: "10px", border: "none", cursor: "pointer",
    fontFamily: "var(--font-body)", fontSize: "15px", fontWeight: 600,
    borderRadius: "10px", transition: "var(--transition)",
    background: tab === t ? "var(--accent)" : "transparent",
    color: tab === t ? "#fff" : "var(--muted)",
  });

  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="login-logo">NeighborAlert</div>
        <div className="login-tagline">Komunalna platforma za prijavu problema</div>
        <div style={{ display: "flex", gap: "4px", background: "var(--surface2)", borderRadius: "12px", padding: "4px", marginBottom: "28px" }}>
          <button style={tabStyle("login")} onClick={() => switchTab("login")}>Prijava</button>
          <button style={tabStyle("register")} onClick={() => switchTab("register")}>Registracija</button>
        </div>
        {error && <Alert>{error}</Alert>}
        {tab === "login" ? (
          <>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input className="form-input" value={loginForm.username} onChange={e => setLoginForm(f => ({ ...f, username: e.target.value }))} onKeyDown={e => e.key === "Enter" && submitLogin()} placeholder="Vaš username" />
            </div>
            <div className="form-group">
              <label className="form-label">Lozinka</label>
              <input className="form-input" type="password" value={loginForm.password} onChange={e => setLoginForm(f => ({ ...f, password: e.target.value }))} onKeyDown={e => e.key === "Enter" && submitLogin()} placeholder="••••••••" />
            </div>
            <button className="btn btn-primary" style={{ width: "100%" }} onClick={submitLogin} disabled={loading}>
              {loading ? <><Spinner /> Prijava...</> : "Prijavi se"}
            </button>
          </>
        ) : (
          <>
            <div className="form-group"><label className="form-label">Username</label><input className="form-input" value={regForm.username} onChange={e => setRegForm(f => ({ ...f, username: e.target.value }))} placeholder="Odaberite username" /></div>
            <div className="form-group"><label className="form-label">Email</label><input className="form-input" type="email" value={regForm.email} onChange={e => setRegForm(f => ({ ...f, email: e.target.value }))} placeholder="vas@email.com" /></div>
            <div className="form-group"><label className="form-label">Lozinka</label><input className="form-input" type="password" value={regForm.password} onChange={e => setRegForm(f => ({ ...f, password: e.target.value }))} placeholder="Najmanje 6 karaktera" /></div>
            <div className="form-group"><label className="form-label">Potvrdi lozinku</label><input className="form-input" type="password" value={regForm.confirm} onChange={e => setRegForm(f => ({ ...f, confirm: e.target.value }))} onKeyDown={e => e.key === "Enter" && submitRegister()} placeholder="Ponovite lozinku" /></div>
            <button className="btn btn-primary" style={{ width: "100%" }} onClick={submitRegister} disabled={loading}>
              {loading ? <><Spinner /> Registracija...</> : "Registruj se"}
            </button>
          </>
        )}
      </div>
    </div>
  );
};
export default LoginPage;