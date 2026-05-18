package dev.gitfudge.musicworkbench.data.tags

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.AndroidArtwork
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class TagEdits(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val trackNumber: String,
    val discNumber: String,
    val year: String,
    val genre: String,
    val composer: String = "",
    val comment: String = "",
    val compilation: Boolean = false,
)

data class ArtWriteResult(
    val lastModified: Long,
    val artWidth: Int?,
    val artHeight: Int?,
    val thumbnailPath: String?,
)

@Singleton
class TagWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safety: WriteSafetyManager,
) {
    /** Edit text tags only. Returns new lastModified. */
    suspend fun write(
        docUri: Uri,
        displayName: String,
        edits: TagEdits,
        expectedLastModified: Long = 0L,
    ): Long =
        withContext(Dispatchers.IO) {
            editInPlace(docUri, displayName, expectedLastModified) { tag ->
                fun set(key: FieldKey, value: String) {
                    if (value.isBlank()) tag.deleteField(key) else tag.setField(key, value.trim())
                }
                set(FieldKey.TITLE, edits.title)
                set(FieldKey.ARTIST, edits.artist)
                set(FieldKey.ALBUM, edits.album)
                set(FieldKey.ALBUM_ARTIST, edits.albumArtist)
                set(FieldKey.TRACK, edits.trackNumber)
                set(FieldKey.DISC_NO, edits.discNumber)
                set(FieldKey.YEAR, edits.year)
                set(FieldKey.GENRE, edits.genre)
                set(FieldKey.COMPOSER, edits.composer)
                set(FieldKey.COMMENT, edits.comment)
                if (edits.compilation) {
                    tag.setField(FieldKey.IS_COMPILATION, "1")
                } else {
                    tag.deleteField(FieldKey.IS_COMPILATION)
                }
            }
        }

    /** Embed plain or LRC lyrics into the LYRICS tag. Returns new lastModified. */
    suspend fun writeLyrics(
        docUri: Uri,
        displayName: String,
        lyricsText: String,
        expectedLastModified: Long = 0L,
    ): Long =
        withContext(Dispatchers.IO) {
            editInPlace(docUri, displayName, expectedLastModified) { tag ->
                if (lyricsText.isBlank()) tag.deleteField(FieldKey.LYRICS)
                else tag.setField(FieldKey.LYRICS, lyricsText)
            }
        }

    /** Replace embedded album art. Returns dimensions + new lastModified + thumbnail path. */
    suspend fun writeArt(
        docUri: Uri,
        displayName: String,
        imageUri: Uri,
        expectedLastModified: Long = 0L,
    ): ArtWriteResult =
        withContext(Dispatchers.IO) {
            val artBytes = scaleToJpeg(imageUri, maxPx = 1000)

            editInPlace(docUri, displayName, expectedLastModified) { tag ->
                val artwork = AndroidArtwork().apply {
                    binaryData = artBytes
                    mimeType = "image/jpeg"
                    pictureType = 3 // PictureTypes.COVER_FRONT
                }
                tag.deleteArtworkField()
                tag.setField(artwork)
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, bounds)
            val artDir = File(context.cacheDir, "art").apply { mkdirs() }

            ArtWriteResult(
                lastModified = queryLastModified(docUri),
                artWidth = bounds.outWidth.takeIf { it > 0 },
                artHeight = bounds.outHeight.takeIf { it > 0 },
                thumbnailPath = writeThumbnail(artBytes, docUri.toString(), artDir),
            )
        }

    /**
     * Replace embedded album art from raw bytes (e.g. downloaded from network).
     * Avoids FileProvider / ContentResolver complexity for in-memory content.
     * Returns dimensions + new lastModified + thumbnail path.
     */
    suspend fun writeArtFromBytes(
        docUri: Uri,
        displayName: String,
        imageBytes: ByteArray,
        expectedLastModified: Long = 0L,
    ): ArtWriteResult = withContext(Dispatchers.IO) {
        val artBytes = scaleToJpegFromBytes(imageBytes, maxPx = 1000)

        editInPlace(docUri, displayName, expectedLastModified) { tag ->
            val artwork = AndroidArtwork().apply {
                binaryData = artBytes
                mimeType = "image/jpeg"
                pictureType = 3 // PictureTypes.COVER_FRONT
            }
            tag.deleteArtworkField()
            tag.setField(artwork)
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, bounds)
        val artDir = File(context.cacheDir, "art").apply { mkdirs() }

        ArtWriteResult(
            lastModified = queryLastModified(docUri),
            artWidth = bounds.outWidth.takeIf { it > 0 },
            artHeight = bounds.outHeight.takeIf { it > 0 },
            thumbnailPath = writeThumbnail(artBytes, docUri.toString(), artDir),
        )
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Restore the most recent pre-write backup of [docUri] (the undo).
     * Returns the new lastModified, or null if there was nothing to restore.
     */
    suspend fun restoreLastWrite(docUri: Uri): Long? = withContext(Dispatchers.IO) {
        val backup = safety.latestBackup(docUri) ?: return@withContext null
        if (safety.restore(docUri, backup)) queryLastModified(docUri) else null
    }

    private fun editInPlace(
        docUri: Uri,
        displayName: String,
        expectedLastModified: Long,
        block: (org.jaudiotagger.tag.Tag) -> Unit,
    ): Long {
        // 1. Refuse if the file changed out-of-band since we scanned it.
        safety.assertUnchanged(docUri, displayName, expectedLastModified)

        val ext = displayName.substringAfterLast('.', "mp3").lowercase()
        val tmp = File(context.cacheDir, "tagwrite_${System.currentTimeMillis()}.$ext")
        try {
            context.contentResolver.openInputStream(docUri)!!.use { input ->
                tmp.outputStream().use { input.copyTo(it) }
            }
            val audioFile = AudioFileIO.read(tmp)
            block(audioFile.tagOrCreateAndSetDefault)
            AudioFileIO.write(audioFile)
            // 2. Snapshot the original before the destructive truncate-write.
            safety.backup(docUri, displayName)
            context.contentResolver.openOutputStream(docUri, "wt")!!.use { out ->
                tmp.inputStream().use { it.copyTo(out) }
            }
            return queryLastModified(docUri)
        } finally {
            tmp.delete()
        }
    }

    private fun scaleToJpeg(imageUri: Uri, maxPx: Int): ByteArray {
        val original = BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(imageUri)!!,
        ) ?: throw IllegalArgumentException("Cannot decode image")
        val longest = maxOf(original.width, original.height)
        val bmp = if (longest > maxPx) {
            val scale = maxPx.toFloat() / longest
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true,
            ).also { if (it !== original) original.recycle() }
        } else {
            original
        }
        return ByteArrayOutputStream().use { bos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos)
            bmp.recycle()
            bos.toByteArray()
        }
    }

    private fun scaleToJpegFromBytes(imageBytes: ByteArray, maxPx: Int): ByteArray {
        val original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw IllegalArgumentException("Cannot decode image bytes")
        val longest = maxOf(original.width, original.height)
        val bmp = if (longest > maxPx) {
            val scale = maxPx.toFloat() / longest
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true,
            ).also { if (it !== original) original.recycle() }
        } else {
            original
        }
        return ByteArrayOutputStream().use { bos ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos)
            bmp.recycle()
            bos.toByteArray()
        }
    }

    private fun writeThumbnail(artBytes: ByteArray, docUriString: String, artDir: File): String? =
        runCatching {
            val target = 192
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > target * 2) sample *= 2
            val bmp = BitmapFactory.decodeByteArray(
                artBytes, 0, artBytes.size,
                BitmapFactory.Options().apply { inSampleSize = sample },
            ) ?: return@runCatching null
            val file = File(artDir, sha1(docUriString) + ".jpg")
            ByteArrayOutputStream().use { bos ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 82, bos)
                file.writeBytes(bos.toByteArray())
            }
            bmp.recycle()
            file.absolutePath
        }.getOrNull()

    private fun queryLastModified(docUri: Uri): Long = runCatching {
        context.contentResolver.query(
            docUri,
            arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED),
            null, null, null,
        )?.use { if (it.moveToFirst()) it.getLong(0) else 0L } ?: 0L
    }.getOrDefault(System.currentTimeMillis())

    private fun sha1(value: String): String =
        MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
