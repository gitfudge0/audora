package dev.gitfudge.musicworkbench.data.lyrics

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads an existing sidecar `.lrc` so the user can edit lyrics by hand
 * instead of only fetching from LRCLIB. Operates entirely on the user's own
 * files in the granted tree.
 */
@Singleton
class LrcReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Existing sidecar text for [audioDocUri], or null if there is none. */
    suspend fun read(
        treeUri: Uri,
        audioDocUri: Uri,
        audioDisplayName: String,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val baseName = audioDisplayName.substringBeforeLast('.')
            val lrcName = "$baseName.lrc"
            val audioDocId = DocumentsContract.getDocumentId(audioDocUri)
            val parentDocId = audioDocId.substringBeforeLast('/', audioDocId)
            val childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                ),
                null, null, null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(1) ?: continue
                    if (name.equals(lrcName, ignoreCase = true)) {
                        val uri = DocumentsContract.buildDocumentUriUsingTree(
                            treeUri, cursor.getString(0),
                        )
                        return@use context.contentResolver.openInputStream(uri)
                            ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    }
                }
                null
            }
        }.getOrNull()
    }

    /** A line beginning with an `[mm:ss.xx]` timestamp means synced lyrics. */
    fun isSynced(text: String): Boolean = LyricsFormat.isSynced(text)
}
