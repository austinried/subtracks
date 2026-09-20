# Architecture

Subtracks is a native Android client for Subsonic-compatible servers (Navidrome, gonic, Airsonic, ...). It is a Kotlin/Jetpack Compose app that keeps a local mirror of the server library in SQLite and plays from a self-hosted server.

## Goals and constraints

- Talk to any Subsonic-compatible server over its REST API.
- Behave well on a poor connection: a local Room mirror, paging, and (later) offline playback.
- F-Droid friendly: free/libre dependencies only, no telemetry, buildable from source without proprietary services.
- `minSdk 24`. The app bundles its own SQLite so behaviour is identical across devices and FTS5 is always available.

## Stack

| Concern | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose (planned) |
| DI | Koin, constructor injection (planned) |
| Persistence | Room 3 (`androidx.room3`) on AndroidX `sqlite-bundled` (SQLite 3.50+, FTS5) |
| Networking | OkHttp + DOM XML parsing |
| Async | Kotlin coroutines / `Flow` |
| Images | Coil 3 (planned) |
| Media | AndroidX Media3 / ExoPlayer (planned) |
| Settings | DataStore (planned) |
| Build | AGP 9.4.1, Gradle 9.7.1, Kotlin 2.3.10 (AGP built-in Kotlin), compileSdk/targetSdk 37 |

## Source layout

A single `app` module for now, so the data layer can settle before any module split. If the UI grows large, split `:core:data`, `:core:model` and `:feature:*`.

```
app/src/main/kotlin/com/subtracks
  MainActivity.kt, SubtracksApp.kt
  ui/                     Compose screens and theme (early)
  data/
    model/Models.kt       Room entities, including the search_index FTS5 table
    db/                   SubtracksDatabase, DAOs, bundled-SQLite builder
    source/               MusicSource abstraction
      subsonic/           SubsonicClient, SubsonicXml, SubsonicSource
    sync/SyncService.kt   fetch, then one transaction of upsert + prune
```

## Layers

```
Compose UI  ->  ViewModel  ->  Repository  ->  MusicSource (remote, Subsonic)
                                          \->  Room (local mirror, Flow)
```

- The remote side sits behind `MusicSource` so additional backends can be added later.
- The local side is Room; reads are `Flow`s, so the UI updates as sync writes.
- A repository layer (planned in Phase 2) joins the two: read from Room, refresh via `MusicSource` and `SyncService`.

## Data model

`sources` and `subsonic_sources` hold the configured servers, and `app_settings` holds playback preferences. Library tables are keyed by `(sourceId, id)` and cascade from `sources`: `artists`, `albums`, `playlists`, `playlist_songs`, `songs`. `search_index` is an FTS5 table (trigram tokenizer) over titles, maintained by sync.

## Sync

`SyncService.sync()` is serialized with a mutex. It fetches the whole library first (artists, albums, songs, playlists and their entries), then writes in a single `immediateTransaction`: upserts, deletes rows no longer present on the server (diffed against the DB), rebuilds `playlist_songs`, and rebuilds the FTS index for that source. Because fetching happens outside the transaction, a network failure never leaves a half-applied state.

## Search

`search_index` is FTS5 with the trigram tokenizer, so `MATCH` supports substring queries. Queries shorter than three characters are rejected by `SearchDao` (below the trigram minimum). Rows are keyed by `(sourceId, type, itemId)`; the DAO scopes results to a source and orders by rank.

## Networking and auth

`SubsonicClient` speaks the Subsonic REST API over OkHttp. Auth is the token scheme (`t = md5(password + salt)`, with a fresh salt per request); plaintext `p=` is supported as a fallback for old servers. Responses are parsed with a hardened `DocumentBuilderFactory` (no DTDs, no external entities), and the client rejects any document that is not a `<subsonic-response>`, so a captive portal or login page can never be mistaken for an empty library and wipe the local mirror.

## Security and privacy

No analytics or third-party telemetry. Credentials are stored locally in app data and are only ever sent to the configured server. Cleartext HTTP is permitted because self-hosted servers often run on a LAN; users can still configure HTTPS.

## Testing

- Unit tests use Robolectric, Roborazzi (screenshot goldens) and MockWebServer (client), plus an in-memory Room database on the bundled SQLite JVM driver (Robolectric's SQLite has no FTS5).
- Integration tests run `SubsonicSourceIntegrationTest` against real navidrome and gonic instances, driven by `tools/integration-test.nu`.

## Build and CI

The Nix flake devshell provides the whole toolchain, and `direnv` loads it into the working shell. CI (`.forgejo/workflows/ci.yml`) runs `unit`, `lint` and `integration` on a containerized Nix runner, restoring the Nix store from the Actions cache and caching Gradle and the integration test music.
