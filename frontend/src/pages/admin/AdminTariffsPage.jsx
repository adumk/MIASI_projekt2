import { useEffect, useState } from 'react';
import * as api from '../../api/rentalApi';

export default function AdminTariffsPage() {
  const [tariffs, setTariffs] = useState({});
  const [edits, setEdits] = useState({});
  const [msg, setMsg] = useState('');

  const load = () => api.getTariffs().then((t) => {
    setTariffs(t);
    setEdits(Object.fromEntries(Object.entries(t).map(([k, v]) => [k, String(v / 100)])));
  });

  useEffect(() => {
    load();
  }, []);

  const save = async (category) => {
    const pln = Number(edits[category]);
    if (!pln || pln <= 0) return;
    await api.updateTariff(category, Math.round(pln * 100));
    setMsg(`Zapisano taryfę ${category}`);
    load();
  };

  return (
    <main className="page-shell">
      <h1>Taryfy cenowe</h1>
      <p className="page-lead">Stawki dzienne (PLN) — używane przy naliczaniu kosztu i wycenie.</p>
      {msg && <p className="success">{msg}</p>}

      <section className="card">
        {Object.keys(tariffs).map((cat) => (
          <div key={cat} style={{ display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '12px' }}>
            <strong style={{ width: '100px' }}>{cat}</strong>
            <input
              type="number"
              step="0.01"
              style={{ width: '120px' }}
              value={edits[cat] ?? ''}
              onChange={(e) => setEdits({ ...edits, [cat]: e.target.value })}
            />
            <span className="muted">PLN / dobę</span>
            <button type="button" className="btn secondary" onClick={() => save(cat)}>
              Zapisz
            </button>
          </div>
        ))}
      </section>
    </main>
  );
}
