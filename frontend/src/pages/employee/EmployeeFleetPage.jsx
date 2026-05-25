import { useEffect, useState } from 'react';
import * as api from '../../api/rentalApi';
import { formatVehicleMeta, formatVehicleName } from '../../utils/vehicles';

const SEVERITIES = ['MINOR', 'MODERATE', 'SEVERE'];
const CATEGORIES = ['ECONOMY', 'STANDARD', 'PREMIUM', 'SUV', 'VAN'];

export default function EmployeeFleetPage() {
  const [vehicles, setVehicles] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [form, setForm] = useState({
    licensePlate: '',
    brand: '',
    model: '',
    year: new Date().getFullYear(),
    category: 'STANDARD',
  });
  const [damage, setDamage] = useState({ description: '', severity: 'MODERATE' });
  const [newStatus, setNewStatus] = useState('AVAILABLE');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);

  const selected = vehicles.find((v) => v.vehicleId === selectedId);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const list = await api.getVehicles({});
      setVehicles(list);
      if (selectedId && !list.some((v) => v.vehicleId === selectedId)) {
        setSelectedId(null);
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    if (selected) setNewStatus(selected.status);
  }, [selected]);

  const addVehicle = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.addVehicle(form);
      setSuccess('Pojazd dodany do floty.');
      setForm({
        licensePlate: '',
        brand: '',
        model: '',
        year: new Date().getFullYear(),
        category: 'STANDARD',
      });
      setShowAddForm(false);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const reportDamage = async () => {
    if (!selected) return;
    try {
      await api.reportDamage(selected.vehicleId, damage.description, damage.severity);
      setSuccess('Uszkodzenie zgłoszone.');
      setDamage({ description: '', severity: 'MODERATE' });
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  const changeStatus = async () => {
    if (!selected) return;
    try {
      await api.updateVehicleStatus(selected.vehicleId, newStatus);
      setSuccess(`Status: ${newStatus}`);
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  const removeVehicle = async () => {
    if (!selected) return;
    if (!confirm(`Usunąć ${formatVehicleName(selected)} z floty?`)) return;
    setError('');
    try {
      await api.removeVehicle(selected.vehicleId);
      setSuccess('Pojazd usunięty z floty.');
      setSelectedId(null);
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <main className="page-shell">
      <div className="hero-strip" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h1>Flota pojazdów</h1>
          <p className="page-lead">Lista aut, dodawanie i usuwanie, uszkodzenia i zmiana statusu.</p>
        </div>
        <button type="button" className="btn primary" onClick={() => setShowAddForm((v) => !v)}>
          {showAddForm ? 'Anuluj' : '+ Dodaj pojazd'}
        </button>
      </div>

      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      {showAddForm && (
        <section className="card">
          <h2>Nowy pojazd</h2>
          <form className="form" onSubmit={addVehicle}>
            <div className="filters-row">
              <div className="form-group">
                <label>Tablica rejestracyjna</label>
                <input required placeholder="np. WA12345" value={form.licensePlate} onChange={(e) => setForm({ ...form, licensePlate: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Marka</label>
                <input required placeholder="Toyota" value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Model</label>
                <input required placeholder="Corolla" value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Rok</label>
                <input type="number" min="1990" max="2030" value={form.year} onChange={(e) => setForm({ ...form, year: Number(e.target.value) })} />
              </div>
              <div className="form-group">
                <label>Kategoria</label>
                <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                  {CATEGORIES.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <button type="submit" className="btn primary">
              Zapisz pojazd
            </button>
          </form>
        </section>
      )}

      <div className="fleet-layout">
        <section className="card">
          <h2>Pojazdy ({vehicles.length})</h2>
          {loading && <p className="muted">Ładowanie…</p>}
          {!loading && vehicles.length === 0 && <p className="muted">Brak pojazdów — dodaj pierwszy przyciskiem +.</p>}
          <ul className="fleet-picker-list">
            {vehicles.map((v) => (
              <li key={v.vehicleId}>
                <button
                  type="button"
                  className={`fleet-picker-item ${selectedId === v.vehicleId ? 'active' : ''}`}
                  onClick={() => setSelectedId(v.vehicleId)}
                >
                  <span className="fleet-picker-title">
                    {formatVehicleName(v)} <span className={`status-badge status-${v.status?.toLowerCase()}`}>{v.status}</span>
                  </span>
                  <span className="vehicle-meta">{formatVehicleMeta(v)}</span>
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="card">
          {!selected && <p className="muted">Wybierz pojazd z listy, aby zgłosić uszkodzenie lub zmienić status.</p>}
          {selected && (
            <>
              <h2>
                {formatVehicleName(selected)}
              </h2>
              <p className="vehicle-meta">{formatVehicleMeta(selected)} · {api.formatPln(selected.dailyRateMinorUnits)}/dobę</p>
              <button type="button" className="btn secondary" style={{ marginTop: '12px' }} onClick={removeVehicle}>
                Usuń pojazd z floty
              </button>

              <h3 style={{ marginTop: '20px' }}>Zgłoś uszkodzenie</h3>
              <div className="form">
                <label>Opis</label>
                <textarea rows={2} value={damage.description} onChange={(e) => setDamage({ ...damage, description: e.target.value })} />
                <label>Poziom</label>
                <select value={damage.severity} onChange={(e) => setDamage({ ...damage, severity: e.target.value })}>
                  {SEVERITIES.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
                <button type="button" className="btn primary" onClick={reportDamage}>
                  Zgłoś uszkodzenie
                </button>
              </div>

              <h3 style={{ marginTop: '24px' }}>Zmiana statusu</h3>
              <div className="form">
                <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
                  <option value="AVAILABLE">Dostępny</option>
                  <option value="MAINTENANCE">Serwis</option>
                  <option value="DAMAGED">Uszkodzony</option>
                  <option value="RESERVED">Zarezerwowany</option>
                  <option value="RENTED">Wypożyczony</option>
                </select>
                <button type="button" className="btn secondary" onClick={changeStatus}>
                  Zastosuj
                </button>
              </div>
            </>
          )}
        </section>
      </div>
    </main>
  );
}
