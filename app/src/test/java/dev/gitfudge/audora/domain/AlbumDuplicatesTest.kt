package dev.gitfudge.audora.domain

import dev.gitfudge.audora.data.db.TrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumDuplicatesTest {

    @Test
    fun normalizeFoldsCaseSpaceBracketsAndDiacritics() {
        assertEquals("greatest hits", normalizeAlbumTitle("Greatest Hits "))
        assertEquals("greatest hits", normalizeAlbumTitle("greatest  hits"))
        assertEquals("greatest hits", normalizeAlbumTitle("Greatest Hits (Deluxe Edition)"))
        assertEquals("beyonce", normalizeAlbumTitle("Beyoncé"))
        assertEquals("", normalizeAlbumTitle(null))
    }

    private fun summary(key: String, label: String) = AlbumSummary(
        albumKey = key,
        albumLabel = label,
        artistLabel = "x",
        trackCount = 1,
        year = null,
        coverThumbnailPath = null,
        artStatus = AlbumArtStatus.ALL_OK,
        lyricsStatus = AlbumLyricsStatus.NONE,
        tagStatus = AlbumTagStatus.ALL_OK,
        mixedArtist = false,
    )

    @Test
    fun suspectsAreOnlyTitlesSharedAcrossKeys() {
        val albums = listOf(
            summary("k1", "Greatest Hits"),
            summary("k2", "Greatest Hits "),
            summary("k3", "Unique Album"),
        )
        assertEquals(setOf("k1", "k2"), duplicateSuspectKeys(albums))
    }

    private fun track(key: String, album: String?, albumArtist: String?, artist: String?) =
        TrackEntity(
            documentUri = "u-$key-$album-$artist", treeUri = "t", displayName = "d",
            parentPath = "p", format = "flac", sizeBytes = 1, lastModified = 1,
            durationMs = null, title = "ti", artist = artist, album = album,
            albumArtist = albumArtist, trackNumber = 1, discNumber = 1, year = null,
            genre = null, composer = null, comment = null, compilation = false,
            albumKey = key, albumLabel = album.orEmpty(), hasEmbeddedArt = false,
            artWidth = null, artHeight = null, thumbnailPath = null, artScanPending = false,
            hasSidecarLrc = false, sidecarLrcSynced = false, coreTagsComplete = true,
            artistUnknown = false, scannedAt = 0,
        )

    @Test
    fun planExplainsMissingAlbumArtistCompilationSplit() {
        val tracks = listOf(
            track("a", "Mix 2020", null, "Artist A"),
            track("b", "Mix 2020", null, "Artist B"),
        )
        val plan = buildCombinePlan(tracks)
        assertEquals("Mix 2020", plan.canonicalAlbum)
        assertTrue(plan.reasons.any { it.contains("No album artist set") })
    }

    @Test
    fun planFlagsDifferingAlbumArtistAndPicksMostCommon() {
        val tracks = listOf(
            track("a", "Album", "Various Artists", "A"),
            track("b", "Album", "various artists", "B"),
            track("c", "Album", "Various Artists", "C"),
        )
        val plan = buildCombinePlan(tracks)
        assertEquals("Various Artists", plan.canonicalAlbumArtist)
        assertTrue(plan.reasons.any { it.contains("Album artist differs") })
    }
}
