package dev.gitfudge.audora.data.lyrics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsFormatTest {

    @Test
    fun plainLyrics_isNotSynced() {
        val text = "line one\nline two\nline three"
        assertFalse(LyricsFormat.isSynced(text))
    }

    @Test
    fun lrcTimestamps_areSynced() {
        val text = "[00:01.00] x\n[00:04.50] y\n[01:10.25] z"
        assertTrue(LyricsFormat.isSynced(text))
    }

    @Test
    fun timestampWithoutFraction_isSynced() {
        assertTrue(LyricsFormat.isSynced("[02:03] x"))
    }

    @Test
    fun metadataOnlyBrackets_areNotSynced() {
        // [ar:...] / [ti:...] LRC metadata tags are not timed lines.
        val text = "[ar:Some Artist]\n[ti:Some Title]\nplain body line"
        assertFalse(LyricsFormat.isSynced(text))
    }

    @Test
    fun blankText_isNotSynced() {
        assertFalse(LyricsFormat.isSynced(""))
    }
}
