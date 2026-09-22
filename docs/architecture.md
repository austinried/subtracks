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
| Navigation | Navigation Compose |
| DI | Koin, constructor injection |
| Persistence | Room 3 (`androidx.room3`) on AndroidX `sqlite-bundled` (SQLite 3.50+, FTS5) |
| Lists | Paging 3 over Room `PagingSource` (local mirror, not remote paging) |
| Networking | OkHttp + DOM XML parsing |
| Async | Kotlin coroutines / `Flow` |
| Images | Coil 3 |
| Media | AndroidX Media3 / ExoPlayer (planned) |
| Settings | DataStore Preferences (UI and playback prefs) |
| Build | AGP 9.4.1, Gradle 9.7.1, Kotlin 2.3.10 (AGP built-in Kotlin), compileSdk/targetSdk 37 |

Versions live in `gradle/libs.versions.toml`; check current releases before bumping (Koin 4.2.2, Navigation Compose 2.10.1, Paging 3.5.1, DataStore 1.2.1, Coil 3.6.3 at the time of writing).

## Source layout

A single `app` module for now, so the data layer can settle before any module split. If the UI grows large, split `:core:data`, `:core:model` and `:feature:*`.

```
app/src/main/kotlin/com/subtracks
  MainActivity.kt, SubtracksApp.kt   Composition root: starts Koin and Coil
  di/AppModule.kt                    Koin module
  ui/
    AppRoot.kt                       Root gate and NavHost
    library/                         Tabbed LibraryScreen, album/playlist detail
    settings/                        Server list and add-server form
    components/                      Cover art and empty/loading states
    theme/                           Material 3 theme (stark black/white)
  data/
    model/Models.kt                  Room entities, including the search_index FTS5 table
    db/                              SubtracksDatabase, DAOs, bundled-SQLite builder
    prefs/UserPreferences.kt         DataStore-backed UI prefs
    repo/                            SourceRepository, LibraryRepository
    source/                          MusicSource abstraction
      subsonic/                      SubsonicClient, SubsonicXml, SubsonicSource
    sync/                            SyncService (fetch + transaction), SyncManager (status)
```

## Layers

```
Compose UI  ->  ViewModel  ->  Repository  ->  MusicSource (remote, Subsonic)
                                          \->  Room (local mirror, Flow / PagingSource)
```

- The remote side sits behind `MusicSource` so additional backends can be added later.
- `SourceRepository` owns the configured servers and builds a `SubsonicSource` from the active one; it also serves cover-art URLs. `LibraryRepository` reads the local mirror.
- The local side is Room; reads are `Flow`s, so the UI updates as sync writes.
- `SyncManager` triggers syncs off the UI (its own scope) and exposes a `StateFlow<SyncStatus>`; `SyncService` does the actual work.
- ViewModels are wired with Koin; screens are split into a stateful `*Route` (default `koinViewModel()`) and a stateless `*Screen` so the latter can be screenshot-tested directly.

## UI shell

The library is one `LibraryScreen` with a pinned top bar: the current section as the title, a custom row of icon buttons (Albums, Artists, Songs, Playlists) on the left, and Sync and Settings icon actions on the right. The selected section is drawn as a filled box in the content colour with the icon inverted, like the Flutter app — this is a hand-rolled row, not Material `TabRow` (no underline indicator). Icons use the Material rounded variants. Tabs switch the content in place; album and playlist rows push a detail screen, and Settings is pushed from the top bar. The sync action doubles as the progress indicator while a sync runs. There is no bottom navigation. (The Flutter app also had a Now Playing bar pinned to the bottom; that returns with playback in Phase 3.)

The Albums tab is a three-column grid of covers only — a later preference will toggle captions, and sorting will hang off a FAB, so Settings only manages servers and neither shows sort nor sync controls.

The theme is monochrome — black surface, white content and accent — with no colour extracted from cover art (that was a Flutter feature and is not ported yet).

## Data model

