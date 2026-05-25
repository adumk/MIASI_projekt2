/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { getCurrentUser, loginUser, registerUser } from '../api/authApi';
import { TOKEN_KEY } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      if (!token) {
        setIsLoading(false);
        return;
      }
      try {
        setUser(await getCurrentUser());
      } catch {
        localStorage.removeItem(TOKEN_KEY);
        setToken(null);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, [token]);

  useEffect(() => {
    const handler = () => {
      setToken(null);
      setUser(null);
    };
    window.addEventListener('miasi:unauthorized', handler);
    return () => window.removeEventListener('miasi:unauthorized', handler);
  }, []);

  const applySession = (response) => {
    if (!response.accessToken) throw new Error('Brak tokenu');
    localStorage.setItem(TOKEN_KEY, response.accessToken);
    setToken(response.accessToken);
    setUser({
      customerId: response.customerId,
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      role: response.role,
      verified: response.verified,
    });
    return response;
  };

  const login = async (email, password) => applySession(await loginUser({ email, password }));
  const register = async (payload) => applySession(await registerUser(payload));
  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
  };

  const value = useMemo(() => {
    const role = user?.role ?? '';
    return {
      token,
      user,
      isLoading,
      isAuthenticated: Boolean(token && user),
      isCustomer: role === 'CUSTOMER',
      isEmployee: role === 'EMPLOYEE',
      isAdmin: role === 'ADMIN',
      homePath: role === 'ADMIN' ? '/admin' : role === 'EMPLOYEE' ? '/employee' : '/',
      login,
      register,
      logout,
    };
  }, [token, user, isLoading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth wymaga AuthProvider');
  return ctx;
}
