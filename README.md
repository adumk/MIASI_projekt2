# Wypożyczalnia Samochodów — MiASI Projekt 2

System wypożyczalni samochodów zgodny z dokumentacją **DDD Strategiczne** i **DDD Taktyczne** (architektura heksagonalna, SOA, domain events).

Repozytorium: [github.com/adumk/MIASI_projekt2](https://github.com/adumk/MIASI_projekt2)

## Architektura

```
                    ┌─────────────────┐
                    │   API Gateway   │ :8080
                    └────────┬────────┘
         ┌──────────┼──────────┼──────────┐
         ▼          ▼          ▼          ▼
   rental-service  fleet-service  customer-service  billing-service
      :8081           :8082           :8083            :8084
         │              │               │                │
    PostgreSQL       MongoDB       PostgreSQL       PostgreSQL
         └──────────────┴───────────────┴────────────────┘
                              │
                         Kafka (car-events, billing-events)
                              │
                         Redis (cache)
```

## Moduły Maven

| Moduł | Bounded Context | Port | Baza |
|-------|-----------------|------|------|
| `common-events` | Kontrakty integracyjne (Kafka) | — | — |
| `rental-service` | BC-01 Rental Management | 8081 | PostgreSQL |
| `fleet-service` | BC-02 Fleet Management | 8082 | MongoDB |
| `customer-service` | BC-03 Customer Management | 8083 | PostgreSQL |
| `billing-service` | BC-04 Payment & Billing | 8084 | PostgreSQL |
| `api-gateway` | BFF / routing + JWT stub | 8080 | — |
| `notification-service` | Powiadomienia (Kafka stub) | 8085 | — |

## Warstwy heksagonalne (każdy serwis)

- `domain` — agregaty, VO, zdarzenia domenowe, invarianty
- `application` — use case handlers, commands/queries
- `ports` — interfejsy driven/driving
- `adapters` — REST, Kafka, JPA/Mongo, HTTP ACL
- `infrastructure` — Spring config, persistence mapping

## Przepływ zdarzeń

1. **Rezerwacja** — `POST /api/v1/reservations` → `ReservationCreated` → Fleet: `Vehicle.reserve()`
2. **Potwierdzenie przedpłaty** — `POST /api/v1/reservations/{id}/confirm`
3. **Wydanie** — `POST /api/v1/rentals/{id}/activate` → `CarRented` → Fleet: `Vehicle.rent()`
4. **Zwrot** — `POST /api/v1/rentals/{id}/return` → `CarReturned` → Billing: `CostCalculated` + faktura
5. **Anulowanie** — `POST /api/v1/reservations/{id}/cancel` → `RentalCancelled` → Fleet: zwolnienie rezerwacji
6. **Przeterminowanie** — scheduler `MarkOverdueUseCase` (cron)

## API (przez gateway :8080)

### Rental
- `POST /api/v1/reservations` — rezerwacja
- `POST /api/v1/reservations/{id}/confirm` — potwierdzenie przedpłaty
- `POST /api/v1/rentals/{id}/activate` — wydanie pojazdu
- `POST /api/v1/rentals/{id}/return` — zwrot
- `POST /api/v1/reservations/{id}/cancel` — anulowanie
- `GET /api/v1/rentals/{id}` — szczegóły wypożyczenia
- `GET /api/v1/customers/{id}/rentals` — historia klienta
- `GET /api/v1/admin/reports/rentals` — raport statusów (admin)

### Fleet
- `POST /api/v1/vehicles` — dodaj pojazd
- `GET /api/v1/vehicles?status=AVAILABLE&category=STANDARD` — wyszukiwanie
- `DELETE /api/v1/vehicles/{id}` — usuń z floty
- `GET /api/v1/vehicles/{id}` — szczegóły
- `PATCH /api/v1/vehicles/{id}/status` — zmiana statusu
- `POST /api/v1/vehicles/{id}/damage` — zgłoś uszkodzenie
- `POST /api/v1/vehicles/{id}/maintenance` — zaplanuj serwis
- `POST /api/v1/vehicles/{id}/maintenance/complete` — zakończ serwis

### Customer
- `POST /api/v1/customers` — rejestracja
- `GET /api/v1/customers/{id}` — profil
- `POST /api/v1/customers/{id}/verify` — weryfikacja
- `POST /api/v1/customers/{id}/block` — blokada
- `GET /api/v1/customers/{id}/can-rent` — ACL dla rental

### Billing
- `GET /api/v1/invoices/{rentalId}` — faktura
- `POST /api/v1/payments` — płatność

## Uruchomienie lokalne

### Testy jednostkowe (wszystkie moduły)

```bash
mvn test
```

### Infrastruktura (Docker)

```bash
docker compose up -d postgres mongo redis kafka
```

### Lokalnie bez Dockera (profil `local`)

Wymaga PostgreSQL (`localhost:5432`, hasło `3006` w `application-local.yml`).

```bash
./scripts/start-local.sh    # backend + gateway + frontend React
./scripts/stop-local.sh     # stop portów 5173, 8080-8084
```

**Aplikacja web (UI):** http://localhost:5173  
**API Gateway:** http://localhost:8080

Konta demo (profil `local`):

| Rola | E-mail | Hasło |
|------|--------|-------|
| Klient | `jan.kowalski@example.com` | `Haslo123!` |
| Pracownik | `pracownik@wypozyczalnia.pl` | `Haslo123!` |
| Admin | `admin@wypozyczalnia.pl` | `Haslo123!` |

### Role w aplikacji web

| Rola | Funkcje w UI |
|------|----------------|
| **CUSTOMER** | Rejestracja, logowanie, przegląd floty (filtry: data, kategoria, cena), rezerwacja, przedpłata, anulowanie, historia, płatność |
| **EMPLOYEE** | Weryfikacja klienta, wydanie/zwrot, inspekcja, uszkodzenia, zmiana statusu pojazdu |
| **ADMIN** | Flota (+/- pojazdy), taryfy, serwis, raporty |

System automatycznie: naliczanie kosztu, faktura, blokada zajętych aut, Kafka, scheduler OVERDUE, powiadomienia (stub).

### Serwisy (IDE lub Maven, pojedynczo)

```bash
mvn spring-boot:run -pl fleet-service -Dspring-boot.run.profiles=local
mvn spring-boot:run -pl customer-service -Dspring-boot.run.profiles=local
mvn spring-boot:run -pl billing-service -Dspring-boot.run.profiles=local
mvn spring-boot:run -pl rental-service -Dspring-boot.run.profiles=local
mvn spring-boot:run -pl api-gateway -Dspring-boot.run.profiles=local
```

### Pełny stack Docker

```bash
docker compose up --build
```

## Język wszechobecny

Terminy zgodne z dokumentacją: `Vehicle`, `Rental`, `Customer`, `CarRented`, `CarReturned`, `CostCalculated` — bez synonimów (`Auto`, `Umowa`, `Użytkownik`).

## Gateway — autoryzacja (stub)

Nagłówek: `Authorization: Bearer <token>` lub `X-API-Key: miasi-dev-key`

## Checklist wymagań

Szczegółowa lista: [REQUIREMENTS.md](REQUIREMENTS.md)

## Branch

Praca na branchu: `feature/rental-service-implementation`
