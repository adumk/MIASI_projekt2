import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AppLayout() {
  const { isAuthenticated, user, logout, isCustomer, isEmployee, isAdmin } = useAuth();

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to={isAuthenticated ? (isAdmin ? '/admin' : isEmployee ? '/employee' : '/') : '/login'} className="brand" end>
            <span className="brand-mark" aria-hidden />
            Wypożyczalnia
          </NavLink>
          <nav className="nav" aria-label="Nawigacja">
            {isAuthenticated ? (
              <>
                {isCustomer && (
                  <>
                    <NavLink to="/" end>Flota</NavLink>
                    <NavLink to="/rentals">Moje wypożyczenia</NavLink>
                    <NavLink to="/profile">Profil</NavLink>
                  </>
                )}
                {isEmployee && (
                  <>
                    <NavLink to="/employee" end>Stanowisko</NavLink>
                    <NavLink to="/employee/fleet">Pojazdy</NavLink>
                  </>
                )}
                {isAdmin && (
                  <>
                    <NavLink to="/admin" end>Panel</NavLink>
                    <NavLink to="/admin/fleet">Flota</NavLink>
                    <NavLink to="/admin/tariffs">Taryfy</NavLink>
                    <NavLink to="/admin/maintenance">Serwis</NavLink>
                  </>
                )}
                <span className="nav-divider" aria-hidden />
                <button type="button" className="btn nav-logout" onClick={logout}>
                  Wyloguj
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login">Logowanie</NavLink>
                <NavLink to="/register">Rejestracja</NavLink>
              </>
            )}
          </nav>
        </div>
        {isAuthenticated && (
          <div className="topbar-user">
            <strong>
              {user.firstName} {user.lastName}
            </strong>{' '}
            · {user.role} · {user.email}
          </div>
        )}
      </header>
      <Outlet />
    </>
  );
}
