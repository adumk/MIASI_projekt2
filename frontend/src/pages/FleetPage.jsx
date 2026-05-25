import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as api from '../api/rentalApi';
import { useAuth } from '../context/AuthContext';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function defaultEndDate() {
  const d = new Date();
  d.setDate(d.getDate() + 3);
  return d.toISOString().slice(0, 10);
}

export default function FleetPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    status: 'AVAILABLE',
    category: '',
    maxPrice: '',
    start: '',
    end: '',
  });
  const [booking, setBooking] = useState(null);
  const [bookingDates, setBookingDates] = useState({ start: '', end: '' });
  const [quote, setQuote] = useState(null);
  const [busy, setBusy] = useState(false);

  const openBooking = (vehicle) => {
    setBooking(vehicle);
    setBookingDates({
      start: filters.start || todayIso(),
      end: filters.end || defaultEndDate(),
    });
    setError('');
  };

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = { status: filters.status || undefined, category: filters.category || undefined };
      if (filters.maxPrice) params.maxDailyRateMinorUnits = Number(filters.maxPrice) * 100;
      let list = await api.getVehicles(params);
      if (filters.start && filters.end) {
        const busyIds = new Set(await api.getBusyVehicleIds(filters.start, filters.end));
        list = list.filter((v) => !busyIds.has(v.vehicleId));
      }
      setVehicles(list);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [filters.status, filters.category, filters.maxPrice, filters.start, filters.end]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!booking || !bookingDates.start || !bookingDates.end) {
      setQuote(null);
      return;
    }
    if (bookingDates.end <= bookingDates.start) {
      setQuote(null);
      return;
    }
    api
      .getQuote(booking.category, bookingDates.start, bookingDates.end)
      .then(setQuote)
      .catch(() => setQuote(null));
  }, [booking, bookingDates.start, bookingDates.end]);

  const reserve = async () => {
    if (!bookingDates.start || !bookingDates.end) {
      setError('Wybierz datę rozpoczęcia i zakończenia wypożyczenia.');
      return;
    }
    if (bookingDates.end <= bookingDates.start) {
      setError('Data zakończenia musi być późniejsza niż rozpoczęcia.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const busyIds = await api.getBusyVehicleIds(bookingDates.start, bookingDates.end);
      if (busyIds.includes(booking.vehicleId)) {
        setError('Ten pojazd jest już zarezerwowany w wybranym terminie. Wybierz inne daty lub inne auto.');
        return;
      }
      const rental = await api.createReservation({
        customerId: user.customerId,
        vehicleId: booking.vehicleId,
        email: user.email,
        startDate: bookingDates.start,
        endDate: bookingDates.end,
      });
      setBooking(null);
      navigate('/rentals', { state: { highlight: rental.rentalId } });
    } catch (e) {
      if (e.status === 409) {
        setError('Ten pojazd jest już zarezerwowany w wybranym terminie. Wybierz inne daty lub inne auto.');
      } else {
        setError(e.message);
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="page-shell">
      <div className="hero-strip">
        <h1>Przeglądaj flotę</h1>
        <p className="page-lead">Filtruj po kategorii i cenie. Daty wypożyczenia wybierzesz przy rezerwacji.</p>
      </div>

      {error && <p className="error" role="alert">{error}</p>}

      <section className="card filters-row">
        <div className="form-group">
          <label>Status</label>
          <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="AVAILABLE">Dostępne</option>
            <option value="">Wszystkie</option>
            <option value="RESERVED">Zarezerwowane</option>
            <option value="RENTED">Wypożyczone</option>
          </select>
        </div>
        <div className="form-group">
          <label>Kategoria</label>
          <select value={filters.category} onChange={(e) => setFilters({ ...filters, category: e.target.value })}>
            <option value="">Wszystkie</option>
            <option value="ECONOMY">Economy</option>
            <option value="STANDARD">Standard</option>
            <option value="PREMIUM">Premium</option>
            <option value="SUV">SUV</option>
            <option value="VAN">Van</option>
          </select>
        </div>
        <div className="form-group">
          <label>Max cena / dobę (PLN)</label>
          <input type="number" min="0" placeholder="np. 200" value={filters.maxPrice} onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Od (opcjonalnie)</label>
          <input type="date" value={filters.start} onChange={(e) => setFilters({ ...filters, start: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Do (opcjonalnie)</label>
          <input type="date" value={filters.end} onChange={(e) => setFilters({ ...filters, end: e.target.value })} />
        </div>
        <button type="button" className="btn secondary" onClick={load}>
          Szukaj
        </button>
      </section>

      {loading && <p className="muted">Ładowanie…</p>}

      <div className="vehicle-grid">
        {vehicles.map((v) => (
          <article key={v.vehicleId} className="card vehicle-card">
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <h3>
                {v.brand} {v.model}
              </h3>
              <span className={`status-badge status-${v.status?.toLowerCase()}`}>{v.status}</span>
            </div>
            <p className="vehicle-meta">
              {v.year} · {v.category} · {api.formatPln(v.dailyRateMinorUnits)}/dobę
            </p>
            <span className="vehicle-plate">{v.licensePlate}</span>
            {v.status === 'AVAILABLE' && (
              <button type="button" className="btn primary" onClick={() => openBooking(v)}>
                Rezerwuj
              </button>
            )}
          </article>
        ))}
      </div>

      {booking && (
        <section className="card" style={{ marginTop: '20px' }}>
          <h2>
            Rezerwacja: {booking.brand} {booking.model}
          </h2>
          <p className="page-lead" style={{ marginTop: '4px' }}>
            Wybierz okres wypożyczenia — koszt zostanie policzony automatycznie.
          </p>
          <div className="filters-row" style={{ marginTop: '12px' }}>
            <div className="form-group">
              <label>Data rozpoczęcia</label>
              <input
                type="date"
                required
                min={todayIso()}
                value={bookingDates.start}
                onChange={(e) => setBookingDates({ ...bookingDates, start: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Data zakończenia</label>
              <input
                type="date"
                required
                min={bookingDates.start || todayIso()}
                value={bookingDates.end}
                onChange={(e) => setBookingDates({ ...bookingDates, end: e.target.value })}
              />
            </div>
          </div>
          {quote && (
            <p className="success">
              Szacowany koszt: {api.formatPln(quote.totalMinorUnits)} ({quote.rentalDays} dni)
            </p>
          )}
          <button type="button" className="btn primary" disabled={busy} onClick={reserve}>
            {busy ? '…' : 'Potwierdź rezerwację'}
          </button>
          <button
            type="button"
            className="btn secondary"
            style={{ marginLeft: '8px' }}
            onClick={() => {
              setBooking(null);
              setQuote(null);
            }}
          >
            Anuluj
          </button>
        </section>
      )}
    </main>
  );
}
