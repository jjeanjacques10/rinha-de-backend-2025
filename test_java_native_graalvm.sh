#!/bin/bash

echo "Starting the test script..."

cd app
./mvnw spring-boot:build-image -Pnative -DskipTests
cd ..

#docker tag docker.io/jjeanjacques10/rinhabackend2025:graalvm jjeanjacques/rinhabackend2025:graalvm-v7.0
#docker push jjeanjacques/rinhabackend2025:graalvm-v7.0

docker-compose -f payment-processor/docker-compose.yml down --remove-orphans
docker-compose -f payment-processor/docker-compose.yml up -d --build

docker-compose -f app/docker-compose-graalvm.yml down --remove-orphans
docker-compose -f app/docker-compose-graalvm.yml up -d --build

echo "Setting up environment variables for k6..."
export K6_WEB_DASHBOARD=true
export K6_WEB_DASHBOARD_PORT=5665
export K6_WEB_DASHBOARD_PERIOD=2s
export K6_WEB_DASHBOARD_OPEN=true
export K6_WEB_DASHBOARD_EXPORT='report.html'


## Await for the services to be up and running
echo "Waiting for the services to be ready..."
sleep 15

echo "Running k6 tests..."
#k6 run -e MAX_REQUESTS=100 rinha-test/rinha.js
k6 run rinha-test/rinha.js