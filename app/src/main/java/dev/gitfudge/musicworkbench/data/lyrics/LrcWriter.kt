package dev.gitfudge.musicworkbench.data.lyrics

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcWriter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Writes lrcContent as a sidecar file next to the audio file in the same
     * SAF tree. Overwrites any existing .lrc with the same base name.
     */
    suspend fun write(
        treeUri: Uri,
        audioDocUri: Uri,
        audioDisplayName: String,
        lrcContent: String,
    ): Unit = withContext(Dispatchers.IO) {
        val baseName = audioDisplayName.substringBeforeLast('.')
        val lrcName = "$baseName.lrc"

        val audioDocId = DocumentsContract.getDocumentId(audioDocUri)
        val parentDocId = audioDocId.substringBeforeLast('/', audioDocId)

        val existingUri = findExistingLrc(treeUri, parentDocId, lrcName)

        val targetUri = existingUri ?: run {
            val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocId)
            DocumentsContract.createDocument(
                context.contentResolver, parentDocUri, "text/plain", lrcName,
            ) ?: throw IOException("Failed to create $lrcName")
        }

        context.contentResolver.openOutputStream(targetUri, "wt")!!.use { out ->
            out.writer(Charsets.UTF_8).use { it.write(lrcContent) }
        }
    }

    private fun findExistingLrc(treeUri: Uri, parentDocId: String, lrcName: String): Uri? =
        runCatching {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
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
                        return@use DocumentsContract.buildDocumentUriUsingTree(
                            treeUri, cursor.getString(0),
                        )
                    }
                }
                null
            }
        }.getOrNull()
}
