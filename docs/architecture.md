# Architecture

Subtracks is a native Android client for Subsonic-compatible servers (Navidrome, gonic, Airsonic, ...). It is a Kotlin/Jetpack Compose app that keeps a local mirror of the server library in SQLite and plays from a self-hosted server.

## Goals and constraints

- Talk to any Subsonic-compatible server over its REST API.
- Behave well on a poor connection: a local Room mirror, paging, and (later) offline playback.
- F-Droid friendly: free/libre dependencies only, no telemetry, buildable from source without proprietary services.
- `minSdk 24`. The app bundles its own SQLite so behaviour is identical across devices and does not depend on the platform version.

## Stack

| Concern | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| DI | Koin, constructor injection |
| Persistence | Room 3 (`androidx.room3`) on AndroidX `sqlite-bundled` (SQLite 3.50+) |
| Lists | Paging 3 over Room `PagingSource` (local mirror, not remote paging) |
| Networking | OkHttp + streaming SAX XML parsing |
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
    theme/                           Material 3 theme (Material You dynamic colour)
  playback/
    PlaybackService.kt               MediaSessionService hosting ExoPlayer
    PlaybackController.kt            MediaController wrapper: state, queue, transport
  data/
    model/Models.kt                  Room entities
    db/                              SubtracksDatabase, DAOs, bundled-SQLite builder
    prefs/UserPreferences.kt         DataStore-backed UI prefs
    repo/                            SourceRepository, LibraryRepository
    source/                          MusicSource abstraction
      subsonic/                      SubsonicClient, SubsonicXml, SubsonicSource
    sync/                            SyncService (stream + per-batch writes + prune), SyncManager (status)
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

The library is one `LibraryScreen` with a pinned top bar: the current section as the title, a custom row of icon buttons (Albums, Artists, Playlists) on the left, and Sync and Settings icon actions on the right. The selected section is drawn as a filled box in the content colour with the icon inverted, like the Flutter app — this is a hand-rolled row, not Material `TabRow` (no underline indicator). Icons use the Material rounded variants. Tabs switch the content in place; album and playlist rows push a detail screen, and Settings is pushed from the top bar. The sync action doubles as the progress indicator while a sync runs. There is no bottom navigation. A mini player sits above the navigation bar across every screen whenever a track is loaded. It is a permanent part of the app shell (the screens above it consume the navigation-bar inset so nothing is padded twice); the Now Playing screen slides up over the whole shell as an overlay rather than being a nav destination, so the mini player never re-lays out during the transition.

The Albums tab is a three-column grid of covers only — a later preference will toggle captions, and sorting will hang off a FAB, so Settings only manages servers and neither shows sort nor sync controls.

The default theme is Material You: on Android 12+ the root scheme is the wallpaper's dynamic dark palette, falling back to the Material 3 baseline dark scheme elsewhere. Screens that show cover art layer an artwork-derived palette (`ArtworkTheme`) on top, and when no art is available they fall back to the same theme colours rather than a fixed monochrome scheme.

## Playback

Audio plays through an `ExoPlayer` owned by `PlaybackService` (a `MediaSessionService`), so the system media notification, headset/Bluetooth controls and background playback come from the framework. The UI never touches the player directly: `PlaybackController` is an app-scoped singleton that connects a `MediaController` to the session, exposes a `StateFlow<PlaybackState>` (current item, playing flag, position, has-next/previous) and the transport calls. It reaches the session through a small `PlayerConnection`/`PlayerHandle` seam rather than Media3 types, so the queue and window logic is covered by JVM unit tests with a fake player. It connects lazily on first observation or play, and restores the persisted queue (paused) on launch.

The queue lives in SQL, not memory. `queue_entries` is an ordered list of *references* — a whole playlist, an album, or a single song — each with an optional inclusive ordinal range (`rangeStart`/`rangeEnd`; both null means the whole referenced list). A queue can therefore be "album X tracks 1-5, song Y, then album X tracks 6-10" without copying a row per track, and inserting, moving or removing an entry is a single row. A one-row `playback_cursor` table holds the current position; both are written as playback advances.

