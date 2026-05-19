<p align="center">
  <img src="brand_assets/png/logo/audora-logo-with-tagline-black-2400.png" alt="Audora - Your music, perfectly organized." width="760">
</p>

<p align="center">
  <a href="https://github.com/gitfudge0/audora/releases"><img alt="Release" src="https://img.shields.io/github/v/release/gitfudge0/audora?include_prereleases&style=for-the-badge&label=release"></a>
  <a href="https://github.com/gitfudge0/audora/actions/workflows/release-build.yml"><img alt="Release Build" src="https://img.shields.io/github/actions/workflow/status/gitfudge0/audora/release-build.yml?style=for-the-badge&label=release%20build"></a>
  <img alt="Android" src="https://img.shields.io/badge/android-8.0%2B-1B1D22?style=for-the-badge&labelColor=F1F2F5">
  <img alt="Kotlin" src="https://img.shields.io/badge/kotlin-2.1-1B1D22?style=for-the-badge&labelColor=F1F2F5">
</p>

Audora is a native Android workspace for cleaning up a local music library.

It is built for people who keep real audio files on their phone: ripped CDs, Bandcamp and Qobuz downloads, archive folders, and years of tracks that deserve better than missing covers, unknown artists, and silent lyric screens.

Audora is not another music player. It edits the metadata, lyrics, and artwork that every player already depends on.

## What It Does

- Scans a local folder and builds album and track views from the files on-device.
- Surfaces missing artwork, low-resolution artwork, missing lyrics, incomplete tags, unknown artists, duplicate albums, and unfiled tracks.
- Edits track metadata and album-level tags with explicit review before writing.
- Downloads synced or plain lyrics from LRCLIB and saves `.lrc` sidecar files next to tracks.
- Searches MusicBrainz and Cover Art Archive for cover art, previews candidates, and writes selected artwork into files.
- Supports bulk cleanup for selected tracks or albums.
- Keeps the library local: no account, no upload, no cloud sync.

## Why Audora Exists

Music players are great at reading metadata, but they rarely help fix it. Desktop tag editors are powerful, but the messy library is often already on the phone.

Audora closes that gap. Pick a folder, inspect what is missing, and make deliberate changes where the files live. The goal is simple: fewer `Unknown artist` rows, fewer blank covers, more synced lyrics, and a library that looks right in any player, lock screen, widget, or car head unit.

## Install

Download the latest draft or published build from [GitHub Releases](https://github.com/gitfudge0/audora/releases).

- Use `app-release.apk` for direct installation on Android devices.
- Use `app-release.aab` for Android App Bundle distribution workflows.

Audora targets Android 8.0 and newer.

> Metadata editing writes to your audio files. Keep a backup of any library you care about before large write operations.

## Build From Source

Requirements:

- JDK 17
- Android SDK
- Android Studio or the Android Gradle Plugin toolchain

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

Run unit tests:

```bash
./gradlew test
```

Install and launch a debug build on a connected device:

```bash
./run.sh
```

Build signed release artifacts by providing the signing environment expected by `app/build.gradle.kts`:

```bash
ANDROID_KEYSTORE_FILE=/path/to/release.jks \
ANDROID_KEYSTORE_PASSWORD=... \
ANDROID_KEY_ALIAS=... \
ANDROID_KEY_PASSWORD=... \
./gradlew assembleRelease bundleRelease
```

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore
- WorkManager
- Retrofit, OkHttp, kotlinx.serialization
- Coil
- jaudiotagger

## Supported Audio Discovery

Audora scans common local audio extensions, including:

`mp3`, `flac`, `m4a`, `m4b`, `aac`, `ogg`, `oga`, `opus`, `wav`, `wv`, `ape`, `mpc`, `aif`, `aiff`, `wma`, and `alac`.

Actual tag-writing support depends on Android storage access and the underlying audio/tag format support available through the app's writer stack.

## Services Used

- [LRCLIB](https://lrclib.net/) for synced and plain lyric lookup.
- [MusicBrainz](https://musicbrainz.org/) and [Cover Art Archive](https://coverartarchive.org/) for album identity and artwork lookup.

## License

Audora is licensed under the [Apache License 2.0](LICENSE).
