# Zgodność z wymaganiami projektu

## Architektura SOA

| Serwis | Port | Odpowiedzialność |
|--------|------|------------------|
| rental-service | 8081 | Rezerwacje, wypożyczenia, FSM Rental |
| fleet-service | 8082 | Pojazdy, FSM Vehicle, uszkodzenia, serwis |
| customer-service | 8083 | Klienci, JWT, weryfikacja |
| billing-service | 8084 | Taryfy, koszt, faktury, płatności |
| notification-service | 8085 | Powiadomienia (stub email/SMS) |
| api-gateway | 8080 | Proxy, JWT, role |

Komunikacja: **Kafka** (`car-events`, `billing-events`) + **REST** (sync: weryfikacja, wycena, dostępność). Profil `local` używa HTTP `/internal/integration-events` zamiast Kafki.

## Maszyny stanów

### Vehicle (fleet-service)
`AVAILABLE` → `RESERVED` (rezerwacja) → `RENTED` (wydanie) → `AVAILABLE` (zwrot)  
Odgałęzienia: `DAMAGED` → `MAINTENANCE` → `AVAILABLE`  
Ręczna zmiana: `PATCH /vehicles/{id}/status` (pracownik/admin)

### Rental (rental-service)
`RESERVED` → `ACTIVE` → `COMPLETED`  
`RESERVED` → `CANCELLED`  
`ACTIVE` → `OVERDUE` (scheduler) → `COMPLETED`  
Rozliczenie: `settlementClosed` po `PaymentConfirmed`

## Zdarzenia domenowe

| Zdarzenie | Publikacja | Konsumenci / efekt |
|-----------|------------|-------------------|
| ReservationCreated | rental → car-events | fleet: reserve; notification: stub email |
| CarRented | rental → car-events | fleet: RENTED; billing: start sesji rozliczenia |
| CarReturned | rental → car-events | fleet: AVAILABLE; billing: CalculateCost |
| DamageReported | fleet → car-events | billing: opłata za szkodę (MINOR/MODERATE/SEVERE) |
| CostCalculated | billing → billing-events | billing: GenerateInvoice |
| PaymentConfirmed | billing → billing-events | rental: closeSettlement |

## Aktorzy — status

Wszystkie funkcje z listy wymagań są zaimplementowane w backendzie i UI (role: CUSTOMER, EMPLOYEE, ADMIN).

Szczegóły endpointów: `ROLES.md`.
