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
- Unit tests: `gradle :app:testDebugUnitTest`
- Lint: `gradle :app:lintDebug`
- Integration tests: `./tools/integration-test.nu` (starts navidrome and gonic, then runs `:app:integrationTest`)
- Screenshots: `gradle :app:recordRoborazziDebug` to record, `gradle :app:verifyRoborazziDebug` to check

## Testing layout

- `app/src/test` - JVM unit tests (JUnit4, Robolectric, Roborazzi, MockWebServer). Run by `:app:testDebugUnitTest`.
- `app/src/integrationTest` - tests that need real servers. Compiled with the unit test sources but run by the dedicated `:app:integrationTest` task, and excluded from every `*UnitTest` task. The nushell harness starts the servers for you.

## CI

- Forgejo Actions, `.forgejo/workflows/ci.yml`, with `unit`, `lint` and `integration` jobs.
- Runs on the `nix-docker` runner (the `localhost/nix-ci` image built by the separate `nix-home` repo) inside the devshell, restoring the Nix store via `cache-nix-action` and caching Gradle and the integration test music.
- Pull requests from a fork need approval before the workflow runs.

## Conventions

- Prefer self-explanatory code; do not add comments unless the reason cannot be inferred from the code itself (docstrings for functions and exports are fine).
- Room 3 (`androidx.room3`) on AndroidX `sqlite-bundled` (`BundledSQLiteDriver`, SQLite 3.50+, FTS5). Do not reintroduce SQLDelight or the platform SQLite.
- Networking uses OkHttp and DOM XML parsing; auth uses the Subsonic token scheme by default.
- Use coroutines and `Flow`; library reads are exposed as `Flow` from Room.
- Verify current dependency versions and their compatibility before adding or pinning anything.

## Gotchas

- Robolectric runs at SDK 35 while `targetSdk` is 37; raising it means re-recording the Roborazzi goldens.
- `applicationId` is `com.subtracks.next` for the beta; change it to `com.subtracks` before any store release.
- The CI image provides `nix-ld`, `jq`, `sqlite`, `node`, `zstd` and a `runner` user that `cache-nix-action` expects. None of that is needed locally beyond the devshell.
- Integration servers: navidrome on 4533 (`admin`/`password`), gonic on 4747 (`admin`/`admin`). The test music is cached in `.integration/music` (gitignored).
- `nix develop` does not source your shell rc files (so tools like atuin are not active); use direnv if you want your normal interactive shell.
