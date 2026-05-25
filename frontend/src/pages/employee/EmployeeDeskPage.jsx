import { useEffect, useState } from 'react';
import * as api from '../../api/rentalApi';
import { enrichRental, fetchVehicleMap } from '../../utils/vehicles';

export default function EmployeeDeskPage() {
  const [rentalId, setRentalId] = useState('');
  const [pendingRentals, setPendingRentals] = useState([]);
  const [vehicleMap, setVehicleMap] = useState({});
  const [rental, setRental] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState('');
  const [returnForm, setReturnForm] = useState({ mileage: '', inspectionNotes: '' });
  const [canRentFlag, setCanRentFlag] = useState(null);

  const loadRental = async (id = rentalId) => {
    setError('');
    setRental(null);
    setCustomer(null);
    setCanRentFlag(null);
    try {
      let map = vehicleMap;
      if (Object.keys(map).length === 0) {
        map = await fetchVehicleMap().catch(() => ({}));
        setVehicleMap(map);
      }
      const r = enrichRental(await api.getRental(id.trim()), map);
      setRental(r);
      const c = await api.getCustomer(r.customerId);
      setCustomer(c);
      setCanRentFlag(await api.canRent(r.customerId));
    } catch (e) {
      setError(e.message);
    }
  };

  useEffect(() => {
    const loadQueue = async () => {
      try {
        const map = await fetchVehicleMap().catch(() => ({}));
        setVehicleMap(map);
        const [reserved, active] = await Promise.all([
          api.getRentals('RESERVED'),
          api.getRentals('ACTIVE'),
        ]);
        const all = [...(reserved || []), ...(active || [])];
        setPendingRentals(all.map((r) => enrichRental(r, map)));
      } catch {
        setPendingRentals([]);
      }
    };
    loadQueue();
  }, []);

  const run = async (action) => {
    setBusy(action);
    setError('');
    setSuccess('');
    try {
      if (action === 'verify') {
        await api.verifyCustomer(rental.customerId);
        setSuccess('Tożsamość klienta zweryfikowana.');
        setCustomer(await api.getCustomer(rental.customerId));
      } else if (action === 'confirm') {
        const r = enrichRental(await api.confirmReservation(rental.rentalId), vehicleMap);
        setRental(r);
        setSuccess('Przedpłata potwierdzona.');
      } else if (action === 'activate') {
        const r = enrichRental(await api.activateRental(rental.rentalId), vehicleMap);
        setRental(r);
        setSuccess('Pojazd wydany (status RENTED w flocie).');
      } else if (action === 'return') {
        const r = enrichRental(await api.returnRental(rental.rentalId, {
          actualReturnDate: new Date().toISOString().slice(0, 10),
          mileage: returnForm.mileage ? Number(returnForm.mileage) : null,
          inspectionNotes: returnForm.inspectionNotes || null,
        }), vehicleMap);
        setRental(r);
        setSuccess('Zwrot przyjęty — faktura generowana przez system.');
      }
      const map = vehicleMap;
      const [reserved, active] = await Promise.all([api.getRentals('RESERVED'), api.getRentals('ACTIVE')]);
      setPendingRentals([...(reserved || []), ...(active || [])].map((item) => enrichRental(item, map)));
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy('');
    }
  };

  return (
    <main className="page-shell">
      <h1>Stanowisko pracownika</h1>
      <p className="page-lead">Weryfikacja klienta, wydanie i zwrot pojazdu, inspekcja.</p>

      {pendingRentals.length > 0 && (
        <section className="card">
          <h2>Rezerwacje i aktywne wypożyczenia</h2>
          <ul className="rental-list">
            {pendingRentals.map((r) => (
              <li key={r.rentalId}>
                <button
                  type="button"
                  className="linkish"
                  onClick={() => {
                    setRentalId(r.rentalId);
                    loadRental(r.rentalId);
                  }}>
                  <strong>{r.vehicleName}</strong> — {r.status} · {r.periodStart} → {r.periodEnd}
                </button>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="card">
        <label>Wyszukaj wypożyczenie (ID)</label>
        <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
          <input style={{ flex: 1 }} value={rentalId} onChange={(e) => setRentalId(e.target.value)} placeholder="UUID rezerwacji" />
          <button type="button" className="btn primary" onClick={() => loadRental()}>
            Wczytaj
          </button>
        </div>
      </section>

      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      {rental && customer && (
        <section className="card">
          <h2>Szczegóły</h2>
          <p>
            <strong>Pojazd:</strong> {rental.vehicleName}
            {rental.vehicleMeta ? ` (${rental.vehicleMeta})` : ''} · <strong>Klient:</strong>{' '}
            {customer.firstName} {customer.lastName}
          </p>
          <p className="vehicle-meta">
            {rental.periodStart} → {rental.periodEnd} · Status: <strong>{rental.status}</strong>
            {rental.paymentConfirmed ? ' · przedpłata OK' : ''}
          </p>
          <p className="vehicle-meta">
            Klient zweryfikowany: {customer.verified ? 'tak' : 'nie'} · Może wypożyczyć:{' '}
            {canRentFlag === null ? '…' : canRentFlag ? 'tak' : 'nie'}
          </p>

          <div className="rental-card-actions">
            {!customer.verified && (
              <button type="button" className="btn secondary" disabled={!!busy} onClick={() => run('verify')}>
                Weryfikuj tożsamość
              </button>
            )}
            {rental.status === 'RESERVED' && !rental.paymentConfirmed && (
              <button type="button" className="btn primary" disabled={!!busy} onClick={() => run('confirm')}>
                Potwierdź przedpłatę
              </button>
            )}
            {rental.status === 'RESERVED' && rental.paymentConfirmed && (
              <button type="button" className="btn primary" disabled={!!busy} onClick={() => run('activate')}>
                Wydaj pojazd
              </button>
            )}
          </div>

          {(rental.status === 'ACTIVE' || rental.status === 'OVERDUE') && (
            <div style={{ marginTop: '16px' }}>
              <h3>Zwrot i inspekcja</h3>
              <div className="form">
                <label>Przebieg (km)</label>
                <input value={returnForm.mileage} onChange={(e) => setReturnForm({ ...returnForm, mileage: e.target.value })} />
                <label>Notatki z inspekcji</label>
                <textarea rows={3} value={returnForm.inspectionNotes} onChange={(e) => setReturnForm({ ...returnForm, inspectionNotes: e.target.value })} />
              </div>
              <button type="button" className="btn primary" disabled={!!busy} onClick={() => run('return')}>
                Przyjmij zwrot
              </button>
            </div>
          )}
        </section>
      )}
    </main>
  );
}
