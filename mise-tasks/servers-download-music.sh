#!/usr/bin/env bash
set -e

rm -rf ./music

docker compose build
docker compose run --rm library-manager music-download.ts
