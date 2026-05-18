package dev.gitfudge.musicworkbench.data.tags

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
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
     * Only enforced when both sides report a usable timestamp — some SAF
     * providers return 0, and a false positive here would block all writes.
     */
    fun assertUnchanged(docUri: Uri, displayName: String, expectedLastModified: Long) {
        if (expectedLastModified <= 0L) return
        val current = queryLastModified(docUri) ?: return
        if (current <= 0L) return
        // 2s tolerance: some providers round to the second.
        if (kotlin.math.abs(current - expectedLastModified) > 2_000L) {
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

    fun queryLastModified(docUri: Uri): Long? = runCatching {
        context.contentResolver.query(
            docUri,
            arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED),
            null, null, null,
        )?.use { if (it.moveToFirst()) it.getLong(0) else null }
    }.getOrNull()

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
