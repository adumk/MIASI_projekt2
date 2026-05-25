import { useCallback, useEffect, useState } from 'react';
import * as api from '../api/rentalApi';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();
  const [customer, setCustomer] = useState(null);
  const [canRent, setCanRent] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setError('');
    try {
      const [c, cr] = await Promise.all([
        api.getCustomer(user.customerId),
        api.canRent(user.customerId),
      ]);
      setCustomer(c);
      setCanRent(cr);
    } catch (e) {
      setError(e.message);
    }
  }, [user.customerId]);

  useEffect(() => {
    load();
  }, [load]);

  const verify = async () => {
    setBusy(true);
    try {
      await api.verifyCustomer(user.customerId);
      setSuccess('Konto zweryfikowane.');
      await load();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  if (!customer) {
    return (
      <main className="page-shell">
        <p className="muted">Ładowanie profilu…</p>
      </main>
    );
  }

  return (
    <main className="page-shell">
      <h1>Profil klienta</h1>
      <p className="page-lead">Dane konta i uprawnienia do wypożyczeń.</p>

      {error && <p className="error" role="alert">{error}</p>}
      {success && <p className="success" role="status">{success}</p>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
        <section className="card">
          <h2>Dane osobowe</h2>
          <p>
            <strong>
              {customer.firstName} {customer.lastName}
            </strong>
          </p>
          <p className="muted">{customer.email}</p>
          <p className="vehicle-meta">
            Prawo jazdy: {customer.licenseNumber} (do {customer.licenseExpiryDate})
          </p>
          <p>
            Status: <strong>{customer.status}</strong>
            {customer.verified ? ' · zweryfikowany' : ' · wymaga weryfikacji'}
          </p>
          {!customer.verified && (
            <button type="button" className="btn primary" disabled={busy} onClick={verify}>
              Zweryfikuj konto
            </button>
          )}
        </section>
        <section className="card">
          <h2>Uprawnienia</h2>
          <p>
            Może wypożyczyć:{' '}
            <strong style={{ color: canRent ? '#047857' : '#b91c1c' }}>{canRent ? 'Tak' : 'Nie'}</strong>
          </p>
          {customer.blockReason && <p className="error">Powód blokady: {customer.blockReason}</p>}
          <p className="muted">
            Rola: <strong>{user.role}</strong>
          </p>
        </section>
      </div>
    </main>
  );
}
