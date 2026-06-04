import { createContext, useContext, useState } from 'react';
import { parseJwt } from '../utils/helpers';

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [authData, setAuthData] = useState(() => {
    const t = localStorage.getItem("na_token");
    if (!t) return null;
    const p = parseJwt(t);
    if (!p) return null;
    if (p.exp && p.exp * 1000 < Date.now()) { localStorage.removeItem("na_token"); return null; }
    return { token: t, username: p.sub, role: p.role || p.roles?.[0] };
  });

  const login = (data) => setAuthData(data);
  const logout = () => { localStorage.removeItem("na_token"); setAuthData(null); };
  
  const jwtPayload = parseJwt(authData?.token);
  const currentUser = authData ? { ...authData, userId: jwtPayload?.userId ? Number(jwtPayload.userId) : null } : null;

  return (
    <AuthContext.Provider value={{ authData, login, logout, currentUser }}>
      {children}
    </AuthContext.Provider>
  );
};