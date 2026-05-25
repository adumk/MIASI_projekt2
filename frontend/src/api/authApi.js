import { apiFetch } from './client';

export function loginUser(body) {
  return apiFetch('/auth/login', { method: 'POST', body: JSON.stringify(body) });
}

export function registerUser(body) {
  return apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(body) });
}

export function getCurrentUser() {
  return apiFetch('/auth/me');
}
