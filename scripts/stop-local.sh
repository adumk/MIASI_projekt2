#!/usr/bin/env bash
for p in 5173 8080 8081 8082 8083 8084; do
  pid=$(lsof -ti :$p 2>/dev/null || true)
  if [ -n "$pid" ]; then
    kill -9 $pid 2>/dev/null || true
    echo "Zatrzymano port $p (pid $pid)"
  fi
done
echo "Serwisy zatrzymane."
