package dev.gitfudge.audora.data.tags

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thrown when the file on disk changed since it was last scanned. The library
 * is precious — writing stale tags would silently clobber an out-of-band edit,
 * so we refuse and ask the caller to rescan first.
 */
class StaleFileException(val displayName: String) :
    IOException("\"$displayName\" changed on disk since the last scan. Rescan before writing.")

/** SAF-reported state of a file: enough to detect an out-of-band edit. */
data class FileSignature(val lastModified: Long, val sizeBytes: Long) {
    /** Returns sizeBytes if known (> 0), else the supplied fallback. */
    fun sizeOr(fallback: Long): Long = if (sizeBytes > 0L) sizeBytes else fallback

    companion object {
        val NONE = FileSignature(0L, 0L)
    }
}

/**
 * Pre-write safety net. Every destructive write goes through here:
 *  1. [assertUnchanged] — refuse if the file changed out-of-band.
 *  2. [backup] — copy the original bytes aside so a write can be undone.
 *  3. [restore] — put the original bytes back (the 30s undo).
 *
 * Backups live in cacheDir/backups and are pruned aggressively; this is an
 * undo buffer, not durable storage.
 */
@Singleton
class WriteSafetyManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir = File(context.cacheDir, "backups").apply { mkdirs() }

    /** Last backup file per documentUri, for the undo affordance. */
    private val lastBackup = ConcurrentHashMap<String, File>()

    /**
     * Refuse the write if the on-disk file diverged from what we scanned.
     * Size is the authoritative change signal — mtime can jitter through
     * SAF/FUSE caching and FAT's 2s resolution, so we only throw when size
     * differs *and* mtime drifts past tolerance.
     */
    fun assertUnchanged(docUri: Uri, displayName: String, expected: FileSignature) {
        val current = querySignature(docUri) ?: return

        val sizeUsable = expected.sizeBytes > 0L && current.sizeBytes > 0L
        if (sizeUsable && current.sizeBytes == expected.sizeBytes) return

        val mtimeUsable = expected.lastModified > 0L && current.lastModified > 0L
        if (!mtimeUsable) {
            if (sizeUsable) throw StaleFileException(displayName)
            return
        }
        // 2s tolerance: some providers/filesystems round to the second.
        if (kotlin.math.abs(current.lastModified - expected.lastModified) > 2_000L) {
            throw StaleFileException(displayName)
        }
    }

    /** Copy current bytes aside. Returns the backup file (or null on failure). */
    fun backup(docUri: Uri, displayName: String): File? = runCatching {
        prune()
        val ext = displayName.substringAfterLast('.', "bin")
        val file = File(dir, "${sha1(docUri.toString())}-${System.currentTimeMillis()}.$ext.bak")
        context.contentResolver.openInputStream(docUri)!!.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        lastBackup[docUri.toString()] = file
        file
    }.getOrNull()

    /** Most recent backup for this file, if one is still on disk. */
    fun latestBackup(docUri: Uri): File? =
        lastBackup[docUri.toString()]?.takeIf { it.exists() }

    /** Restore [backup] over the file. Returns true on success. */
    fun restore(docUri: Uri, backup: File): Boolean = runCatching {
        if (!backup.exists()) return false
        context.contentResolver.openOutputStream(docUri, "wt")!!.use { out ->
            backup.inputStream().use { it.copyTo(out) }
        }
        true
    }.getOrDefault(false)

    /** Single-doc cursor read of mtime + size. null if the file no longer exists. */
    fun querySignature(docUri: Uri): FileSignature? = runCatching {
        context.contentResolver.query(
            docUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_SIZE,
            ),
            null, null, null,
        )?.use { c ->
            if (!c.moveToFirst()) null
            else FileSignature(
                lastModified = if (c.isNull(0)) 0L else c.getLong(0),
                sizeBytes = if (c.isNull(1)) 0L else c.getLong(1),
            )
        }
    }.getOrNull()

    /** Convenience for callers that only need mtime. */
    fun queryLastModified(docUri: Uri): Long? = querySignature(docUri)?.lastModified

    /**
     * Post-write signature read. SAF providers can keep returning the pre-write
     * mtime for ~hundreds of ms after [openOutputStream] closes (FUSE/
     * MediaProvider flush lag). Recording that value would poison DB.lastModified
     * and guarantee the *next* [assertUnchanged] fails. Poll briefly until mtime
     * advances past [preWriteMtime]; fall back to the system clock as a last
     * resort so we never persist a known-stale timestamp.
     */
    suspend fun stableSignatureAfterWrite(docUri: Uri, preWriteMtime: Long): FileSignature {
        fun FileSignature?.hasAdvanced() =
            this != null && lastModified > 0L && lastModified != preWriteMtime

        var last = querySignature(docUri)
        repeat(5) {
            if (last.hasAdvanced()) return last!!
            delay(100)
            last = querySignature(docUri)
        }
        return if (last.hasAdvanced()) last!!
        else FileSignature(System.currentTimeMillis(), last?.sizeBytes ?: 0L)
    }

    /** Keep backups small: drop anything older than 10 minutes. */
    private fun prune() {
        val cutoff = System.currentTimeMillis() - 10 * 60 * 1000L
        dir.listFiles()?.forEach { f ->
            if (f.lastModified() < cutoff) {
                f.delete()
                lastBackup.entries.removeIf { it.value == f }
            }
        }
    }

    private fun sha1(value: String): String =
        MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
