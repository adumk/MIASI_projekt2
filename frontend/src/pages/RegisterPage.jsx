import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    licenseNumber: '',
    licenseExpiryDate: '',
  });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      await register(form);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message || 'Nie udało się utworzyć konta.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="page-shell auth-layout">
      <section className="card auth-card" style={{ maxWidth: '520px' }}>
        <h1>Rejestracja</h1>
        <p className="page-lead">Utwórz konto klienta wypożyczalni (wymagane prawo jazdy).</p>
        <form onSubmit={handleSubmit} className="form">
          <label htmlFor="firstName">Imię</label>
          <input id="firstName" name="firstName" required value={form.firstName} onChange={handleChange} />
          <label htmlFor="lastName">Nazwisko</label>
          <input id="lastName" name="lastName" required value={form.lastName} onChange={handleChange} />
          <label htmlFor="email">E-mail</label>
          <input id="email" name="email" type="email" required value={form.email} onChange={handleChange} />
          <label htmlFor="password">Hasło (min. 8 znaków)</label>
          <input
            id="password"
            name="password"
            type="password"
            required
            minLength={8}
            value={form.password}
            onChange={handleChange}
          />
          <label htmlFor="licenseNumber">Numer prawa jazdy</label>
          <input
            id="licenseNumber"
            name="licenseNumber"
            required
            value={form.licenseNumber}
            onChange={handleChange}
          />
          <label htmlFor="licenseExpiryDate">Ważność prawa jazdy</label>
          <input
            id="licenseExpiryDate"
            name="licenseExpiryDate"
            type="date"
            required
            value={form.licenseExpiryDate}
            onChange={handleChange}
          />
          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          <button className="btn primary" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Tworzenie konta…' : 'Załóż konto'}
          </button>
        </form>
        <p className="muted">
          Masz już konto?{' '}
          <Link className="text-link" to="/login">
            Zaloguj się
          </Link>
        </p>
      </section>
    </main>
  );
}
