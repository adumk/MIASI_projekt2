import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function PrivateRoute({ roles }) {
  const { isAuthenticated, isLoading, user, homePath } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <main className="page-shell">
        <section className="card centered">
          <p>Ładowanie sesji…</p>
        </section>
      </main>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles?.length && !roles.includes(user.role)) {
    return <Navigate to={homePath} replace />;
  }

  return <Outlet />;
}
