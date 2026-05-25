import { useState } from 'react';
import * as api from '../../api/rentalApi';

export default function AdminMaintenancePage() {
  const [vehicleId, setVehicleId] = useState('');
  const [vehicle, setVehicle] = useState(null);
  const [msg, setMsg] = useState('');

  const load = async () => {
    setVehicle(await api.getVehicle(vehicleId.trim()));
  };

  const schedule = async () => {
    await api.scheduleMaintenance(vehicle.vehicleId);
    setMsg('Serwis zaplanowany (MAINTENANCE).');
    load();
  };

  const complete = async () => {
    await api.completeMaintenance(vehicle.vehicleId);
    setMsg('Serwis zakończony (AVAILABLE).');
    load();
  };

  return (
    <main className="page-shell">
      <h1>Planowanie serwisu</h1>
      <section className="card">
        <label>ID pojazdu</label>
        <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
          <input style={{ flex: 1 }} value={vehicleId} onChange={(e) => setVehicleId(e.target.value)} />
          <button type="button" className="btn primary" onClick={load}>
            Wczytaj
          </button>
        </div>
      </section>
      {msg && <p className="success">{msg}</p>}
      {vehicle && (
        <section className="card">
          <p>
            {vehicle.brand} {vehicle.model} — status: <strong>{vehicle.status}</strong>
          </p>
          <button type="button" className="btn primary" onClick={schedule}>
            Zaplanuj serwis
          </button>
          <button type="button" className="btn secondary" style={{ marginLeft: '8px' }} onClick={complete}>
            Zakończ serwis
          </button>
        </section>
      )}
    </main>
  );
}
