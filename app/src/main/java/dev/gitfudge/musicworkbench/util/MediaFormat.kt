package dev.gitfudge.musicworkbench.util

/**
 * Pure formatting for technical metadata so it's unit-testable and rendered
 * identically across screens (mono, tabular).
 */
object MediaFormat {
    /** Bytes → human size, e.g. `38.4 MB` / `512 KB`. */
    fun size(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val mb = bytes / 1_048_576.0
        return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(bytes / 1024.0)
    }

    /** Milliseconds → `m:ss`. */
    fun duration(ms: Long): String {
        val total = (ms / 1000).coerceAtLeast(0)
        return "%d:%02d".format(total / 60, total % 60)
    }
}
