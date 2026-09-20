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

## Phase 2 - UI vertical slice (next)

Goal: a usable app for browsing a server, end to end.

- DI wiring (Koin) and DataStore-backed settings.
- App shell: root navigation and a Material 3 theme with cover-art tonal colour extraction.
- Source setup: add/edit sources (URL and credentials, token auth), ping/test, trigger a sync, show progress.
- Library browsing: artists, albums and songs with Paging 3 and Coil 3 cover art, reading from Room.
- Roborazzi screenshot tests and goldens for the new screens.

## Phase 3 - Playback

- Media3/ExoPlayer playback service, queue and now-playing UI.
- Streaming with `maxBitRate`/format taken from settings, a gapless queue, media notifications and headset controls.
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
- Robolectric runs at SDK 35 while `targetSdk` is 37; aligning them means re-recording the Roborazzi goldens.
- Whether to extract `:core:data` and friends once the UI has grown.
