import { useCallback, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import * as api from '../api/rentalApi';
import { useAuth } from '../context/AuthContext';
import { enrichRental, fetchVehicleMap } from '../utils/vehicles';
import { rentalStatusHint, rentalStatusLabel } from '../utils/rentalStatus';

function StatusBadge({ status }) {
  const s = (status || 'pending').toLowerCase();
  return (
    <span className={`rental-status rental-status-${s}`}>
      <span className="rental-status-dot" aria-hidden />
      {rentalStatusLabel(status)}
    </span>
  );
}

const INVOICE_STATUS_LABELS = {
  DRAFT: 'W przygotowaniu',
  COST_CALCULATED: 'Koszt policzony',
  ISSUED: 'Do zapłaty',
  PAID: 'Opłacona',
  REFUNDED: 'Zwrócona',
};

export default function RentalsPage() {
  const { user } = useAuth();
  const location = useLocation();
  const [rentals, setRentals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState('');
  const [invoices, setInvoices] = useState({});

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [list, map] = await Promise.all([
        api.getCustomerRentals(user.customerId),
        fetchVehicleMap().catch(() => ({})),
      ]);
      setRentals((Array.isArray(list) ? list : []).map((r) => enrichRental(r, map)));
    } catch (e) {
      if (e.status === 500) {
        setError('Serwis wypożyczeń jest niedostępny. Uruchom ponownie: ./scripts/start-local.sh');
      } else {
        setError(e.message || 'Nie udało się pobrać wypożyczeń.');
      }
      setRentals([]);
    } finally {
      setLoading(false);
    }
  }, [user.customerId]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (location.state?.highlight) {
      setSuccess('Rezerwacja utworzona pomyślnie.');
      window.history.replaceState({}, '');
    }
  }, [location.state]);

  const loadInvoice = async (rentalId) => {
    setError('');
    setBusy(`${rentalId}-invoice`);
    try {
      const inv = await api.getInvoice(rentalId);
      setInvoices((p) => ({ ...p, [rentalId]: inv }));
    } catch (e) {
      if (e.status === 404) {
        setError('Faktura nie jest jeszcze gotowa. Odśwież za chwilę po zwrocie pojazdu.');
      } else if (e.status === 409) {
        setError('Faktura jest w przygotowaniu — spróbuj ponownie za kilka sekund.');
      } else {
        setError(e.message || 'Nie udało się pobrać faktury.');
      }
    } finally {
      setBusy('');
    }
  };

  const run = async (id, action) => {
    setBusy(`${id}-${action}`);
    setError('');
    setSuccess('');
    try {
      let rental;
      if (action === 'confirm') rental = await api.confirmReservation(id);
      else if (action === 'return') {
        rental = await api.returnRental(id, {
          actualReturnDate: new Date().toISOString().slice(0, 10),
          mileage: null,
          inspectionNotes: null,
        });
      } else if (action === 'cancel') rental = await api.cancelReservation(id);
      setSuccess(
        action === 'confirm'
          ? 'Przedpłata potwierdzona. Odbierz pojazd w wypożyczalni.'
          : action === 'return'
            ? 'Zwrot zarejestrowany. Faktura pojawi się za chwilę.'
            : `Status: ${rental?.status}`
      );
      await load();
      if (action === 'return') {
        setTimeout(() => loadInvoice(id), 800);
      }
    } catch (e) {
      if (e.status === 409) {
        setError('Ten pojazd jest już zarezerwowany w wybranym terminie.');
      } else {
        setError(e.message);
      }
    } finally {
      setBusy('');
    }
  };

  const actions = (r) => {
    const list = [];
    if (r.status === 'RESERVED' && !r.paymentConfirmed) {
      list.push({ key: 'confirm', label: 'Opłać przedpłatę', primary: true });
      list.push({ key: 'cancel', label: 'Anuluj rezerwację', danger: true });
    }
    if (r.status === 'RESERVED' && r.paymentConfirmed) {
      list.push({ key: 'cancel', label: 'Anuluj rezerwację', danger: true });
    }
    if (r.status === 'ACTIVE' || r.status === 'OVERDUE') {
      list.push({ key: 'return', label: 'Zgłoś zwrot pojazdu', primary: true });
    }
    if (r.status === 'COMPLETED') {
      list.push({ key: 'invoice', label: 'Pokaż fakturę', secondary: true });
    }
    return list;
  };

  return (
    <main className="page-shell">
      <h1>Moje wypożyczenia</h1>
      <p className="page-lead">
        Rezerwacja → przedpłata → odbiór w wypożyczalni → jazda → zwrot → faktura.
      </p>

      {error && <p className="error" role="alert">{error}</p>}
      {success && <p className="success" role="status">{success}</p>}

      <button type="button" className="btn secondary" onClick={load} style={{ marginBottom: '16px' }}>
        Odśwież
      </button>

      {loading && <p className="muted">Ładowanie…</p>}
      {!loading && rentals.length === 0 && (
        <section className="card centered">
          <p className="muted">Brak wypożyczeń. Zarezerwuj pojazd w zakładce Flota.</p>
        </section>
      )}

      <div className="rental-list-grid">
        {rentals.map((r) => {
          const hint = rentalStatusHint(r);
          return (
            <article
              key={r.rentalId}
              className="card rental-card"
              style={
                location.state?.highlight === r.rentalId
                  ? { outline: '2px solid var(--color-accent)' }
                  : undefined
              }
            >
              <div className="rental-card-header">
                <div>
                  <h3 className="rental-vehicle-title">{r.vehicleName}</h3>
                  {r.vehicleMeta && <p className="vehicle-meta">{r.vehicleMeta}</p>}
                  <p className="vehicle-meta">
                    {r.periodStart} → {r.periodEnd}
                  </p>
                </div>
                <StatusBadge status={r.status} />
              </div>

              {hint && <p className="rental-step-hint">{hint}</p>}

              <div className="rental-card-actions">
                {actions(r).map((a) => (
                  <button
                    key={a.key}
                    type="button"
                    className={`btn ${a.primary ? 'primary' : a.danger ? 'danger' : 'secondary'}`}
                    disabled={!!busy}
                    onClick={() => (a.key === 'invoice' ? loadInvoice(r.rentalId) : run(r.rentalId, a.key))}
                  >
                    {busy === `${r.rentalId}-${a.key}` ? '…' : a.label}
                  </button>
                ))}
              </div>

              {invoices[r.rentalId] && (
                <div className="invoice-panel">
                  <p className="vehicle-meta">
                    <strong>Faktura:</strong> {(invoices[r.rentalId].amount / 100).toFixed(2)}{' '}
                    {invoices[r.rentalId].currency} · {invoices[r.rentalId].rentalDays} dni ·{' '}
                    {INVOICE_STATUS_LABELS[invoices[r.rentalId].status] || invoices[r.rentalId].status}
                  </p>
                  {invoices[r.rentalId].status !== 'PAID' && (
                    <button
                      type="button"
                      className="btn primary"
                      onClick={() =>
                        api
                          .payInvoice(r.rentalId, invoices[r.rentalId].amount, invoices[r.rentalId].currency)
                          .then(() => {
                            setSuccess('Płatność zaksięgowana');
                            loadInvoice(r.rentalId);
                          })
                          .catch((e) => setError(e.message))
                      }
                    >
                      Zapłać fakturę
                    </button>
                  )}
                </div>
              )}
            </article>
          );
        })}
      </div>
    </main>
  );
}
