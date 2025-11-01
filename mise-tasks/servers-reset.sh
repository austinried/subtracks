#!/usr/bin/env bash
set -e

docker compose build
docker compose down
docker volume rm $(docker compose volumes -q) || true

docker compose up -d

docker compose run --rm library-manager music-download.ts
docker compose run --rm library-manager setup-servers.ts

echo "waiting for library scans..."
sleep 10

docker compose down
