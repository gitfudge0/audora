# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

Audora is a native Android app (Kotlin, Jetpack Compose, Material 3) for cleaning up a **local** music library on-device: editing metadata/tags, downloading synced `.lrc` lyrics from LRCLIB, and finding cover art via MusicBrainz / Cover Art Archive. It is not a player — it writes the metadata players consume. No account, no cloud, no upload of file data.

The package/namespace is `dev.gitfudge.audora`. minSdk 26, target/compileSdk 36, JDK 17.

## Commands

```bash
./gradlew test                 # JVM unit tests
./gradlew test --tests "dev.gitfudge.audora.domain.AlbumDuplicatesTest"  # single test class
./gradlew :app:assembleDebug   # debug APK
./run.sh                       # assembleDebug + adb install -r + launch on connected device
```

There is no emulator in this environment; device verification is done by the user via `./run.sh` on a physical device. Guide on-device test steps explicitly when changes need runtime verification.

Release builds require signing env vars (`ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`); without them the `release` signing config is simply not created. Release builds are minified + resource-shrunk (ProGuard).

## Architecture

Single-module (`:app`) app. Layered: `data/` ← `domain/` ← `ui/`, wired with Hilt (`di/`). ViewModels are `@HiltViewModel`, expose `StateFlow` UI state, and UI state shapes are modeled as `sealed interface` (e.g. `RootUiState` in `ui/MainViewModel.kt`).

**Storage model — read this before touching scan/write code.** The library is accessed through a single user-granted SAF tree URI (Storage Access Framework / `DocumentFile`), persisted in DataStore. The app does not own the files. Two consequences pervade the codebase:

- The persisted SAF grant can vanish (revoked, app data cleared, SD remount). `MainViewModel` detects this and routes to `RootUiState.PermissionLost` rather than failing writes silently. Treat SAF/SD-card/permission-loss as normal flow, not edge cases.
- Every audio file is precious and user-owned. Writes are deliberate and recoverable.

**Write safety pipeline (`data/tags/WriteSafetyManager`).** All destructive writes go through it: `assertUnchanged` (refuse if the on-disk file diverged from the last scan → `StaleFileException`, asking for a rescan), `backup` (copy original bytes to `cacheDir/backups`), `restore` (the ~30s undo). Backups are an aggressively-pruned undo buffer, not durable storage. `TagWriter` (text tags + artwork via `jaudiotagger`) and `BulkTagApplier` depend on it. Never assume a write succeeded unless the API confirms it.

**Data layer (`data/`):**
- `scan/MediaScanner` — walks the SAF tree, extracts tags, computes a content hash + lastModified, upserts into Room. Tuned for large libraries: batched writes minimize Room transactions (each write invalidates every observing query — the album rollup, filtered track list, counts), with throttled progress emission.
- `db/` — Room (`LibraryDatabase`, `TrackDao`, `TrackEntity`). The album view is a GROUP-BY rollup over tracks; there is no separate album table.
- `lyrics/` — `LrclibApi`/`LrclibRepository` (Retrofit) + `LrcReader`/`LrcWriter`/`LyricsFormat` for `.lrc` sidecar files written next to tracks.
- `art/` — `CoverArtRepository` + `MusicBrainzApi`.
- `settings/SettingsRepository` — DataStore-backed (`provideDataStore` in `di/DataModule`); source of truth for the music tree URI, walkthrough/onboarding flags, theme mode.
- `net/Interceptors` — OkHttp interceptors; keep external API usage respectful (avoid unnecessary calls, preserve clear failure states).

**Domain (`domain/`)** is pure, testable Kotlin: `buildAlbumKey` (album grouping — must stay in sync with the grouping in `MediaScanner`), `AlbumDuplicates`, `AlbumSummary`, `TrackStatus`, `BulkTagField`. Prefer putting parsing/matching/duplicate/format/write-planning logic here with unit tests over UI-only fixes.

**UI (`ui/`)** is Compose + Navigation. `AppRoot` is the NavHost and switches top-level destinations off `RootUiState` (Walkthrough → Onboarding → Library / PermissionLost). Feature packages: `library`, `album`, `detail`, `unfiled`, `onboarding`, `settings`; shared widgets in `components/` and `common/`. Theme system in `ui/theme/` is a deliberate monochrome design: the **only** chromatic colors are the three status hues (`ok`/`warn`/`missing`), always paired with icon + label. Light/dark are two equal modes from one OKLCH source.

## Conventions

- Product rule (`CONTRIBUTING.md`): **preview before commit** — any write of metadata/lyrics/artwork/sidecar must be deliberate, reviewable, and recoverable.
- Add/extend unit tests when changing parsing, matching, duplicate detection, formatting, or write-planning. Tests live in `app/src/test/` (JVM, no instrumentation).
- Keep changes focused; match the Kotlin/Compose style of nearby files; no unrelated refactors in feature/bugfix changes.
- Imperative commit messages (e.g. `Fix album duplicate grouping`). The repo's own automation appends a `Co-Authored-By` trailer to commits and a Claude Code footer to PR bodies.
