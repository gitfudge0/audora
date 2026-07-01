# Multi-Path Scanning Design

## Status

Approved (brainstormed). Ready for implementation.

## Summary

Today Audora scans and views a single user-granted SAF tree URI. This spec extends the app to hold **multiple** library folders (a set of SAF tree URIs), scan each independently, and present a **merged union** of their tracks. Albums roll up across folders exactly as they do today. Permission loss is handled per-folder: valid folders keep working while broken ones are flagged, and the app only falls back to the blocking `PermissionLost` screen when *all* folders are lost. The change is broad but mostly mechanical — the scanner is already tree-scoped and `TrackEntity` already carries a `treeUri` column, so there is no Room schema change.

## Product decisions

1. **Permission loss is per-folder.** If one of several folders loses access, keep showing the valid folders' tracks and flag the broken one (dismissible banner in Library + "unavailable" badge in Settings). Route to the full blocking `PermissionLost` screen **only when every folder is lost**.
2. **Library is a merged union.** All folders combine into one view with **no per-folder filtering UI**. Albums roll up across folders, matching today's GROUP-BY rollup.

## 1. Storage — `data/settings/SettingsRepository.kt`

Replace the single-URI persistence with set-based persistence.

- Replace `music_tree_uri` (`stringPreferencesKey`) with `music_tree_uris` (`stringSetPreferencesKey`), exposed as `musicTreeUris: Set<String>` on the `Settings` data class (`SettingsRepository.kt:16-33`).
- Replace `last_scanned_tree_uri` with `scanned_tree_uris` (`stringSetPreferencesKey`). This set tracks which folders have already been scanned so auto-scan fires only for **newly-added** folders.
- **One-time DataStore migration on read** (in the `Settings` mapping): if `music_tree_uris` is absent but the old `music_tree_uri` is present, seed the set from it; likewise seed `scanned_tree_uris` from the old `last_scanned_tree_uri`. No separate migration step or Room touch — it happens transparently the first time the flow is read.
- New write APIs (`SettingsRepository.kt:40-73`):
  - `addMusicTreeUri(uri: String)` — add to the set.
  - `removeMusicTreeUri(uri: String)` — remove from the set.
  - `markTreeScanned(uri: String)` — add to `scanned_tree_uris`.
  - `unmarkTreeScanned(uri: String)` — remove from `scanned_tree_uris`.
- The set is **unordered** — folder order is irrelevant for the union view. Mark this at the declaration with:

  ```kotlin
  // ponytail: unordered Set is fine for a union view; upgrade to an ordered List<String> if the Settings UI ever needs stable folder order.
  ```

DataStore is provided by `provideDataStore` in `di/DataModule.kt:14-16`; no change there.

## 2. Scan — `data/scan/MediaScanner.kt`

**UNCHANGED.** `scan(treeUriString, onProgress)` (`MediaScanner.kt:76-79`) already scopes everything to the one tree:

- Prior signatures are fetched per tree via `trackDao.signatures(treeUriString)` (`MediaScanner.kt:85`).
- Stale-deletion is scoped to that tree's prior signatures (`MediaScanner.kt:191-194`), so removing files that vanished from one folder never touches another folder's rows.
- Each upserted row is written with its `treeUri` (`MediaScanner.kt:405-408`).

The only change is at the **caller**, which now invokes `scan(...)` once per folder.

## 3. LibraryViewModel — `ui/library/LibraryViewModel.kt`

- Replace `musicTreeUriFlow` (`LibraryViewModel.kt:205-207`) with a `musicTreeUrisFlow` sourced from `settings.musicTreeUris`.
- **Auto-scan gate** (`LibraryViewModel.kt:788-802`): compute the diff `selected − scanned` (see Testing §a) and scan **every** URI in it, calling `markTreeScanned(uri)` after each successful scan. This ensures a newly-added folder auto-scans once and existing folders are not re-scanned on every launch.
- **`doScan`** (`LibraryViewModel.kt:804-845`): scans a single URI as today; the loop lives in the gate/`rescan`.
- **`rescan()`** (`LibraryViewModel.kt:778-786`): loop over all folders and scan each.
- **TrackDao queries:** every query parameter changes from `treeUri: String` to `treeUris: List<String>`, and `WHERE treeUri = :treeUri` becomes `WHERE treeUri IN (:treeUris)` (`TrackDao.kt:23-139`).
- **Raw-query builder** `buildTrackWhereClause` (`LibraryViewModel.kt:914-951`): emit `treeUri IN (?, ?, …)` with one placeholder and one bound arg per URI.