`QueueRepository` resolves a queue position to a song lazily: it walks the entries summing their lengths (a `COUNT(*)` per entry) and then fetches one row with `LIMIT 1 OFFSET`. `PlaybackController` keeps only the resolved entry lengths and the cursor in memory — never the songs — and hands the player a bounded window (currently `2 * 25 + 1` items) centred on the cursor. A player index maps back to a queue position as `windowStart + index`. The window is loaded once with `setMediaItems` and then shifted incrementally with `addMediaItems`/`removeMediaItem` as playback advances; a shift never replaces the playlist, so the current item and its prepared next source survive and transitions stay gapless. `hasNext`/`hasPrevious` are intentionally always true rather than derived from the SQL queue size: the previous/next buttons stay usable at both ends of the queue so repeat mode and restart-the-track keep working, and `next()`/`previous()` decide the actual end-of-queue action (advance, wrap when repeating, restart, or no-op). The snapshot is re-read whenever the window shifts and on skip, so a sync that shrinks the referenced album or playlist cannot strand the window on stale lengths; if the active source changes under a loaded queue, playback stops rather than resolving the old queue's stream URLs against the new server.

Playback is a *context* plus a manual *up next* list. `queue_entries` is the context — the album, playlist or song you started, and the only thing shuffle permutes. `up_next_entries` is the manual queue: the same reference-plus-range rows, ordered, never shuffled. A `playback_cursor.upNextAnchor` holds the context track the block sits behind; the controller re-anchors it whenever playback lands on a different context track, so the block always plays next. `QueueRepository` resolves a combined position by mapping it through the context order and splicing the block after the anchor, so the controller's window and next/previous logic keep working. Add to queue appends to the block and play next inserts at its front; both reuse the context edit machinery (range split, insert, compact). Advancing out of a block track removes it — consume as played, with no history — and a new context clears the block, so a manual edit never disturbs the shuffle permutation. Shuffle itself stores no per-track order: `playback_cursor.shuffleSeed`/`shuffleSize` are the whole state, and a position maps through a seekable Feistel permutation of `[0, size)`, so a very large album cannot materialise an order list. Reordering the context while shuffled would need that order back, so the context is playback-only while shuffled and manual ordering goes through the up-next block.

Because the resolver reports *which entry* a position falls in, `PlaybackState` also carries the current queue context (`kind` and `refId`). That is what scopes the playing-track indicator in the album and playlist lists: a song that appears in both only lights up in the list the queue was actually started from.

Stream URLs come from `SourceRepository.streamUri`, which asks the active `SubsonicSource` for a freshly salted `stream` URL; because the URL changes every request, artwork is attached to the media item by identity (the `CoverArtRef` cache key) rather than by URL. The active source carries the stream quality (a max bitrate plus an optional transcode format) for the current network: `NetworkMonitor` reports Wi-Fi vs mobile (metered) and `UserPreferences` stores a separate `StreamQuality` per mode, so `SourceRepository` rebuilds the source when either the mode or a quality changes. `PlaybackController` observes that effective quality and reloads the window around the current position, preserving play/pause and the playback position, so a Wi-Fi↔mobile transition re-issues the stream URLs with the new settings (a no-op when the two modes share a quality). Transcoded responses are chunked with no length and (because ffmpeg cannot seek back in a stream) no MP3 duration header, so Media3 would treat them as live and ignore seeks; the server's `estimateContentLength` is no better, since its estimate under-counts and cuts the track short. So a transcoded URL carries a declared upper-bound length in its fragment (never sent to the server), `KnownLengthDataSource` hands that length to Media3 to keep the stream seekable without truncation, and each transcoded item is clipped to the library duration so the timeline ends where the audio does and the next track still advances.

The window is also the queue the media notification and Android Auto can browse — a deliberate trade-off, since the alternative is holding an entire large library in memory. The queue view resolves positions lazily too: it loads the queue around the cursor in chunks and extends by a chunk when you scroll near either end, so it never materializes the whole queue. Dragging uses `sh.calvin.reorderable`, which floats the lifted row and reorders the loaded list live (animating with `animateItem`), so editing is what a drag needs: the view reorders its loaded list while dragging and commits one move on drop. `queue_entries` is rewritten to edit: removing a position splits its entry into up to two ranges, and moving one turns the moved track into an explicit song entry inserted at the target index, so an edit costs a couple of rows rather than a copy of the queue. When the edit falls inside the player's window the controller mirrors it with `removeMediaItem`/`moveMediaItem` so playback is not interrupted; removing the playing track, or a move that crosses the window boundary, reloads the window around the cursor. The controller keeps the pre-edit entries and cursor, so the queue view can offer a one-step undo. The up-next block is fully editable in the same view (drag reorder and delete), while the context is read-only while shuffled. Timed lyrics are not wired yet.

