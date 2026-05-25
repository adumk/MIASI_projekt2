import * as api from '../api/rentalApi';

export function formatVehicleName(vehicle, vehicleId) {
  if (vehicle?.brand && vehicle?.model) {
    return `${vehicle.brand} ${vehicle.model}`;
  }
  return vehicleId || 'Nieznany pojazd';
}

export function formatVehicleMeta(vehicle) {
  if (!vehicle) return '';
  const parts = [vehicle.licensePlate, vehicle.category, vehicle.year].filter(Boolean);
  return parts.join(' · ');
}

export async function fetchVehicleMap() {
  const vehicles = await api.getVehicles({});
  return Object.fromEntries(vehicles.map((v) => [v.vehicleId, v]));
}

export function enrichRental(rental, vehicleMap) {
  const vehicle = vehicleMap[rental.vehicleId];
  return {
    ...rental,
    vehicleName: formatVehicleName(vehicle, rental.vehicleId),
    vehicleMeta: formatVehicleMeta(vehicle),
    vehicle,
  };
}
