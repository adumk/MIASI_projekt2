#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
LOGDIR="${MIASI_LOG_DIR:-/tmp/miasi-logs}"
mkdir -p "$LOGDIR"
export PGPASSWORD="${PGPASSWORD:-3006}"
PROFILE="-Dspring-boot.run.profiles=local"

echo "=== PostgreSQL: bazy i migracje ==="
for db in rental_db customer_db billing_db; do
  psql -h localhost -U postgres -d postgres -tc "SELECT 1 FROM pg_database WHERE datname='$db'" 2>/dev/null | grep -q 1 \
    || psql -h localhost -U postgres -d postgres -c "CREATE DATABASE $db;" 2>/dev/null
done
psql -h localhost -U postgres -d rental_db -f rental-service/src/main/resources/db/migration/V1__create_rentals_table.sql -q 2>/dev/null || true
psql -h localhost -U postgres -d rental_db -f rental-service/src/main/resources/db/migration/V2__add_payment_confirmed.sql -q 2>/dev/null || true
psql -h localhost -U postgres -d rental_db -f rental-service/src/main/resources/db/migration/V3__add_return_inspection_fields.sql -q 2>/dev/null || true
psql -h localhost -U postgres -d customer_db -f customer-service/src/main/resources/db/migration/V1__create_customers_table.sql -q 2>/dev/null || true
psql -h localhost -U postgres -d customer_db -f customer-service/src/main/resources/db/migration/V2__add_auth_columns.sql -q 2>/dev/null || true
psql -h localhost -U postgres -d billing_db -f billing-service/src/main/resources/db/migration/V1__create_invoices_and_payments.sql -q 2>/dev/null || true

echo "=== Zatrzymanie starych procesów (8080-8084) ==="
for p in 8080 8081 8082 8083 8084; do
  lsof -ti :$p 2>/dev/null | xargs kill -9 2>/dev/null || true
done

echo "=== Kompilacja ==="
mvn -q install -N
mvn -q install -pl common-events -DskipTests
mvn -q -pl customer-service,fleet-service,rental-service,billing-service,api-gateway compile -DskipTests

echo "=== Start serwisów ==="
for mod in customer-service fleet-service rental-service billing-service api-gateway; do
  mvn -q -pl "$mod" spring-boot:run $PROFILE > "$LOGDIR/$mod.log" 2>&1 &
  echo "  $mod -> log: $LOGDIR/$mod.log"
done

echo "=== Czekam na gotowość (max 90s) ==="
for i in $(seq 1 45); do
  fleet=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/vehicles/vehicle-001 2>/dev/null || echo 0)
  cust=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:8083/api/v1/customers/customer-001 2>/dev/null || echo 0)
  rent=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/customers/customer-001/rentals 2>/dev/null || echo 0)
  bill=$(curl -sf -o /dev/null -w "%{http_code}" http://localhost:8084/api/v1/tariffs 2>/dev/null || echo 0)
  login=$(curl -sf -o /dev/null -w "%{http_code}" -X POST http://localhost:8083/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"jan.kowalski@example.com","password":"Haslo123!"}' 2>/dev/null || echo 0)
  token=""
  if [ "$login" = "200" ]; then
    token=$(curl -sf -X POST http://localhost:8083/api/v1/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"jan.kowalski@example.com","password":"Haslo123!"}' 2>/dev/null | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
  fi
  gw=0
  if [ -n "$token" ]; then
    gw=$(curl -sf -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $token" \
      http://localhost:8080/api/v1/vehicles/vehicle-001 2>/dev/null || echo 0)
  fi
  if [ "$fleet" = "200" ] && [ "$cust" = "200" ] && [ "$rent" = "200" ] && [ "$bill" = "200" ] && [ "$login" = "200" ] && [ "$gw" = "200" ]; then
    echo ""
    echo "Gotowe!"
    echo "  API Gateway:  http://localhost:8080"
    echo "  Rental:       http://localhost:8081"
    echo "  Fleet:        http://localhost:8082"
    echo "  Customer:     http://localhost:8083"
    echo "  Billing:      http://localhost:8084"
    echo ""
    echo "Test API: curl http://localhost:8080/api/v1/vehicles/vehicle-001"
    echo ""
    echo "=== Frontend (React) ==="
    if [ -d "$ROOT/frontend/node_modules" ] || (cd "$ROOT/frontend" && npm install --silent); then
      cd "$ROOT/frontend"
      lsof -ti :5173 2>/dev/null | xargs kill -9 2>/dev/null || true
      npm run dev > "$LOGDIR/frontend.log" 2>&1 &
      echo "  Aplikacja web:  http://localhost:5173"
      echo "  Log frontend:   $LOGDIR/frontend.log"
    else
      echo "  Frontend: npm install nie powiódł się — uruchom ręcznie: cd frontend && npm install && npm run dev"
    fi
    exit 0
  fi
  sleep 2
done

echo "Timeout — sprawdź logi w $LOGDIR"
exit 1