Scrobbling is a `Scrobbler` that collects the controller's `state` and `positionMs` and drives the Subsonic `scrobble` endpoint: a now-playing notification (`submission=false`) when a track starts playing, and one submission (`submission=true`, with the playback start time) when the track passes half its duration or four minutes, whichever comes first. That split is the endpoint's own `submission` flag, and the threshold is the Last.fm rule the server forwards to; servers such as Navidrome trust the client and apply no threshold themselves. Tracks under 30 seconds are never submitted, and only time actually played counts: forward and backward seeks are ignored, and a rewind only starts a fresh listen when it lands in the track's first few seconds (a repeat or an explicit restart). The duration is the player's resolved value, falling back to the library row when the player has not reported one. Sending is decoupled from the collector so a slow or offline write cannot stall position tracking, while a lock keeps the writes in order. The policy is a pure state machine with no Android dependencies. A Settings toggle (`UserPreferences.scrobbling`, on by default) gates both calls. A submission also bumps the played song's local `playCount`/`played` and its album and artist ranks, so the play sorts move before the next sync; the sync then overwrites them with the server's own numbers, so a play the server never accepted is rolled back at that point.

Server writes — now-playing, scrobbles and stars — go through a `ServerActionSink`. The only implementation today is `NetworkServerActionSink`, which resolves the active source and calls it; a persistent, ordered outbox can replace it later so plays and stars taken offline are queued and replayed when the server is reachable. Now-playing is deliberately not durable (Last.fm says failed now-playing requests must not be retried), while submissions and stars carry everything a queue would need (the song or entity id and, for a play, the time it was listened).

The media notification carries a star button for the playing track. `PlaybackService` adds a `com.subtracks.STAR` session command and a `CommandButton` to the `MediaSession`, and drives its filled/unfilled icon from the playing song's Room row. That path — `MediaSession.Callback`, `SessionCommand`, `CommandButton`, `SessionResult`/`SessionError` and `MediaSession.setMediaButtonPreferences` — is annotated `@UnstableApi` in Media3 1.11.1 and is therefore opted into explicitly: when bumping Media3, re-check those signatures and the notification's button resolution. The stable `setCustomLayout` is not a substitute here, because the notification manager only refreshes on `onMediaButtonPreferencesChanged`, so the icon would not update mid-playback.

## Data model

`sources` and `subsonic_sources` hold the configured servers. Library tables are keyed by `(sourceId, id)` and cascade from `sources`: `artists`, `albums`, `playlists`, `playlist_songs`, `songs`.

The library is mirrored wholesale at sync and must work offline, so metadata is never fetched on demand: everything used to browse, filter or sort a local list has to be present locally. The only on-demand network is media bytes (streams and downloads) and images. A sort whose key is server-derived — say play frequency — therefore has to be synced into a local column, not requested lazily when the sort is selected. Play frequency and last-played are read from the synced song rows and aggregated into the album and artist rows by the same sync (`SUM(playCount)`, `MAX(played)`): servers disagree on what they put on album objects (Airsonic returns no album play count at all), while the song fields are consistent, and the aggregate only writes rows whose value changed. The artist roll-up attributes plays to the album's artist (the same `artistId` the artist list is built from), so a track artist credited only on a compilation can appear unplayed; that is deliberate, since it keeps the rank consistent with the album rows rather than the server's per-track artists.

UI and playback preferences live in DataStore (`user_prefs`), not in SQLite: they are not relational, nothing joins against them, and keeping them out of Room avoids a schema migration per preference. Room is for the library only.

Every schema version step ships a hand-written `Migration` (`data/db/Migrations.kt`), and there is no destructive fallback, so an upgrade never silently wipes the local mirror. The exported schemas under `app/schemas/` are the source of truth for the migration SQL, and `MigrationsTest` runs each step against a real SQLite connection and asserts the resulting tables, columns and indices.

