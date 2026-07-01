package dev.gitfudge.audora.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MultiFolderTest {

    // ── (a) scan-set diff ───────────────────────────────────────────────────

    @Test
    fun onlyNewlyAddedFoldersNeedScanning() {
        assertEquals(
            setOf("c"),
            treesNeedingScan(selected = setOf("a", "b", "c"), scanned = setOf("a", "b")),
        )
    }

    @Test
    fun nothingToScanWhenAllScanned() {
        assertEquals(
            emptySet<String>(),
            treesNeedingScan(selected = setOf("a", "b"), scanned = setOf("a", "b", "gone")),
        )
    }

    @Test
    fun everyFolderScansWhenNoneScannedYet() {
        assertEquals(
            setOf("a", "b"),
            treesNeedingScan(selected = setOf("a", "b"), scanned = emptySet()),
        )
    }

    // ── (b) folder-routing decision ─────────────────────────────────────────

    @Test
    fun emptyFoldersRouteToOnboarding() {
        val routing = decideFolderRouting(emptyList())
        assertEquals(FolderRoute.ONBOARDING, routing.route)
        assertEquals(emptyList<String>(), routing.unavailable)
    }

    @Test
    fun allUnavailableRouteToPermissionLost() {
        val routing = decideFolderRouting(
            listOf(FolderAccess("a", false), FolderAccess("b", false)),
        )
        assertEquals(FolderRoute.PERMISSION_LOST, routing.route)
        assertEquals(listOf("a", "b"), routing.unavailable)
    }

    @Test
    fun someUnavailableRouteToLibraryWithUnavailableList() {
        val routing = decideFolderRouting(
            listOf(FolderAccess("a", true), FolderAccess("b", false)),
        )
        assertEquals(FolderRoute.LIBRARY, routing.route)
        assertEquals(listOf("b"), routing.unavailable)
    }

    @Test
    fun allAvailableRouteToLibraryWithNoUnavailable() {
        val routing = decideFolderRouting(
            listOf(FolderAccess("a", true), FolderAccess("b", true)),
        )
        assertEquals(FolderRoute.LIBRARY, routing.route)
        assertEquals(emptyList<String>(), routing.unavailable)
    }
}
