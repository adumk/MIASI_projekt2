import { apiFetch } from './client';

export const getVehicles = (params = {}) => {
  const q = new URLSearchParams();
  if (params.status) q.set('status', params.status);
  if (params.category) q.set('category', params.category);
  if (params.maxDailyRateMinorUnits) q.set('maxDailyRateMinorUnits', params.maxDailyRateMinorUnits);
  const query = q.toString();
  return apiFetch(`/vehicles${query ? `?${query}` : ''}`);
};

export const getBusyVehicleIds = (startDate, endDate) => {
  const q = new URLSearchParams({ startDate, endDate });
  return apiFetch(`/availability/busy-vehicles?${q}`);
};

export const getVehicle = (id) => apiFetch(`/vehicles/${id}`);
export const addVehicle = (body) => apiFetch('/vehicles', { method: 'POST', body: JSON.stringify(body) });
export const removeVehicle = (id) => apiFetch(`/vehicles/${id}`, { method: 'DELETE' });
export const updateVehicleStatus = (id, status) =>
  apiFetch(`/vehicles/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });
export const reportDamage = (id, description, severity) =>
  apiFetch(`/vehicles/${id}/damage`, { method: 'POST', body: JSON.stringify({ description, severity }) });
export const scheduleMaintenance = (id) => apiFetch(`/vehicles/${id}/maintenance`, { method: 'POST' });
export const completeMaintenance = (id) => apiFetch(`/vehicles/${id}/maintenance/complete`, { method: 'POST' });

export const getQuote = (category, startDate, endDate) => {
  const q = new URLSearchParams({ category, startDate, endDate });
  return apiFetch(`/quotes?${q}`);
};

export const getTariffs = () => apiFetch('/tariffs');
export const updateTariff = (category, dailyRateMinorUnits) =>
  apiFetch(`/tariffs/${category}`, { method: 'PUT', body: JSON.stringify({ dailyRateMinorUnits }) });

export const getCustomer = (id) => apiFetch(`/customers/${id}`);
export const canRent = (id) => apiFetch(`/customers/${id}/can-rent`);
export const verifyCustomer = (id) => apiFetch(`/customers/${id}/verify`, { method: 'POST' });

export const getRentals = (status) => {
  const q = status ? `?status=${status}` : '';
  return apiFetch(`/rentals${q}`);
};
export const getRental = (id) => apiFetch(`/rentals/${id}`);
export const getCustomerRentals = (customerId) => apiFetch(`/customers/${customerId}/rentals`);

export const createReservation = (body) =>
  apiFetch('/reservations', { method: 'POST', body: JSON.stringify(body) });
export const confirmReservation = (id) => apiFetch(`/reservations/${id}/confirm`, { method: 'POST' });
export const activateRental = (id) => apiFetch(`/rentals/${id}/activate`, { method: 'POST' });
export const returnRental = (id, body) => {
  const payload =
    typeof body === 'string'
      ? { actualReturnDate: body, mileage: null, inspectionNotes: null }
      : body;
  return apiFetch(`/rentals/${id}/return`, { method: 'POST', body: JSON.stringify(payload) });
};
export const cancelReservation = (id) => apiFetch(`/reservations/${id}/cancel`, { method: 'POST' });

export const getInvoice = (rentalId) => apiFetch(`/invoices/${rentalId}`);
export const payInvoice = (rentalId, amount, currency = 'PLN') =>
  apiFetch('/payments', { method: 'POST', body: JSON.stringify({ rentalId, amount, currency }) });

export const getAdminReport = () => apiFetch('/admin/reports/rentals');

export const formatPln = (minor) => `${(minor / 100).toFixed(2)} PLN`;
