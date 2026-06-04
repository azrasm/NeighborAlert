import { useState, useEffect } from 'react';
import api from '../api/apiClient';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

export const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try { setUsers(await api.getUsers()); }
      catch (e) { setError(e.message); }
      finally { setLoading(false); }
    })();
  }, []);

  return (
    <div>
      <div className="page-header">
        <div className="page-title">Korisnici</div>
        <div className="page-sub">Registrirani korisnici platforme</div>
      </div>
      {error && <Alert>{error}</Alert>}
      {loading ? <div className="loading-full"><Spinner /></div> : (
        <div className="card">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Ime</th>
                  <th>E-mail</th>
                  <th>Rola</th>
                  <th>Skor</th>
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id}>
                    <td style={{ color: "var(--muted)" }}>#{u.id}</td>
                    <td style={{ fontWeight: 500 }}>{u.username}</td>
                    <td>{u.firstName || u.name || "—"} {u.lastName || ""}</td>
                    <td style={{ color: "var(--muted)" }}>{u.email || "—"}</td>
                    <td>
                      <span className={`badge ${u.role === "ADMIN" ? "badge-admin" : "badge-user"}`}>
                        {u.role || "USER"}
                      </span>
                    </td>
                    <td>{u.userScore ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
export default UsersPage;