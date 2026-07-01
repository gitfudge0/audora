package dev.gitfudge.audora.domain

/**
 * Pure decision helpers for the multi-folder library. Kept out of the view
 * models so the auto-scan gate and the top-level routing are unit-testable
 * without Android (SAF/DataStore) dependencies.
 */

/**
 * (a) Scan-set diff: the folders that still need an auto-scan.
 *
 * A folder auto-scans exactly once — the first time it is seen. [selected] is
 * every library folder; [scanned] is the set already scanned at least once.
 * The difference is the set of newly-added folders to scan now.
 */
fun treesNeedingScan(selected: Set<String>, scanned: Set<String>): Set<String> =
    selected - scanned

/** How a folder should be routed given the set of folders and their access. */
enum class FolderRoute { ONBOARDING, PERMISSION_LOST, LIBRARY }

/** One folder and whether its SAF grant currently resolves. */
data class FolderAccess(val uri: String, val available: Boolean)

/**
 * The routing decision plus the URIs that are currently lost (empty unless the
 * route is [FolderRoute.LIBRARY] with a partial loss).
 */
data class FolderRouting(val route: FolderRoute, val unavailable: List<String>)

/**
 * (b) Folder-routing decision. No folders → onboarding. Every folder lost →
 * the blocking permission-lost screen. Otherwise the library, surfacing any
 * partially-lost folders so they can be flagged rather than blocking the app.
 */
fun decideFolderRouting(folders: List<FolderAccess>): FolderRouting {
    if (folders.isEmpty()) return FolderRouting(FolderRoute.ONBOARDING, emptyList())
    val unavailable = folders.filterNot { it.available }.map { it.uri }
    return when {
        unavailable.size == folders.size ->
            FolderRouting(FolderRoute.PERMISSION_LOST, unavailable)
        else -> FolderRouting(FolderRoute.LIBRARY, unavailable)
    }
}
