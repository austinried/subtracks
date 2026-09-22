# Implementation plan

The rewrite is delivered in vertical slices. Each phase leaves the app in a working, testable state.

## Phase 0 - Foundation (done)

Nix devshell (JDK, Gradle, Android SDK, nushell, navidrome, gonic), Gradle Kotlin DSL at the repo root, a Compose skeleton, Roborazzi/Robolectric screenshot testing, and the carried-over launcher and notification assets. The app ships as `com.subtracks.next`.

## Phase 1 - Data foundation (done)

Everything needed to mirror a server library locally:

- `MusicSource` abstraction with `SubsonicClient`, `SubsonicXml` and `SubsonicSource`.
- Room 3 schema on bundled SQLite, so the SQLite version and FTS5 are identical on every device.
- `SyncService`: fetch first, then a single transaction of upserts with diff-based pruning.
- FTS5 trigram search index over titles.
- Unit tests (client, XML, sync, search), native integration tests against navidrome and gonic, and CI (`unit`, `lint`, `integration`) with warm caches.

## Phase 2 - UI vertical slice (done)

Goal: a usable app for browsing a server, end to end.

- Koin DI and DataStore-backed preferences (album sort); the unused `app_settings` Room table was removed in favour of DataStore.
- App shell: a root gate that shows the add-server flow until a server exists, then the tabbed library screen matching the Flutter app (icon-only tabs for albums, artists, songs and playlists, with sync and settings actions), plus album and playlist detail screens and a monochrome theme.
- Source setup: name/address/username/password with token-auth toggle, a connection test, save-and-sync, plus server switching and removal in settings.
- Library browsing: albums (covers-only grid), artists, songs and playlists paged from Room (`PagingSource`) and rendered with `LazyPagingItems`, with Coil cover art served from stable media URLs.
- Roborazzi screenshots for the eight new screens, rendered on demand for review (not committed).

Not done yet in this slice (deliberately): artist detail, cover-art tonal colour extraction, long-press menus, editing an existing server, and per-section sort/filter controls (only album sort is wired).

## Phase 3 - Playback (in progress)

- (done) Media3/ExoPlayer playback service, queue and now-playing UI: a `MediaSessionService` plus a mini player and a Now Playing screen.
- (done) Media notifications and headset/Bluetooth controls, from the media session.
- (done) A SQL-backed queue of references (whole playlist / album / song, each with an optional ordinal range) resolved lazily, with only a bounded window in memory and in the player.
- (done) Streaming bitrate and preferred format from settings (`maxBitRate`/`format`, so the server transcodes as configured).
- (done) Playing from the Songs tab: the tapped song queues the whole songs list at that position.
- The queue view (add to queue / play next / reorder), and gapless format preferences.
- Scrobbling and "now playing" back to the server.
- Optional download/caching.

## Phase 4 - Search, playlists, offline

- Search UI over the FTS5 index (substring, ranked).
- Playlist browsing and editing (create/add/remove), syncing changes back to the server.
- Offline mode and downloads.

## Phase 5 - Release

- Change `applicationId` from `com.subtracks.next` to `com.subtracks`.
- Signing, versioning and Fastlane metadata.
- F-Droid submission. Their buildserver must have Gradle >= 9.6 and SDK 37, which this project targets; do not rely on the Gradle wrapper there (fdroidserver deletes `gradlew` and uses its own Gradle).

## Deferred decisions

- Whether to drop the plaintext-auth fallback or keep it behind an "insecure server" toggle.
- Robolectric runs at SDK 35 while `targetSdk` is 37; aligning them changes the on-demand Roborazzi renders (nothing committed to re-record).
- Whether to extract `:core:data` and friends once the UI has grown.
