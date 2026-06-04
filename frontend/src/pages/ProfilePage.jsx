export const ProfilePage = ({ authData, onLogout }) => {
  return (
    <div style={{ maxWidth: 520 }}>
      <div className="page-header">
        <div className="page-title">Moj profil</div>
        <div className="page-sub">Informacije o trenutnoj sesiji</div>
      </div>
      <div className="card">
        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          {[["Username", authData?.username], ["Rola", authData?.role], ["Token tip", authData?.tokenType || "Bearer"]].map(([l, v]) => (
            <div key={l} style={{ display: "flex", justifyContent: "space-between", paddingBottom: 14, borderBottom: "1px solid var(--border)", fontSize: 14 }}>
              <span style={{ color: "var(--muted)" }}>{l}</span>
              <span style={{ fontWeight: 500 }}>
                {l === "Rola" ? <span className={`badge ${v === "ADMIN" ? "badge-admin" : "badge-user"}`}>{v}</span> : v || "—"}
              </span>
            </div>
          ))}
        </div>
        <div style={{ marginTop: 20 }}>
          <button className="btn btn-danger" onClick={onLogout}>Odjavi se</button>
        </div>
      </div>
    </div>
  );
};
export default ProfilePage;