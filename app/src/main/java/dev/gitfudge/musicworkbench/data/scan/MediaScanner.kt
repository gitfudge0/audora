package dev.gitfudge.musicworkbench.data.scan

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gitfudge.musicworkbench.data.db.TrackDao
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

data class ScanResult(
    val scanned: Int,
    val upserted: Int,
    val removed: Int,
    val failed: Int,
)

private val AUDIO_EXTENSIONS = setOf(
    "mp3", "flac", "m4a", "m4b", "aac", "ogg", "oga", "opus",
    "wav", "wv", "ape", "mpc", "aif", "aiff", "wma", "alac",
)

private val SYNCED_LRC = Regex("""\[\d{1,2}:\d{2}""")

class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
) {
    private data class Child(
        val docId: String,
        val name: String,
        val mime: String,
        val size: Long,
        val lastModified: Long,
    )

    suspend fun scan(
        treeUriString: String,
        lowResThresholdPx: Int,
        onProgress: suspend (scanned: Int, label: String) -> Unit,
    ): ScanResult = coroutineScope {
        val treeUri = treeUriString.toUri()
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)

        val priorSignatures = trackDao.signatures(treeUriString)
            .associateBy { it.documentUri }
        val seenUris = HashSet<String>()
        val pending = ArrayList<TrackEntity>(64)
        var scanned = 0
        var upserted = 0
        var failed = 0

        val artDir = java.io.File(context.cacheDir, "art").apply { mkdirs() }

        // Stack of (documentId, path-so-far) for iterative recursion.
        val stack = ArrayDeque<Pair<String, String>>()
        stack.addLast(rootDocId to "")

        while (stack.isNotEmpty()) {
            coroutineContext.ensureActive()
            val (dirDocId, dirPath) = stack.removeLast()
            val children = listChildren(treeUri, dirDocId)

            val lrcByBase = HashMap<String, String>()
            for (c in children) {
                if (c.mime != DocumentsContract.Document.MIME_TYPE_DIR &&
                    c.name.substringAfterLast('.', "").equals("lrc", ignoreCase = true)
                ) {
                    lrcByBase[c.name.substringBeforeLast('.').lowercase()] = c.docId
                }
            }

            for (c in children) {
                coroutineContext.ensureActive()
                if (c.mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val childPath = if (dirPath.isEmpty()) c.name else "$dirPath/${c.name}"
                    stack.addLast(c.docId to childPath)
                    continue
                }
                if (!isAudio(c.name, c.mime)) continue

                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, c.docId)
                val docUriString = docUri.toString()
                seenUris.add(docUriString)
                scanned++

                val prior = priorSignatures[docUriString]
                if (prior != null && prior.lastModified == c.lastModified &&
                    prior.sizeBytes == c.size
                ) {
                    if (scanned % 25 == 0) onProgress(scanned, c.name)
                    continue // unchanged: keep existing row
                }

                val lrcDocId = lrcByBase[c.name.substringBeforeLast('.').lowercase()]
                val sidecar = lrcDocId?.let { readLrcState(treeUri, it) }

                val entity = runCatching {
                    readTrack(
                        docUri = docUri,
                        docUriString = docUriString,
                        treeUriString = treeUriString,
                        name = c.name,
                        parentPath = dirPath,
                        size = c.size,
                        lastModified = c.lastModified,
                        hasSidecar = lrcDocId != null,
                        sidecarSynced = sidecar == true,
                        artDir = artDir,
                    )
                }.getOrElse {
                    failed++
                    null
                }

                if (entity != null) {
                    pending.add(entity)
                    if (pending.size >= 50) {
                        trackDao.upsertAll(pending.toList())
                        upserted += pending.size
                        pending.clear()
                    }
                }
                onProgress(scanned, c.name)
            }
        }

        if (pending.isNotEmpty()) {
            trackDao.upsertAll(pending.toList())
            upserted += pending.size
        }

        val stale = priorSignatures.keys - seenUris
        if (stale.isNotEmpty()) {
            stale.chunked(400).forEach { trackDao.deleteByUris(it) }
        }

        ScanResult(scanned, upserted, stale.size, failed)
    }

    private fun listChildren(treeUri: Uri, parentDocId: String): List<Child> {
        val childrenUri =
            DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val out = ArrayList<Child>()
        context.contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            ),
            null, null, null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                out.add(
                    Child(
                        docId = cursor.getString(0),
                        name = cursor.getString(1) ?: continue,
                        mime = cursor.getString(2) ?: "",
                        size = if (cursor.isNull(3)) 0L else cursor.getLong(3),
                        lastModified = if (cursor.isNull(4)) 0L else cursor.getLong(4),
                    ),
                )
            }
        }
        return out
    }

    private fun isAudio(name: String, mime: String): Boolean {
        if (mime.startsWith("audio/")) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in AUDIO_EXTENSIONS
    }

    private fun readLrcState(treeUri: Uri, lrcDocId: String): Boolean {
        return runCatching {
            val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, lrcDocId)
            context.contentResolver.openInputStream(uri)?.use { input ->
                val buf = ByteArray(4096)
                val read = input.read(buf)
                if (read <= 0) return@use false
                SYNCED_LRC.containsMatchIn(String(buf, 0, read, Charsets.UTF_8))
            } ?: false
        }.getOrDefault(false)
    }

    private fun readTrack(
        docUri: Uri,
        docUriString: String,
        treeUriString: String,
        name: String,
        parentPath: String,
        size: Long,
        lastModified: Long,
        hasSidecar: Boolean,
        sidecarSynced: Boolean,
        artDir: java.io.File,
    ): TrackEntity {
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(context, docUri)
            fun key(k: Int) = mmr.extractMetadata(k)?.trim()?.takeIf { it.isNotEmpty() }

            val title = key(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = key(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = key(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val albumArtist = key(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            val durationMs = key(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val trackNo = parseLeadingInt(key(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER))
            val discNo = parseLeadingInt(key(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER))
            val year = parseYear(
                key(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    ?: key(MediaMetadataRetriever.METADATA_KEY_DATE),
            )
            val genre = key(MediaMetadataRetriever.METADATA_KEY_GENRE)

            val picture: ByteArray? = mmr.embeddedPicture
            var artW: Int? = null
            var artH: Int? = null
            var thumbPath: String? = null
            if (picture != null && picture.isNotEmpty()) {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(picture, 0, picture.size, bounds)
                artW = bounds.outWidth.takeIf { it > 0 }
                artH = bounds.outHeight.takeIf { it > 0 }
                thumbPath = writeThumbnail(picture, bounds, docUriString, artDir)
            }

            val artistUnknown = artist == null ||
                artist.lowercase() in setOf("unknown", "unknown artist", "<unknown>")
            val coreComplete = !title.isNullOrBlank() &&
                !artist.isNullOrBlank() && !album.isNullOrBlank()

            val groupArtist = (albumArtist ?: artist ?: "").lowercase()
            val groupAlbum = (album ?: parentPath.substringAfterLast('/')).lowercase()
            val albumLabel = album
                ?: parentPath.substringAfterLast('/').ifEmpty { "Unknown album" }

            return TrackEntity(
                documentUri = docUriString,
                treeUri = treeUriString,
                displayName = name,
                parentPath = parentPath,
                format = name.substringAfterLast('.', "").uppercase().ifEmpty { "?" },
                sizeBytes = size,
                lastModified = lastModified,
                durationMs = durationMs,
                title = title,
                artist = artist,
                album = album,
                albumArtist = albumArtist,
                trackNumber = trackNo,
                discNumber = discNo,
                year = year,
                genre = genre,
                albumKey = "$groupArtist$groupAlbum",
                albumLabel = albumLabel,
                hasEmbeddedArt = picture != null && picture.isNotEmpty(),
                artWidth = artW,
                artHeight = artH,
                thumbnailPath = thumbPath,
                hasSidecarLrc = hasSidecar,
                sidecarLrcSynced = sidecarSynced,
                coreTagsComplete = coreComplete,
                artistUnknown = artistUnknown,
                scannedAt = System.currentTimeMillis(),
            )
        } finally {
            runCatching { mmr.release() }
        }
    }

    private fun writeThumbnail(
        picture: ByteArray,
        bounds: BitmapFactory.Options,
        docUriString: String,
        artDir: java.io.File,
    ): String? = runCatching {
        val target = 192
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > target * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeByteArray(picture, 0, picture.size, opts)
            ?: return@runCatching null
        val file = java.io.File(artDir, sha1(docUriString) + ".jpg")
        ByteArrayOutputStream().use { bos ->
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 82, bos)
            file.writeBytes(bos.toByteArray())
        }
        bmp.recycle()
        file.absolutePath
    }.getOrNull()

    private fun parseLeadingInt(raw: String?): Int? =
        raw?.trim()?.takeWhile { it.isDigit() }?.toIntOrNull()

    private fun parseYear(raw: String?): String? {
        if (raw == null) return null
        val m = Regex("""\d{4}""").find(raw)
        return m?.value ?: raw.takeIf { it.isNotBlank() }
    }

    private fun sha1(value: String): String =
        MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
