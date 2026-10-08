<h1><img src=".assets/logo.png" alt="subtracks logo" width="44" align="texttop" /> subtracks</h1>

[![f-droid](https://img.shields.io/f-droid/v/com.subtracks?color=007ec6&label=f-droid&logo=fdroid&logoColor=1f78d2)](https://f-droid.org/en/packages/com.subtracks)
[![github](https://img.shields.io/github/v/release/austinried/subtracks?label=github&logo=github)](https://github.com/austinried/subtracks/releases/latest/)
[![downloads](https://img.shields.io/github/downloads/austinried/subtracks/total?label=downloads&logo=github)](https://github.com/austinried/subtracks/releases/)
<a href="https://hosted.weblate.org/engage/subtracks/"><img src="https://hosted.weblate.org/widget/subtracks/svg-badge.svg" alt="Translation status"></a>
[![license](https://img.shields.io/github/license/austinried/subtracks?color=222&label=license)](LICENSE)

subtracks is an open source Android client for [Subsonic-compatible](http://www.subsonic.org/pages/api.jsp) servers ([Navidrome](https://www.navidrome.org/), [gonic](https://github.com/sentriz/gonic), [LMS](https://github.com/epoupon/lms), [Nextcloud Music](https://apps.nextcloud.com/apps/music) and more). It is written natively in Kotlin and Jetpack Compose and gives you clean, convenient access to your music in the style of a modern media player.

## Screenshots

<div align="center">
  <a href="metadata/en-US/images/phoneScreenshots/01_home.png"><img src="metadata/en-US/images/phoneScreenshots/01_home.png" alt="home" width="150"/></a>
  <a href="metadata/en-US/images/phoneScreenshots/02_now-playing.png"><img src="metadata/en-US/images/phoneScreenshots/02_now-playing.png" alt="now playing" width="150"/></a>
  <a href="metadata/en-US/images/phoneScreenshots/03_library-albums.png"><img src="metadata/en-US/images/phoneScreenshots/03_library-albums.png" alt="albums" width="150"/></a>
  <a href="metadata/en-US/images/phoneScreenshots/04_album.png"><img src="metadata/en-US/images/phoneScreenshots/04_album.png" alt="album" width="150"/></a>
  <a href="metadata/en-US/images/phoneScreenshots/05_artist.png"><img src="metadata/en-US/images/phoneScreenshots/05_artist.png" alt="artist" width="150"/></a>
</div>

## Download

<div align="center">
  <a href="https://play.google.com/store/apps/details?id=com.subtracks"><img src=".assets/google-play-badge.png" width="250"/></a>
  <a href="https://f-droid.org/en/packages/com.subtracks/"><img src=".assets/f-droid-badge.png" width="250"></a>
  <a href="https://github.com/austinried/subtracks/releases/"><img src=".assets/github-badge.png" width="250"/></a>
</div>

## Verifying releases

APKs are signed. Every GitHub release attaches `SHA256SUMS` and the signing certificate (`release-certificate.pem`); the `next` build key is committed. See [signing/README.md](signing/README.md) for the fingerprints and the commands to verify a download.

## Features

- **Large Libraries:** Built for very large libraries, from syncing the whole collection to playing playlists with thousands of tracks.
- **Downloads:** Song and bulk downloads, with download quality (encoding, bitrate) and metered-connection settings.
- **Offline Mode:** Stars, scrobbles and play counts queue up while you're offline and sync back to the server when you're online again.
- **Artwork-first:** Album and artist art everywhere, shown full-resolution in the detail and now playing views.
- **Adaptive Theming:** Colours drawn from the current song's artwork.
- **Search:** Fast search across the whole library, with per-list sort and filter controls.
- **Playback:** Gapless playback, an editable queue you can reorder and jump through, plus loop and shuffle modes.
- **Up Next:** Add items into the currently playing queue without rewriting it.
- **Multiple Servers:** Connect several servers and switch between them quickly.
- **Stream Quality:** Bitrate and format settings for Wi-Fi and mobile, with automatic switching.
- **System Controls:** Media notification and headset/Bluetooth controls.

## Building

The project's entire toolchain is provided by a Nix flake. The default devshell provides JDK, Gradle and the Android SDK, so there is nothing to install by hand.

Therefore, to get started, you'll need to install Nix. I prefer [Lix](https://lix.systems/install/).

```sh
nix develop
gradle :app:assembleDebug
```

Test, lint and screenshot commands, plus the integration harness, are documented in [AGENTS.md](AGENTS.md). The architecture is described in [docs/architecture.md](docs/architecture.md).

## Contributing

The GitHub repository [austinried/subtracks](https://github.com/austinried/subtracks) is a mirror of the repo hosted on my personal (private) code forge that syncs whenever I commit to it.

Bug reports and feature requests are accepted on GitHub, as well as pull requests, but:
* A well-crafted issue with clear reproduction and logs for bugs or mock-ups and use case details for features is preferred over a pull request
* Pull requests will be pulled in as patches to my code forge and then synced back (you'll still be the author, I'll be shown as the committer)

## Translations

Want to see subtracks in your language? Visit the project on [Weblate](https://hosted.weblate.org/engage/subtracks/) to help!

<a href="https://hosted.weblate.org/engage/subtracks/">
<img src="https://hosted.weblate.org/widget/subtracks/subtracks/multi-auto.svg" alt="Translation status" />
</a>

## AI usage disclosure

This project contains code and documentation generated by LLM, which is created via human prompting, iteration and review. The current native rewrite is based on two (and a half) other iterations of this project that I coded by hand, so the architecture and other decisions behind that are all still coming from me.

## License

subtracks is free software, licensed under the [GNU General Public License v3.0](LICENSE). It has no ads and no telemetry, and uses only free/libre dependencies.
