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
| Media | AndroidX Media3 (`media3-exoplayer`, `media3-session`) |
| Settings | DataStore Preferences (UI and playback prefs) |
| Build | AGP 9.4.1, Gradle 9.7.1, Kotlin 2.3.10 (AGP built-in Kotlin), compileSdk/targetSdk 37 |

Versions live in `gradle/libs.versions.toml`; check current releases before bumping (Koin 4.2.2, Navigation Compose 2.10.1, Paging 3.5.1, DataStore 1.2.1, Coil 3.6.3, Media3 1.11.1 at the time of writing).

## Source layout

A single `app` module for now, so the data layer can settle before any module split. If the UI grows large, split `:core:data`, `:core:model` and `:feature:*`.

```
app/src/main/kotlin/com/subtracks
  MainActivity.kt, SubtracksApp.kt   Composition root: starts Koin and Coil
  di/AppModule.kt                    Koin module
  ui/
    AppRoot.kt                       Root gate, NavHost and the mini player
    library/                         Tabbed LibraryScreen, album/playlist detail
    playback/                        Mini player and Now Playing screen
    settings/                        Server list and add-server form
    components/                      Cover art and empty/loading states
    theme/                           Material 3 theme (stark black/white)
  playback/
    PlaybackService.kt               MediaSessionService hosting ExoPlayer
    PlaybackController.kt            MediaController wrapper: state, queue, transport
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

The library is one `LibraryScreen` with a pinned top bar: the current section as the title, a custom row of icon buttons (Albums, Artists, Songs, Playlists) on the left, and Sync and Settings icon actions on the right. The selected section is drawn as a filled box in the content colour with the icon inverted, like the Flutter app — this is a hand-rolled row, not Material `TabRow` (no underline indicator). Icons use the Material rounded variants. Tabs switch the content in place; album and playlist rows push a detail screen, and Settings is pushed from the top bar. The sync action doubles as the progress indicator while a sync runs. There is no bottom navigation. A mini player sits above the navigation bar across every screen whenever a track is loaded. It is a permanent part of the app shell (the screens above it consume the navigation-bar inset so nothing is padded twice); the Now Playing screen slides up over the whole shell as an overlay rather than being a nav destination, so the mini player never re-lays out during the transition.

The Albums tab is a three-column grid of covers only — a later preference will toggle captions, and sorting will hang off a FAB, so Settings only manages servers and neither shows sort nor sync controls.

The theme is monochrome — black surface, white content and accent — with no colour extracted from cover art (that was a Flutter feature and is not ported yet).

## Playback

Audio plays through an `ExoPlayer` owned by `PlaybackService` (a `MediaSessionService`), so the system media notification, headset/Bluetooth controls and background playback come from the framework. The UI never touches the player directly: `PlaybackController` is an app-scoped singleton that connects a `MediaController` to the session, exposes a `StateFlow<PlaybackState>` (current item, playing flag, position, has-next/previous) and the transport calls. It reaches the session through a small `PlayerConnection`/`PlayerHandle` seam rather than Media3 types, so the queue and window logic is covered by JVM unit tests with a fake player. It connects lazily on first observation or play, and restores the persisted queue (paused) on launch.

The queue lives in SQL, not memory. `queue_entries` is an ordered list of *references* — a whole playlist, an album, or a single song — each with an optional inclusive ordinal range (`rangeStart`/`rangeEnd`; both null means the whole referenced list). A queue can therefore be "album X tracks 1-5, song Y, then album X tracks 6-10" without copying a row per track, and inserting, moving or removing an entry is a single row. A one-row `playback_cursor` table holds the current position; both are written as playback advances.

`QueueRepository` resolves a queue position to a song lazily: it walks the entries summing their lengths (a `COUNT(*)` per entry) and then fetches one row with `LIMIT 1 OFFSET`. `PlaybackController` keeps only the resolved entry lengths and the cursor in memory — never the songs — and hands the player a bounded window (currently `2 * 25 + 1` items) centred on the cursor. A player index maps back to a queue position as `windowStart + index`. The window is loaded once with `setMediaItems` and then shifted incrementally with `addMediaItems`/`removeMediaItem` as playback advances; a shift never replaces the playlist, so the current item and its prepared next source survive and transitions stay gapless. `hasNext`/`hasPrevious` come from the SQL queue size, not the player's playlist. The snapshot is re-read whenever the window shifts and on skip, so a sync that shrinks the referenced album or playlist cannot strand the window on stale lengths; if the active source changes under a loaded queue, playback stops rather than resolving the old queue's stream URLs against the new server.

Because the resolver reports *which entry* a position falls in, `PlaybackState` also carries the current queue context (`kind` and `refId`). That is what scopes the playing-track indicator in the album and playlist lists: a song that appears in both only lights up in the list the queue was actually started from.

Stream URLs come from `SourceRepository.streamUri`, which asks the active `SubsonicSource` for a freshly salted `stream` URL; because the URL changes every request, artwork is attached to the media item by identity (the `CoverArtRef` cache key) rather than by URL.

The window is also the queue the media notification and Android Auto can browse — a deliberate trade-off, since the alternative is holding an entire large library in memory. The queue view resolves positions lazily too: it pages with queries bounded to one page per entry (never the whole queue) and rewrites `queue_entries` to edit. Removing a position splits its entry into up to two ranges, and moving one turns the moved track into an explicit song entry inserted at the target index, so an edit costs a couple of rows rather than a copy of the queue. When the edit falls inside the player's window the controller mirrors it with `removeMediaItem`/`moveMediaItem` so playback is not interrupted; removing the playing track, or a move that crosses the window boundary, reloads the window around the cursor. The controller keeps the pre-edit entries and cursor, so the queue view can offer a one-step undo. Timed lyrics/gapless format preferences, scrobbling, downloads and adding to the queue from the library are not wired yet.

## Data model

`sources` and `subsonic_sources` hold the configured servers. Library tables are keyed by `(sourceId, id)` and cascade from `sources`: `artists`, `albums`, `playlists`, `playlist_songs`, `songs`. `search_index` is an FTS5 table (trigram tokenizer) over titles, maintained by sync.

UI and playback preferences live in DataStore (`user_prefs`), not in SQLite: they are not relational, nothing joins against them, and keeping them out of Room avoids a schema migration per preference. Room is for the library only.

Every schema version step ships a hand-written `Migration` (`data/db/Migrations.kt`), and there is no destructive fallback, so an upgrade never silently wipes the local mirror. The exported schemas under `app/schemas/` are the source of truth for the migration SQL, and `MigrationsTest` runs each step against a real SQLite connection and asserts the resulting tables, columns and indices.

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

- Unit tests use Robolectric, Roborazzi (screenshots) and MockWebServer (client), plus an in-memory Room database on the bundled SQLite JVM driver (Robolectric's SQLite has no FTS5).
- Roborazzi screenshots exercise the stateless `*Screen` composables with fake data; paging screens are fed `PagingData.from(...)`. They are rendered on demand with `gradle :app:recordRoborazziDebug` (into the gitignored `app/src/test/screenshots/`) for local review; no images are committed and nothing verifies them in CI.
- Cover art in the screenshots is generated deterministically in the test and served through Coil's `FakeImageLoaderEngine` (synchronous, and no third-party images are committed); a real `ImageLoader` is installed for the run and reset afterwards. Each image is two or three flat colours in a pattern that identifies its type — stripes for albums, dots for playlists, rings for artists.
- Integration tests run `SubsonicSourceIntegrationTest` against real navidrome and gonic instances, driven by `tools/integration-test.nu`.

## Build and CI

The Nix flake devshell provides the whole toolchain, and `direnv` loads it into the working shell. CI (`.forgejo/workflows/ci.yml`) runs `unit`, `lint` and `integration` on a containerized Nix runner, restoring the Nix store from the Actions cache and caching Gradle and the integration test music.
