package dev.gitfudge.musicworkbench.data.lyrics

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gitfudge.musicworkbench.data.tags.WriteSafetyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safety: WriteSafetyManager,
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
            // Use a generic mime instead of text/plain: SAF providers append an
            // extension derived from the mime (text/plain -> .txt), which would
            // produce "name.lrc.txt". octet-stream has no registered extension,
            // so the ".lrc" name is preserved as given.
            val created = DocumentsContract.createDocument(
                context.contentResolver, parentDocUri, "application/octet-stream", lrcName,
            ) ?: throw IOException("Failed to create $lrcName")
            // Defensive: if a provider still mangled the name, rename it back.
            if (displayNameOf(created) != lrcName) {
                runCatching {
                    DocumentsContract.renameDocument(context.contentResolver, created, lrcName)
                }.getOrNull() ?: created
            } else {
                created
            }
        }

        // Back up a pre-existing sidecar before overwriting — a hand-authored
        // .lrc must be recoverable if a fetch replaces it.
        if (existingUri != null) safety.backup(existingUri, lrcName)

        context.contentResolver.openOutputStream(targetUri, "wt")!!.use { out ->
            out.writer(Charsets.UTF_8).use { it.write(lrcContent) }
        }
    }

    private fun displayNameOf(docUri: Uri): String? = runCatching {
        context.contentResolver.query(
            docUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null,
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }.getOrNull()

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
