# Implementation plan

The rewrite is delivered in vertical slices. Each phase leaves the app in a working, testable state.

## Phase 0 - Foundation (done)

Nix devshell (JDK, Gradle, Android SDK, nushell, navidrome, gonic), Gradle Kotlin DSL at the repo root, a Compose skeleton, Roborazzi/Robolectric screenshot testing, and the carried-over launcher and notification assets. The app ships as `com.subtracks.next`.

## Phase 1 - Data foundation (done)

Everything needed to mirror a server library locally:

- `MusicSource` abstraction with `SubsonicClient`, `SubsonicXml` and `SubsonicSource`.
- Room 3 schema on bundled SQLite, so the SQLite version is identical on every device.
- `SyncService`: stream each entity in batches, upserting per batch and pruning the IDs the server no longer lists; the sync is not atomic.
- Unit tests (client, XML, sync), native integration tests against navidrome and gonic, and CI (`unit`, `lint`, `integration`) with warm caches.

## Phase 2 - UI vertical slice (done)

Goal: a usable app for browsing a server, end to end.

- Koin DI and DataStore-backed preferences (album sort); the unused `app_settings` Room table was removed in favour of DataStore.
- App shell: a root gate that shows the add-server flow until a server exists, then the tabbed library screen matching the Flutter app (icon-only tabs for albums, artists and playlists, with sync and settings actions), plus album and playlist detail screens and a monochrome theme.
- Source setup: name/address/username/password with token-auth toggle, a connection test, save-and-sync, plus server switching and removal in settings.
- Library browsing: albums (covers-only grid), artists and playlists paged from Room (`PagingSource`) and rendered with `LazyPagingItems`, with Coil cover art served from stable media URLs.
- Artist detail: tapping an artist opens their albums, newest first, and an album can be opened from there.
- Roborazzi screenshots for the new screens, rendered on demand for review (not committed).

Not done yet in this slice (deliberately): cover-art tonal colour extraction, long-press menus, editing an existing server, and per-section sort/filter controls (only album sort is wired).

## Phase 3 - Playback (in progress)

- (done) Media3/ExoPlayer playback service, queue and now-playing UI: a `MediaSessionService` plus a mini player and a Now Playing screen.
- (done) Media notifications and headset/Bluetooth controls, from the media session.
- (done) A SQL-backed queue of references (whole playlist / album / song, each with an optional ordinal range) resolved lazily, with only a bounded window in memory and in the player.
- (done) Streaming bitrate and preferred format from settings (`maxBitRate`/`format`, so the server transcodes as configured).
- (done) A queue view that lists the resolved queue, jumps to a track on tap, removes tracks and reorders them by dragging, with a one-step undo.
- (done) Adding to the queue from the library (play next / add to queue), backed by a separate, editable up-next list that plays between the current track and the rest of the context.
- Gapless format preferences.
- Scrobbling and "now playing" back to the server.
- Optional download/caching.

## Phase 4 - Search, playlists, offline

- (done) Search: a substring filter on the library queries, scoped to the active tab, with a docked search field and per-tab sort/filter controls.
- Playlist browsing and editing (create/add/remove), syncing changes back to the server.
- Offline mode and downloads.

## Phase 5 - Release

- Change `applicationId` from `com.subtracks.next` to `com.subtracks`.
- Signing, versioning and Fastlane metadata.
- F-Droid submission. Their buildserver must have Gradle >= 9.6 and SDK 37, which this project targets; do not rely on the Gradle wrapper there (fdroidserver deletes `gradlew` and uses its own Gradle).

## Scale hardening backlog

Follow-up work for very large libraries, playlists and long sessions, grouped so each block can be one PR.

- **Library read path.** Index the sort keys used by album/artist/playlist browsing (a denormalized `albumArtist` on `albums` plus `NOCASE` indices) and replace `LIMIT 1 OFFSET` with keyset/cursor seeks in `QueueRepository`/`QueueDao`. Shared dependency for the read-side fixes; can split into "indices" then "keyset" if too large for one review.
- **Shuffle.** Drop `shuffle_order` and the retained whole-order/whole-id arrays; derive the permutation from `(seed, size)` and resolve positions through the same positional lookup. Recommended after the read-path PR, since it shares `QueueRepository`.
- **Sync pipeline.** Prune with a memory-bounded id set, remove `fetchRanks` and the write-only rank columns, fetch playlists/albums with bounded concurrency and skip unchanged ones, and raise/make configurable the page cap with a clear "library too large" failure. Independent of the read path.
- **Queue view and edits.** Bound the queue view's loaded rows (or page over the queue ordinal) and scope its ViewModel to the overlay; compact adjacent same-ref ranges after an edit and cache resolved entry lengths. The view half is independent; the edit half shares `QueueRepository` with the read-path PR.
- **Artwork.** Give `artwork_seeds` a `sourceId` with `ON DELETE CASCADE` and an LRU/TTL (or drop the table and rely on Coil's disk cache); prefetch the thumbnail, not the original, on now-playing transitions. Independent.
- **FTS5 search.** Deferred; the revival requirements are recorded in `architecture.md` (per-entity trigram tables indexing every scanned field, incremental maintenance, three-character floor).
- **UI/flow overhead.** Subscribe only the active tab's paging flow; move the position ticker off the shared `PlaybackState`. Independent.

A `syncGen` column was considered for the sync prune and rejected: it is part of the upsert's update set, so its always-changing value defeats `upsertChanged`'s changed-only `WHERE` predicate and rewrites every row on every full sync. The prune should instead keep the two-phase diff with a memory-bounded id set (primitive long hashes) or diff a staging `seen` table in SQL.

## Deferred decisions

- Whether to drop the plaintext-auth fallback or keep it behind an "insecure server" toggle.
- Robolectric runs at SDK 35 while `targetSdk` is 37; aligning them changes the on-demand Roborazzi renders (nothing committed to re-record).
- Whether to extract `:core:data` and friends once the UI has grown.
