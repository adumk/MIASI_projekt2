# Role i funkcje — mapowanie wymagań

## Klient (CUSTOMER)

| Wymaganie | UI | API |
|-----------|-----|-----|
| Rejestracja i logowanie (JWT) | `/register`, `/login` | `POST /auth/register`, `/auth/login` |
| Przeglądanie pojazdów (data, kategoria, cena) | `/` + filtry + `getBusyVehicleIds` | `GET /vehicles`, `GET /availability/busy-vehicles`, `GET /quotes` |
| Rezerwacja | Rezerwuj | `POST /reservations` (+ walidacja nakładania terminów) |
| Potwierdzenie + przedpłata | `/rentals` | `POST /reservations/{id}/confirm` |
| Anulowanie | `/rentals` | `POST /reservations/{id}/cancel` (tylko RESERVED) |
| Odbiór (aktywacja) | `/rentals` lub stanowisko | `POST /rentals/{id}/activate` |
| Zwrot + rozliczenie | `/rentals` + faktura + płatność | `POST /rentals/{id}/return`, `GET /invoices`, `POST /payments` |
| Historia | `/rentals` | `GET /customers/{id}/rentals` (gateway → rental-service) |

## Pracownik (EMPLOYEE)

| Wymaganie | UI | API |
|-----------|-----|-----|
| Weryfikacja tożsamości | `/employee` | `POST /customers/{id}/verify` |
| Wydanie pojazdu | `/employee` | `POST /rentals/{id}/activate` → `CarRented` → fleet RENTED |
| Zwrot + inspekcja | `/employee` | `POST /rentals/{id}/return` (mileage, inspectionNotes) |
| Flota +/− | `/employee/fleet` | `POST/DELETE /vehicles` |
| Zgłoszenie uszkodzenia | `/employee/fleet` | `POST /vehicles/{id}/damage` → `DamageReported` |
| Zmiana statusu | `/employee/fleet` | `PATCH /vehicles/{id}/status` |

## Administrator (ADMIN)

| Wymaganie | UI | API |
|-----------|-----|-----|
| Flota +/- | `/admin/fleet` | `POST/DELETE /vehicles` |
| Taryfy | `/admin/tariffs` | `GET/PUT /tariffs/{category}` |
| Serwis | `/admin/maintenance` | `POST .../maintenance`, `.../complete` |
| Raporty | `/admin` | `GET /admin/reports/rentals` |

## System (automatycznie)

| Wymaganie | Implementacja |
|-----------|----------------|
| Naliczanie kosztu | `TariffCalculator` + `CalculateCostUseCase` na `CarReturned` |
| Faktura | `CostCalculated` → `GenerateInvoiceUseCase` |
| Blokada aut | status floty + `hasOverlappingBooking` w rental |
| Polityka anulowania | `Rental.cancel()` tylko z RESERVED |
| Powiadomienia | `notification-service` (stub log) |
| OVERDUE | `MarkOverdueUseCase` + scheduler |

## Zdarzenia (Kafka / local HTTP bridge)

`ReservationCreated` → fleet reserve, notification  
`CarRented` → fleet RENTED, billing start sesji  
`CarReturned` → fleet AVAILABLE, billing koszt  
`DamageReported` → opłata za szkodę w billing  
`CostCalculated` → faktura  
`PaymentConfirmed` → `settlementClosed` w rental  

Szczegóły architektury: `REQUIREMENTS_COMPLIANCE.md`.
