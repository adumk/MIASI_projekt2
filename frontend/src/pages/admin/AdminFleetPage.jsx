import { useEffect, useState } from 'react';
import * as api from '../../api/rentalApi';

export default function AdminFleetPage() {
  const [vehicles, setVehicles] = useState([]);
  const [form, setForm] = useState({
    licensePlate: '',
    brand: '',
    model: '',
    year: 2024,
    category: 'STANDARD',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = () => api.getVehicles({}).then(setVehicles).catch((e) => setError(e.message));

  useEffect(() => {
    load();
  }, []);

  const add = async (e) => {
    e.preventDefault();
    try {
      await api.addVehicle(form);
      setSuccess('Pojazd dodany.');
      setForm({ licensePlate: '', brand: '', model: '', year: 2024, category: 'STANDARD' });
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const remove = async (id) => {
    if (!confirm('Usunąć pojazd z floty?')) return;
    try {
      await api.removeVehicle(id);
      setSuccess('Usunięto.');
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <main className="page-shell">
      <h1>Zarządzanie flotą</h1>
      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      <section className="card">
        <h2>Dodaj pojazd</h2>
        <form className="form" onSubmit={add}>
          <input placeholder="Tablica" required value={form.licensePlate} onChange={(e) => setForm({ ...form, licensePlate: e.target.value })} />
          <input placeholder="Marka" required value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} />
          <input placeholder="Model" required value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} />
          <input type="number" placeholder="Rok" value={form.year} onChange={(e) => setForm({ ...form, year: Number(e.target.value) })} />
          <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
            {['ECONOMY', 'STANDARD', 'PREMIUM', 'SUV', 'VAN'].map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
          <button type="submit" className="btn primary">
            Dodaj
          </button>
        </form>
      </section>

      <section className="card">
        <h2>Lista pojazdów</h2>
        <ul>
          {vehicles.map((v) => (
            <li key={v.vehicleId} style={{ marginBottom: '8px', display: 'flex', justifyContent: 'space-between' }}>
              <span>
                {v.brand} {v.model} ({v.licensePlate}) — {v.status}
              </span>
              <button type="button" className="btn secondary" onClick={() => remove(v.vehicleId)}>
                Usuń
              </button>
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}