Library screens page over Room with `PagingSource<Int, T>` queries, ordering by a sort chosen in preferences (name, artist, year, recently added, frequently/recently played). Every album, artist and playlist order has an index on its own table, and the sortable text columns carry `COLLATE NOCASE`, so a page seeks the index in order instead of re-sorting the table for each `LIMIT`/`OFFSET`. Starred orders pin unstarred rows last in both directions, matching the list grouping, so a starred "reversed" order is not a literal mirror of the base order. Paging is local, but it keeps memory bounded on large libraries and lets a huge playlist render incrementally. Room 3 requires `@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)` on the DAO for this.

Song rows show the album's cover art, fetched by joining `songs` to `albums` into a `SongListItem` rather than storing cover art on the song: some servers return a distinct cover ID per track, and keeping those would fragment the image cache for no visual gain. Artists show their image cropped to a circle.

Images: grids and lists request a 256 px `getCoverArt` thumbnail; the 120 dp detail headers request the original. Because every request is freshly salted, the URL changes constantly, so each image is wrapped in a `CoverArtRef` carrying a `memoryCacheKey`/`diskCacheKey` of `sourceId:coverArtId:thumbnail` — caching is keyed on the identity of the art, not the request URL, and so survives the salt and app restarts.

## Sync

`SyncService.sync()` is serialized by `SyncManager`, which coalesces duplicate requests. It walks five entities in sequence — artists, albums, songs, playlists and playlist songs — and each one is a cold `Flow` from `MusicSource` that the service collects: it remembers the IDs already in the local mirror, upserts every batch as it arrives (one `immediateTransaction` per ~500-row batch), crosses those IDs off, and after that entity's stream ends deletes whatever is left, i.e. the rows the server no longer lists. The parser reads the HTTP response stream directly, so the fetched library is streamed page by page rather than materialised wholesale, though the prune still holds the existing ID set to diff against.

Songs have a primary and a fallback fetch. The primary probes `search3` with an empty query once (memoized per source); if the server supports it, the library is paged through `search3` 500 songs at a time. Otherwise the fallback pages `getAlbumList2` and then issues one `getAlbum` per album — it works everywhere but costs a request per album instead of per 500 songs. Every paged loop stops on a short page and is capped at 1000 pages. Playlists are fetched whole, and their songs are stored by position so a playlist that got shorter (or was emptied) is trimmed at the end.

Cleanup is the "everything I had, minus everything the server just sent" step described above: artists, albums, songs and playlists delete the leftover IDs in 500-row chunks, disc titles are deleted per `(album, disc)` for albums that still exist, and playlist entries beyond the new count are removed along with entries for playlists that disappeared.

Sync is not atomic. Each batch commits on its own, so a failure leaves the batches already written in place and skips that entity's prune; stale rows can linger until the next successful sync, which also invalidates the queue's cached library. The one hazard is a scan that stops early — a server returning a short page by mistake, or a library larger than the page cap — because the prune trusts that the stream actually reached the end. Each batch commit also makes Room notify paged observers once per batch (a few per second, bounded by the network round-trips), which is cheap but can make an on-screen list churn during a sync; gate UI observation on `SyncStatus.Running` if that is ever noticeable.

## Search

Search is a case-insensitive substring filter on the library queries, scoped to the active tab and ordered by the tab's sort. A query of three characters or more goes through a per-entity FTS5 trigram table: `album_search` indexes `name` and `albumArtist`, `artist_search` and `playlist_search` index `name`, and the filter is `rowid IN (SELECT rowid FROM <table> WHERE <table> MATCH '"' || replace(:search, '"', '""') || '"')`. Two characters or fewer cannot form a trigram, so they fall back to the `instr(lower(column), lower(:search))` scan; an empty query skips both.

The trigram tables are external content (`content=` the base table, keyed by its rowid), so they store no second copy of the text. Room declares them as `@Fts5(contentEntity = ...)` entities and generates the insert/update/delete triggers that keep the index current per row, so every write path — batch sync upserts, star toggles, prune deletes and source cascades — maintains it with no extra code. Sorting and paging still come from the normal indexed library queries; the FTS match only narrows which rows they return, and `MIGRATION_19_20` creates the tables, triggers and a `'rebuild'` for existing libraries.

The two branches do not fold case identically: the trigram tokenizer lowercases all of Unicode, while SQLite's `lower()` (used by the fallback) is ASCII-only. So an accented query of three or more characters matches case-insensitively where its two-character prefix would not. This only ever widens the result set, and is not worth normalising in SQL.

