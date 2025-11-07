#!/usr/bin/env bash
set -e

docker compose build
docker compose down
docker volume rm $(docker compose volumes -q) || true

docker compose up -d
echo "waiting for library scans..."
sleep 10

docker compose run --rm library-manager setup-servers.ts

docker compose down