`sources` and `subsonic_sources` hold the configured servers. Library tables are keyed by `(sourceId, id)` and cascade from `sources`: `artists`, `albums`, `playlists`, `playlist_songs`, `songs`. `search_index` is an FTS5 table (trigram tokenizer) over titles, maintained by sync.

UI and playback preferences live in DataStore (`user_prefs`), not in SQLite: they are not relational, nothing joins against them, and keeping them out of Room avoids a schema migration per preference. Room is for the library only.

Library screens page over Room with `PagingSource<Int, T>` queries, ordering by a sort chosen in preferences (name, artist, year, recently added). Paging is local, but it keeps memory bounded on large libraries and lets a huge song or playlist list render incrementally. Room 3 requires `@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)` on the DAO for this.

Song rows show the album's cover art, fetched by joining `songs` to `albums` into a `SongListItem` rather than storing cover art on the song: some servers return a distinct cover ID per track, and keeping those would fragment the image cache for no visual gain. Artists show their image cropped to a circle.

Images: grids and lists request a 256 px `getCoverArt` thumbnail; the 120 dp detail headers request the original. Because every request is freshly salted, the URL changes constantly, so each image is wrapped in a `CoverArtRef` carrying a `memoryCacheKey`/`diskCacheKey` of `sourceId:coverArtId:thumbnail` — caching is keyed on the identity of the art, not the request URL, and so survives the salt and app restarts.

## Sync

`SyncService.sync()` is serialized with a mutex. It fetches the whole library first (artists, albums, songs, playlists and their entries), then writes in a single `immediateTransaction`: upserts, deletes rows no longer present on the server (diffed against the DB), rebuilds `playlist_songs`, and rebuilds the FTS index for that source. Because fetching happens outside the transaction, a network failure never leaves a half-applied state.

## Search

`search_index` is FTS5 with the trigram tokenizer, so `MATCH` supports substring queries. Queries shorter than three characters are rejected by `SearchDao` (below the trigram minimum). Rows are keyed by `(sourceId, type, itemId)`; the DAO scopes results to a source and orders by rank.

## Networking and auth

`SubsonicClient` speaks the Subsonic REST API over OkHttp. Auth is the token scheme (`t = md5(password + salt)`), salted freshly on every request including media URLs; plaintext `p=` is supported as a fallback for old servers. Responses are parsed with a `DocumentBuilderFactory` hardened best-effort (Android's parser rejects some flags, so unsupported ones are ignored), and the client rejects any document that is not a `<subsonic-response>`, so a captive portal or login page can never be mistaken for an empty library and wipe the local mirror.

## Security and privacy

No analytics or third-party telemetry. Credentials are stored locally in app data and are only ever sent to the configured server. Cleartext HTTP is permitted because self-hosted servers often run on a LAN; users can still configure HTTPS.

## Testing

- Unit tests use Robolectric, Roborazzi (screenshot goldens) and MockWebServer (client), plus an in-memory Room database on the bundled SQLite JVM driver (Robolectric's SQLite has no FTS5).
- Screenshot goldens exercise the stateless `*Screen` composables with fake data; paging screens are fed `PagingData.from(...)`. Record with `gradle :app:recordRoborazziDebug`, check with `:app:verifyRoborazziDebug`.
- Cover art in goldens is generated deterministically in the test and served through Coil's `FakeImageLoaderEngine` (synchronous, and no third-party images are committed); a real `ImageLoader` is installed for the run and reset afterwards. Each image is two or three flat colours in a pattern that identifies its type — stripes for albums, dots for playlists, rings for artists.
- Integration tests run `SubsonicSourceIntegrationTest` against real navidrome and gonic instances, driven by `tools/integration-test.nu`.

## Build and CI

The Nix flake devshell provides the whole toolchain, and `direnv` loads it into the working shell. CI (`.forgejo/workflows/ci.yml`) runs `unit`, `lint` and `integration` on a containerized Nix runner, restoring the Nix store from the Actions cache and caching Gradle and the integration test music.