This is the broadest but most mechanical part of the change.

## 4. MainViewModel — `ui/MainViewModel.kt`

Rework `uiState` derivation (`MainViewModel.kt:83-98`) to route off the set of folders and their per-URI access:

- **uris empty** → `Onboarding`.
- Otherwise compute valid vs. unavailable by calling `hasValidAccess(uri)` (`MainViewModel.kt:272-301`) per URI:
  - **all unavailable** → `PermissionLost`.
  - **some unavailable** → `Library`, with the unavailable ones surfaced.
  - **none unavailable** → normal `Library`.

`RootUiState.Library` (`MainViewModel.kt:33-47`) gains:

- `folderSummary: String` — e.g. `"3 folders"`.
- `unavailableFolders: List<String>` — the URIs currently lacking access.

`onFolderPicked` (`MainViewModel.kt:272-301`) takes persistable permission then **APPENDS** via `addMusicTreeUri(uri)` (not replace). The old single-folder `changeFolder` replace semantics are dropped.

New `removeFolder(uri)`:

1. `settings.removeMusicTreeUri(uri)`
2. `trackDao.clearTree(uri)` — the existing, currently-unused DAO method (`TrackDao.kt:63-64`).
3. `settings.unmarkTreeScanned(uri)`

`resolveFolderLabel` (`MainViewModel.kt:303-308`) is reused per folder for display.

## 5. UI

**Settings — "Library" section (`ui/settings/SettingsScreen.kt:185-195`).** The single folder row becomes a **list of folder rows**. Each row shows the folder label, a remove/trash action (→ `removeFolder`), and an **"unavailable" badge** when that folder's grant is lost. Below the list, an **"Add folder" row** reuses the existing SAF launcher.

**Library banner.** When `unavailableFolders` is non-empty, show a **dismissible banner**: `"N folder(s) unavailable — tap to fix"`, wired to the SAF picker for re-grant.

**AppRoot (`ui/AppRoot.kt`).** The single `OpenDocumentTree` launcher (`AppRoot.kt:63-65`) stays and now **appends** (via `onFolderPicked` → `addMusicTreeUri`). Its existing reuse sites (`AppRoot.kt:90-96,140,158`) continue to work; the picker is shared by onboarding, Settings "Add folder", and the Library banner.

## 6. Testing

Extract two **pure helpers into `domain/`**, each with a JVM unit test in `app/src/test/`, following the project convention (parsing/matching/planning logic lives in `domain/` with tests):

- **(a) Scan-set diff.** `selected − scanned` → the set of URIs needing an auto-scan. Trivial set difference, but pulling it out of the ViewModel makes the auto-scan gate testable.
- **(b) Folder-routing decision.** Given the folder set and per-URI access, decide the `RootUiState`: empty → Onboarding; all-lost → PermissionLost; partial-lost → Library (with unavailable list); all-valid → Library. Cover all four branches.

## Non-goals

- **Per-folder filtering UI** — the Library view is a merged union only.
- **Folder ordering** — the set is unordered; no reorder UI.
- **Releasing persisted URI permissions on remove** — Android's 512-grant cap is irrelevant for a handful of folders, so `removeFolder` does not call `releasePersistableUriPermission`.
- **Room schema change** — `TrackEntity` already has a `treeUri` column (`TrackEntity.kt:24`) with tree-scoped indices (`TrackEntity.kt:13-20`), so `fallbackToDestructiveMigration` is not even triggered.
