package dev.gitfudge.musicworkbench.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaFormatTest {

    @Test
    fun megabytes() {
        assertEquals("38.4 MB", MediaFormat.size(40_265_318))
    }

    @Test
    fun kilobytesUnderOneMb() {
        assertEquals("512 KB", MediaFormat.size(512 * 1024))
    }

    @Test
    fun zeroOrNegativeSize() {
        assertEquals("0 KB", MediaFormat.size(0))
        assertEquals("0 KB", MediaFormat.size(-10))
    }

    @Test
    fun durationPadsSeconds() {
        assertEquals("4:05", MediaFormat.duration(245_000))
        assertEquals("0:09", MediaFormat.duration(9_000))
    }

    @Test
    fun durationClampsNegative() {
        assertEquals("0:00", MediaFormat.duration(-5))
    }
}
