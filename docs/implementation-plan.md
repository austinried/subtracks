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

## Phase 3 - Playback (done)

- (done) Media3/ExoPlayer playback service, queue and now-playing UI: a `MediaSessionService` plus a mini player and a Now Playing screen.
- (done) Media notifications and headset/Bluetooth controls, from the media session.
- (done) A SQL-backed queue of references (whole playlist / album / song, each with an optional ordinal range) resolved lazily, with only a bounded window in memory and in the player.
- (done) Streaming bitrate and preferred format from settings (`maxBitRate`/`format`, so the server transcodes as configured).
- (done) A queue view that lists the resolved queue, jumps to a track on tap, removes tracks and reorders them by dragging, with a one-step undo.
- (done) Adding to the queue from the library (play next / add to queue), backed by a separate, editable up-next list that plays between the current track and the rest of the context.

## Phase 4 - Search, playlists, offline (done)

- (done) Search: a substring filter on the library queries, scoped to the active tab, with a docked search field and per-tab sort/filter controls. Queries of three or more characters run through per-entity FTS5 trigram tables (external content, trigger-maintained), with the `instr` scan as the below-three-character fallback.
- (done) Offline mode and downloads.

## Phase 5 - Release

- Change `applicationId` from `com.subtracks.next` to `com.subtracks`.
- Signing, versioning and Fastlane metadata.
- F-Droid submission. Their buildserver must have Gradle >= 9.6 and SDK 37, which this project targets; do not rely on the Gradle wrapper there (fdroidserver deletes `gradlew` and uses its own Gradle).

## Scale hardening (done)

Follow-up work for very large libraries, playlists and long sessions, grouped so each block could be one PR. All blocks have landed.

- **Library read path.** Landed: the sort keys used by album/artist/playlist browsing are indexed (a denormalized `albumArtist` on `albums`, `NOCASE` text, one index per order). Row resolution seeks through a cached keyset cursor (`QueueRepository.keyedRows` over `QueueDao`'s `...From`/`...Before`/`...After` queries); the old offset query is kept only as the cold and far-jump fallback.
- **Shuffle.** Landed: the permutation is derived from `(seed, size)` (a seekable Feistel in `Shuffle.kt`) and positions resolve through the same positional lookup, so `shuffle_order` and the retained whole-order/whole-id arrays are gone. The context is read-only while shuffled; manual ordering goes through the up-next block.
- **Sync pipeline.** Landed: the prune diffs against a memory-bounded set of primitive `long` id hashes and walks the local ids in keyset pages; `fetchRanks` and the write-only rank columns are gone; playlists and the album-song fallback are fetched with a bounded, user-configurable concurrency; the page cap is raised to 20k pages and overrun fails with a "Library is too large to sync" error; unchanged rows are not rewritten (`upsertChanged`'s changed-only `WHERE`).
- **Queue view and edits.** Landed: the queue view loads a bounded window (`QUEUE_CHUNK * 3`) and extends and trims it as you scroll, its ViewModel is scoped to the overlay, adjacent same-ref ranges are compacted after an edit, and resolved entry lengths are cached.
- **Artwork.** Landed: `artwork_seeds` carries a `sourceId` with `ON DELETE CASCADE` and an index, is pruned as an LRU by `storedAt`, and the seed prefetch requests the thumbnail.
- **FTS5 search.** Landed: per-entity external-content trigram tables (`album_search`, `artist_search`, `playlist_search`) with Room-generated content-sync triggers, a three-character floor and an `instr` fallback below it.
- **UI/flow overhead.** Landed: only the active tab's paging flow is collected, and the position ticker updates its own `positionMs` flow instead of the shared `PlaybackState`.

A `syncGen` column was considered for the sync prune and rejected: it is part of the upsert's update set, so its always-changing value defeats `upsertChanged`'s changed-only `WHERE` predicate and rewrites every row on every full sync. The prune should instead keep the two-phase diff with a memory-bounded id set (primitive long hashes) or diff a staging `seen` table in SQL.

## Deferred decisions

- Robolectric runs at SDK 35 while `targetSdk` is 37; aligning them changes the on-demand Roborazzi renders (nothing committed to re-record).
- Whether to extract `:core:data` and friends once the UI has grown.
