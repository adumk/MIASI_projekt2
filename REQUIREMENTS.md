# Checklist wymagań MiASI / DDD

## Strategia DDD

| Wymaganie | Status |
|-----------|--------|
| BC-01 Rental Management | ✅ `rental-service` |
| BC-02 Fleet Management | ✅ `fleet-service` |
| BC-03 Customer Management | ✅ `customer-service` |
| BC-04 Payment & Billing | ✅ `billing-service` |
| Język wszechobecny (Vehicle, Rental, Customer…) | ✅ |
| Context Map — zdarzenia + ACL | ✅ Kafka + HTTP ACL |
| Core / Supporting / Generic subdomains | ✅ opis w README |

## Taktyka DDD / Heksagon

| Wymaganie | Status |
|-----------|--------|
| Warstwy domain / application / ports / adapters / infrastructure | ✅ wszystkie serwisy |
| Agregat Rental + maszyna stanów | ✅ |
| Agregat Vehicle + RESERVED/RENTED/… | ✅ |
| Agregat Customer | ✅ |
| Agregat Invoice / Payment | ✅ |
| Value Objects (DateRange, Money, Email…) | ✅ |
| Domain Events (czas przeszły) | ✅ |
| ArchUnit — reguły heksagonu | ✅ |
| SOLID / CQS / Tell Don't Ask | ✅ |

## Use cases (PDF §7)

| Use case | Implementacja |
|----------|---------------|
| CreateReservation | ✅ |
| ConfirmReservation | ✅ `POST …/reservations/{id}/confirm` |
| CancelReservation | ✅ |
| RentVehicle | ✅ |
| ReturnVehicle | ✅ |
| SearchVehicles | ✅ `GET /vehicles?status=&category=` |
| GetRentalHistory | ✅ |
| AddVehicle | ✅ |
| RemoveVehicle | ✅ `DELETE /vehicles/{id}` |
| ReportDamage | ✅ |
| UpdateVehicleStatus | ✅ |
| ScheduleMaintenance | ✅ |
| CompleteMaintenance | ✅ |
| CalculateCost | ✅ (Kafka CarReturned) |
| GenerateInvoice | ✅ |
| ProcessPayment | ✅ |
| MarkOverdue | ✅ scheduler cron |
| SendNotification | ✅ `notification-service` (stub email/SMS) |
| GenerateReport | ✅ `GET /admin/reports/rentals` |

## Integracja

| Wymaganie | Status |
|-----------|--------|
| Kafka `car-events` | ✅ |
| Kafka `billing-events` | ✅ |
| Fleet ← ReservationCreated, CarRented, CarReturned, RentalCancelled | ✅ |
| Billing ← CarReturned, RentalCancelled (RefundIssued) | ✅ |
| Rental → Fleet HTTP (dostępność) | ✅ |
| Rental → Customer HTTP (can-rent) | ✅ |
| API Gateway routing | ✅ |
| API Gateway JWT | ✅ |
| Rejestracja / logowanie (frontend + `/api/v1/auth`) | ✅ |
| PostgreSQL (rental, customer, billing) | ✅ |
| MongoDB (fleet) | ✅ |
| Redis w docker-compose | ✅ (infrastruktura) |
| Docker Compose | ✅ |

## Poza zakresem (świadomie)

- Frontend React (`frontend/`) — UI do demonstracji systemu (Vite + React)
- Kubernetes / Helm — opcjonalnie na produkcję
- Prawdziwy provider płatności (Stripe) — stub `ProcessPayment`
