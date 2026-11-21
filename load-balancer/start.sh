#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
docker compose up -d --wait

java -jar ../fibonacci-service/target/fibonacci-service-1.0.jar f1 localhost 8081 http://localhost:8500 &
java -jar ../fibonacci-service/target/fibonacci-service-1.0.jar f2 localhost 8082 http://localhost:8500 &
java -jar ../primes-service/target/primes-service-1.0.jar p1 localhost 8091 http://localhost:8500 &
java -jar ../primes-service/target/primes-service-1.0.jar p2 localhost 8092 http://localhost:8500 &

wait