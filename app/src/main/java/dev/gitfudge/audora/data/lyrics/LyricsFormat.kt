package dev.gitfudge.audora.data.lyrics

/**
 * Pure, Android-free lyrics-format helpers so the synced/plain decision is
 * unit-testable and identical everywhere it's evaluated.
 */
object LyricsFormat {
    private val TIMESTAMP_LINE = Regex("""(?m)^\s*\[\d{1,2}:\d{2}(\.\d{1,3})?]""")

    /** True if any line is prefixed with an `[mm:ss.xx]` LRC timestamp. */
    fun isSynced(text: String): Boolean = TIMESTAMP_LINE.containsMatchIn(text)
}
