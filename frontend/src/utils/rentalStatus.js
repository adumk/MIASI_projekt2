const STATUS_LABELS = {
  RESERVED: 'Zarezerwowane',
  ACTIVE: 'W trakcie',
  OVERDUE: 'Po terminie',
  COMPLETED: 'Zakończone',
  CANCELLED: 'Anulowane',
};

export function rentalStatusLabel(status) {
  return STATUS_LABELS[status] || status;
}

export function rentalStatusHint(rental) {
  if (rental.status === 'RESERVED' && !rental.paymentConfirmed) {
    return 'Krok 1/2: opłać przedpłatę, aby potwierdzić rezerwację.';
  }
  if (rental.status === 'RESERVED' && rental.paymentConfirmed) {
    return 'Krok 2/2: odbierz pojazd w wypożyczalni (pracownik wyda auto).';
  }
  if (rental.status === 'ACTIVE' || rental.status === 'OVERDUE') {
    return 'Po zakończeniu jazdy zgłoś zwrot pojazdu.';
  }
  if (rental.status === 'COMPLETED') {
    return 'Pobierz fakturę i ureguluj płatność końcową.';
  }
  return '';
}
