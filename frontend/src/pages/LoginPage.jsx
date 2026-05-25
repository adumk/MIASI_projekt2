import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: 'jan.kowalski@example.com', password: 'Haslo123!' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const session = await login(form.email, form.password);
      const path =
        session.role === 'ADMIN' ? '/admin' : session.role === 'EMPLOYEE' ? '/employee' : '/';
      navigate(location.state?.from?.pathname ?? path, { replace: true });
    } catch {
      setError('Nie udało się zalogować.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="page-shell auth-layout">
      <section className="card auth-card">
        <h1>Logowanie</h1>
        <p className="page-lead">Wybierz konto zgodne z rolą (klient / pracownik / admin).</p>
        <div className="card" style={{ background: 'var(--color-surface-subtle)', padding: '12px' }}>
          <p className="muted" style={{ fontSize: '0.85rem', margin: 0 }}>
            <strong>Klient:</strong> jan.kowalski@example.com<br />
            <strong>Pracownik:</strong> pracownik@wypozyczalnia.pl<br />
            <strong>Admin:</strong> admin@wypozyczalnia.pl<br />
            Hasło wszystkich: <code>Haslo123!</code>
          </p>
        </div>
        <form className="form" onSubmit={submit}>
          <label htmlFor="email">E-mail</label>
          <input id="email" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <label htmlFor="password">Hasło</label>
          <input id="password" type="password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          {error && <p className="error" role="alert">{error}</p>}
          <button className="btn primary" type="submit" disabled={busy}>
            {busy ? 'Logowanie…' : 'Zaloguj się'}
          </button>
        </form>
        <p className="muted">
          Nie masz konta? <Link className="text-link" to="/register">Rejestracja klienta</Link>
        </p>
      </section>
    </main>
  );
}
