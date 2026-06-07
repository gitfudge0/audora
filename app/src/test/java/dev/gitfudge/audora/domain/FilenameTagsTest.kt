package dev.gitfudge.audora.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FilenameTagsTest {

    // ── stripExtension ─────────────────────────────────────────────────────────

    @Test
    fun stripsCommonAudioExtensions() {
        assertEquals("04 - Luna Rail - Night Bus", FilenameTags.stripExtension("04 - Luna Rail - Night Bus.flac"))
        assertEquals("song", FilenameTags.stripExtension("song.MP3"))
        assertEquals("song", FilenameTags.stripExtension("song.m4a"))
    }

    @Test
    fun keepsDotsThatAreNotExtensions() {
        assertEquals("Mr. Brightside", FilenameTags.stripExtension("Mr. Brightside"))
        assertEquals(".hidden", FilenameTags.stripExtension(".hidden"))
    }

    // ── tokenize ───────────────────────────────────────────────────────────────

    @Test
    fun tokenizesOnDashSeparator() {
        assertEquals(listOf("04", "Luna Rail", "Night Bus"), FilenameTags.tokenize("04 - Luna Rail - Night Bus"))
    }

    @Test
    fun tokenizesOnUnderscores() {
        assertEquals(listOf("Nils Frahm", "All Melody"), FilenameTags.tokenize("Nils Frahm_All Melody"))
    }

    @Test
    fun fallsBackToWhitespaceWhenNoSeparator() {
        assertEquals(listOf("04", "Luna", "Rail", "Night", "Bus"), FilenameTags.tokenize("04 Luna Rail Night Bus"))
    }

    // ── detectPattern ──────────────────────────────────────────────────────────

    @Test
    fun detectsTrackArtistTitle() {
        assertEquals(NamingPattern.TRACK_ARTIST_TITLE, FilenameTags.detectPattern("04 - Luna Rail - Night Bus.flac"))
    }

    @Test
    fun detectsArtistTitle() {
        assertEquals(NamingPattern.ARTIST_TITLE, FilenameTags.detectPattern("Luna Rail - Night Bus.mp3"))
    }

    @Test
    fun detectsTrackTitle() {
        assertEquals(NamingPattern.TRACK_TITLE, FilenameTags.detectPattern("07 - Night Bus.flac"))
    }

    @Test
    fun detectsTitleOnlyWhenNoSeparators() {
        // No real separators → we should not invent an artist.
        assertEquals(NamingPattern.TITLE_ONLY, FilenameTags.detectPattern("Night Bus.flac"))
    }

    @Test
    fun whitespaceOnlyNameDoesNotImplyArtist() {
        assertEquals(NamingPattern.TITLE_ONLY, FilenameTags.detectPattern("04 Luna Rail Night Bus.flac"))
    }

    // ── parse: field maps ──────────────────────────────────────────────────────

    @Test
    fun parsesTrackArtistTitleAndNormalizesTrackNumber() {
        val r = FilenameTags.parse("04 - Luna Rail - Night Bus.flac", NamingPattern.TRACK_ARTIST_TITLE)
        assertEquals("4", r.fields["trackNumber"])
        assertEquals("Luna Rail", r.fields["artist"])
        assertEquals("Night Bus", r.fields["title"])
        // Extension token is present but ignored.
        assertTrue(r.mappings.any { it.token == ".flac" && it.fieldLabel == null })
    }

    @Test
    fun parsesArtistTitle() {
        val r = FilenameTags.parse("Nils Frahm - Says.mp3", NamingPattern.ARTIST_TITLE)
        assertEquals(mapOf("artist" to "Nils Frahm", "title" to "Says"), r.fields)
    }

    @Test
    fun parsesTrackTitle() {
        val r = FilenameTags.parse("07 - Night Bus.flac", NamingPattern.TRACK_TITLE)
        assertEquals("7", r.fields["trackNumber"])
        assertEquals("Night Bus", r.fields["title"])
        assertNull(r.fields["artist"])
    }

    @Test
    fun parsesTitleOnly() {
        val r = FilenameTags.parse("Night Bus.flac", NamingPattern.TITLE_ONLY)
        assertEquals(mapOf("title" to "Night Bus"), r.fields)
    }

    @Test
    fun extraTokensFoldIntoTitle() {
        // Title contains the separator-equivalent; remaining tokens join back.
        val r = FilenameTags.parse("02 - Aphex Twin - Avril 14th - Reprise.flac", NamingPattern.TRACK_ARTIST_TITLE)
        assertEquals("2", r.fields["trackNumber"])
        assertEquals("Aphex Twin", r.fields["artist"])
        assertEquals("Avril 14th Reprise", r.fields["title"])
    }

    @Test
    fun messyUnderscoreNameWithDefaultDetection() {
        val r = FilenameTags.parse("the_national_bloodbuzz_ohio.mp3", NamingPattern.ARTIST_TITLE)
        // Underscores tokenize the whole stem; first token is artist, rest title.
        assertEquals("the", r.fields["artist"])
        assertEquals("national bloodbuzz ohio", r.fields["title"])
    }

    @Test
    fun defaultParseUsesAutoDetectedPattern() {
        val r = FilenameTags.parse("04 - Luna Rail - Night Bus.flac")
        assertEquals(NamingPattern.TRACK_ARTIST_TITLE, r.pattern)
        assertEquals("Luna Rail", r.fields["artist"])
    }

    @Test
    fun blankTokenIsIgnoredNotAssigned() {
        // Leading non-numeric track token in TRACK_TITLE produces no trackNumber.
        val r = FilenameTags.parse("intro - Night Bus.flac", NamingPattern.TRACK_TITLE)
        assertNull(r.fields["trackNumber"])
        assertEquals("Night Bus", r.fields["title"])
    }
}