## Networking and auth

`SubsonicClient` speaks the Subsonic REST API over OkHttp. Auth is the token scheme (`t = md5(password + salt)`), salted freshly on every request including media URLs; plaintext `p=` is supported as a fallback for old servers. Responses are parsed as a stream with SAX (`SAXParserFactory`, hardened best-effort — Android's parser rejects some flags, so unsupported ones are ignored), so entities are mapped as elements close rather than through a DOM. The client rejects any document that is not a `<subsonic-response>` and classifies malformed or truncated bodies as a `SubsonicException`, so a captive portal or login page can never be mistaken for an empty library and wipe the local mirror.

## Internationalization

All user-facing text lives in Android string resources: `res/values/strings.xml` (the English source, including `<plurals>`) and `res/values-*/strings.xml` for translations. This is the platform-native mechanism, needs no extra dependency, and is a first-class Weblate format ("Android String Resource": monolingual base `res/values/strings.xml`, file mask `res/values-*/strings.xml`, plurals supported). Code reads it through `stringResource`/`pluralStringResource` in Compose and `getString`/`getQuantityString` elsewhere.

Resource names are `snake_case`. They were derived once from the old Flutter ARB keys: resources whose English matched an old string (the "identical" set) reuse the old key's translations, mechanically renamed (`settingsServersFieldsName` -> `settings_servers_fields_name`); everything else is a new key. Strings whose wording changed were deliberately given new keys rather than reusing a translation with stale meaning. The migration is one-time; there is no generator in the build.

27 locales were carried over from the Flutter app: `ar, ca, cs, da, de, es, eu, fr, gl, hu, it, ja, ko, nb, pa, pl, pt, pt-BR, ru, sv, ta, th, tr, uk, vi, zh, zh-Hant` (PR #178 plus `ar/ca/cs/pa/pt` from the frozen Flutter branches). Locale files contain only translated keys; anything missing falls back to English. Chinese uses script qualifiers (`values-b+zh+Hans`, `values-b+zh+Hant`) and Brazilian Portuguese is `values-b+pt+BR`.

Per-app language selection (Android 13+) is generated by AGP: `androidResources.generateLocaleConfig = true` with `res/resources.properties` supplying the default locale. `StringResourcesTest` guards the translations: every locale resource must exist in the default with the same kind and no placeholder the default lacks, which is what caught a duplicated `{value}` in the Thai ARB during migration.

Some text is still English-only: transient toasts and status strings produced below the UI (`showMessage` and `SyncStatus.Failed`), the download progress notification, playback error messages, and Subsonic parser exceptions. Persisted download failures are no longer free text: `SongDownload.error` is a `DownloadError` enum mapped to `@StringRes` and stored by name (unknown legacy values degrade to `DownloadError.Failed`), so `SongInfoDialog` resolves it in the current locale.

## Security and privacy

No analytics or third-party telemetry. Credentials are stored locally in app data and are only ever sent to the configured server. Cleartext HTTP is permitted because self-hosted servers often run on a LAN; users can still configure HTTPS.

## Testing

- Unit tests use Robolectric, Roborazzi (screenshots) and MockWebServer (client), plus an in-memory Room database on the bundled SQLite JVM driver (Robolectric's SQLite differs from the bundled one).
- Roborazzi screenshots exercise the stateless `*Screen` composables with fake data; paging screens are fed `PagingData.from(...)`. They are rendered on demand with `gradle :app:recordRoborazziDebug` (into the gitignored `app/src/test/screenshots/`) for local review; no images are committed and nothing verifies them in CI.
- Cover art in the screenshots is generated deterministically in the test and served through Coil's `FakeImageLoaderEngine` (synchronous, and no third-party images are committed); a real `ImageLoader` is installed for the run and reset afterwards. Each image is two or three flat colours in a pattern that identifies its type — stripes for albums, dots for playlists, rings for artists.
- Integration tests run `SubsonicSourceIntegrationTest` against real navidrome and gonic instances, driven by `tools/integration-test.nu`.

## Build and CI

The Nix flake devshell provides the whole toolchain, and `direnv` loads it into the working shell. CI (`.forgejo/workflows/ci.yml`) runs `unit`, `lint` and `integration` on a containerized Nix runner, restoring the Nix store from the Actions cache and caching Gradle and the integration test music.
