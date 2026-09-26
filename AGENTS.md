# AGENTS.md

Guidance for agents (and humans) working in this repository.

## What this is

A native Android rewrite of Subtracks, a client for Subsonic-compatible servers (Navidrome, gonic, Airsonic, ...), in Kotlin and Jetpack Compose. The `main` history still contains the earlier Flutter app; the native app is being built fresh under `app/` and ships as `com.subtracks.next` for side-by-side beta. The aim is a clean, F-Droid-friendly client: free/libre dependencies only, no telemetry.

## Environment

Everything comes from the Nix flake devshell; do not install toolchains by hand.

- Enter it with `nix develop`, or let direnv load it into your shell (`.envrc` runs `use flake`).
- The devshell provides JDK 21, Gradle 9, the Android SDK (platform 37, build-tools 37.0.0, platform-tools 37.0.1), nushell, navidrome and gonic.
- Build with the devshell `gradle`, not `./gradlew`. CI uses the nix Gradle pinned by `flake.lock`; the wrapper is kept only for people without Nix.

## Commands

- Build: `gradle :app:assembleDebug`
- Release build: `gradle :app:assembleRelease` only when build config, R8/ProGuard or dependencies change, and once before a merge; not part of routine verification
- Unit tests: `gradle :app:testDebugUnitTest`
- Lint: `gradle :app:ktlintCheck :app:lintDebug`
- Format: `gradle :app:ktlintFormat`
- Integration tests: `./tools/integration-test.nu` (starts navidrome and gonic, then runs `:app:integrationTest`)
- Screenshots: `gradle :app:recordRoborazziDebug` renders the `*Screen` composables to `app/src/test/screenshots/` (gitignored) plus an HTML report in `app/build/reports/roborazzi/`, for local review. No golden images are committed and `verifyRoborazziDebug` is not part of CI.

## Testing layout

- `app/src/test` - JVM unit tests (JUnit4, Robolectric, Roborazzi, MockWebServer). Run by `:app:testDebugUnitTest`.
- `app/src/integrationTest` - tests that need real servers. Compiled with the unit test sources but run by the dedicated `:app:integrationTest` task, and excluded from every `*UnitTest` task. The nushell harness starts the servers for you.
- A test earns its place only if it fails when the behaviour it covers breaks, and passes when the behaviour works. A test that passes either way is worse than no test: it looks like coverage while hiding the gap. So when you add one, break the behaviour (or run the test against the pre-fix code), watch it fail, then restore the fix and watch it pass.

## CI

- Forgejo Actions, `.forgejo/workflows/ci.yml`, a single `ci` job that runs the lint, unit test and integration test steps (one cache restore/save and Gradle warm-up instead of three).
- Runs on the `nix-docker` runner (the `localhost/nix-ci` image built by the separate `nix-home` repo) inside the devshell, restoring the Nix store via `cache-nix-action` and caching Gradle and the integration test music.
- Pull requests from a fork need approval before the workflow runs.

## Conventions

- Prefer self-explanatory code; do not add comments unless the reason cannot be inferred from the code itself (docstrings for functions and exports are fine).
- ktlint (`org.jlleitschuh.gradle.ktlint`) enforces style and flags unused imports; run `gradle :app:ktlintFormat` before committing. `.editorconfig` exempts `@Composable` functions from the lowerCamelCase function-naming rule.
- Room 3 (`androidx.room3`) on AndroidX `sqlite-bundled` (`BundledSQLiteDriver`, SQLite 3.50+). Do not reintroduce SQLDelight or the platform SQLite.
- Networking uses OkHttp and DOM XML parsing; auth uses the Subsonic token scheme by default.
- Use coroutines and `Flow`; library reads are exposed as `Flow` from Room. Large lists page with Paging 3 over Room `PagingSource` rather than loading the whole table.
- Playback is Media3: `PlaybackService` is a `MediaSessionService` owning an `ExoPlayer`, and `PlaybackController` (app-scoped Koin singleton) is the only thing the UI talks to. The queue is a `queue_entries` list of references with optional ranges plus a `playback_cursor` row; keep only a bounded window in memory (never the whole queue, and never load a whole table to build one). Stream URLs are freshly salted per request, so attach artwork by `CoverArtRef` cache key, not by URL.
- UI state lives in `androidx.lifecycle.ViewModel`, wired with Koin (`koinViewModel()`); screens split into a stateful `*Route` and a stateless `*Screen` for screenshot tests.
- UI and playback preferences go in DataStore (`UserPreferences`), not Room.
- Verify current dependency versions and their compatibility before adding or pinning anything.

## Gotchas

- Robolectric runs at SDK 35 while `targetSdk` is 37; raising it changes the on-demand Roborazzi renders (there are no committed goldens to re-record).
- Room 3 does not map `PagingSource` automatically: a DAO that returns it needs `@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)`.
- Material icons are not pulled in by `material3`; use the BOM-managed `material-icons-core`/`material-icons-extended` (frozen at 1.7.8). Extended is large in debug builds but R8 strips unused icons from release.
- Robolectric creates a fresh `Application` per test in one JVM, so `SubtracksApp.onCreate` stops any running Koin before `startKoin`.
- `applicationId` is `com.subtracks.next` for the beta; change it to `com.subtracks` before any store release.
- The CI image provides `nix-ld`, `jq`, `sqlite`, `node`, `zstd` and a `runner` user that `cache-nix-action` expects. None of that is needed locally beyond the devshell.
- Integration servers: navidrome on 4533 (`admin`/`password`), gonic on 4747 (`admin`/`admin`). The test music is cached in `.integration/music` (gitignored).
- `nix develop` does not source your shell rc files (so tools like atuin are not active); use direnv if you want your normal interactive shell.
