# Contributing

Thanks for helping improve Audora. This project is an Android app that reads and writes local music metadata, so contributions should optimize for correctness, clear review, and not damaging user files.

## Ways to Contribute

- Report reproducible bugs with device, Android version, app version, file format, and storage location.
- Propose focused improvements to scanning, metadata editing, lyric lookup, cover art lookup, or bulk cleanup flows.
- Improve tests for domain logic, file-format handling, and data transformations.
- Improve documentation, release notes, and brand assets.

Do not attach copyrighted music files to issues or pull requests. Use small synthetic files, public-domain samples, screenshots, logs, or metadata descriptions instead.

## Before You Start

For user-facing changes, read:

- [README.md](README.md)

Audora's product rule is simple: preview before commit. Any change that writes metadata, lyrics, artwork, or sidecar files must make the write deliberate, reviewable, and recoverable wherever practical.

## Development Setup

Requirements:

- JDK 17
- Android SDK
- Android Studio or the Android Gradle Plugin command-line toolchain

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

Build signed release artifacts only when you have the required signing environment:

```bash
ANDROID_KEYSTORE_FILE=/path/to/release.jks \
ANDROID_KEYSTORE_PASSWORD=... \
ANDROID_KEY_ALIAS=... \
ANDROID_KEY_PASSWORD=... \
./gradlew assembleRelease bundleRelease
```

## Code Guidelines

- Keep changes focused. Avoid unrelated refactors in feature or bug-fix pull requests.
- Follow the existing Kotlin and Jetpack Compose style in nearby files.
- Prefer explicit, testable domain logic over UI-only fixes for metadata behavior.
- Keep storage and file-writing code conservative. Never assume a write succeeded unless the API confirms it.
- Treat Android storage access, SD cards, SAF permissions, and mixed audio formats as normal cases, not edge cases.
- Add tests when changing parsing, matching, duplicate detection, formatting, or write-planning behavior.
- Keep network integrations respectful of external services. Avoid unnecessary calls and preserve clear failure states.

## Pull Requests

Before opening a pull request:

- Run `./gradlew test`.
- Run `./gradlew :app:assembleDebug` for app changes.
- Manually test write-related flows with disposable files, not a primary music library.
- Update documentation when behavior, setup, or release workflows change.

Pull request descriptions should include:

- What changed.
- Why it changed.
- How it was tested.
- Any known limitations or follow-up work.
- Screenshots or screen recordings for visible UI changes.

## Commit Style

Use short, imperative commit messages:

- `Fix album duplicate grouping`
- `Add lyric sidecar formatting tests`
- `Document release signing setup`

Keep commits logically grouped so review can follow the intent.

## Safety Expectations

Audora handles user-owned files. Contributions must preserve these expectations:

- No silent destructive writes.
- No surprise uploads of local file data.
- No account or cloud dependency for core local-library cleanup.
- Clear error states when permissions, network calls, or file formats block an operation.

When in doubt, choose the path that protects the user's library and explains what happened.
