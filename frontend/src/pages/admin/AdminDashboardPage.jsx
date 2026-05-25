import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import * as api from '../../api/rentalApi';

export default function AdminDashboardPage() {
  const [report, setReport] = useState(null);

  useEffect(() => {
    api.getAdminReport().then(setReport).catch(() => setReport(null));
  }, []);

  return (
    <main className="page-shell">
      <h1>Panel administratora</h1>
      <p className="page-lead">Zarządzanie flotą, taryfami, serwisem i raportami.</p>

      <div className="vehicle-grid">
        <Link to="/admin/fleet" className="card vehicle-card" style={{ textDecoration: 'none', color: 'inherit' }}>
          <h3>Flota</h3>
          <p className="muted">Dodawanie i usuwanie pojazdów</p>
        </Link>
        <Link to="/admin/tariffs" className="card vehicle-card" style={{ textDecoration: 'none', color: 'inherit' }}>
          <h3>Taryfy</h3>
          <p className="muted">Stawki dzienne per kategoria</p>
        </Link>
        <Link to="/admin/maintenance" className="card vehicle-card" style={{ textDecoration: 'none', color: 'inherit' }}>
          <h3>Serwis</h3>
          <p className="muted">Planowanie i zakończenie serwisu</p>
        </Link>
      </div>

      {report && (
        <section className="card">
          <h2>Raport wypożyczeń</h2>
          <div className="report-grid">
            {Object.entries(report).map(([status, count]) => (
              <div key={status} className="report-tile">
                <strong>{count}</strong>
                <span className="vehicle-meta">{status}</span>
              </div>
            ))}
          </div>
        </section>
      )}
    </main>
  );
}
