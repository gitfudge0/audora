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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
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
    private companion object {
        const val PROGRESS_EMIT_EVERY_FILES = 25
        const val PROGRESS_EMIT_MIN_INTERVAL_MS = 200L
        // Larger batches mean fewer write transactions, and each write
        // transaction invalidates every observing Room query (the album
        // GROUP-BY rollup, the filtered track list, the counts). On a large
        // first scan a small batch produced ~1 re-aggregation per 50 files;
        // 300 keeps the progressive list moving while cutting that ~6x.
        const val UPSERT_BATCH_SIZE = 300
        const val FILE_TASK_BATCH_SIZE = 64
        val SCAN_PARALLELISM = Runtime.getRuntime().availableProcessors().coerceIn(4, 8)
        val ART_ENRICH_PARALLELISM = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
    }

    private data class Child(
        val docId: String,
        val name: String,
        val mime: String,
        val size: Long,
        val lastModified: Long,
    )

    data class ArtEnrichmentResult(
        val processed: Int,
        val failed: Int,
    )

    suspend fun scan(
        treeUriString: String,
        onProgress: suspend (scanned: Int, label: String) -> Unit,
    ): ScanResult = withContext(Dispatchers.IO) {
        coroutineScope {
            val extractorDispatcher = Dispatchers.IO.limitedParallelism(SCAN_PARALLELISM)
            val treeUri = treeUriString.toUri()
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)

            val priorSignatures = trackDao.signatures(treeUriString).associateBy { it.documentUri }
            val seenUris = HashSet<String>()
            val pending = ArrayList<TrackEntity>(UPSERT_BATCH_SIZE)
            var scanned = 0
            var upserted = 0
            val failed = AtomicInteger(0)
            var lastProgressCount = 0
            var lastProgressAtMs = 0L

            val stack = ArrayDeque<Pair<String, String>>()
            stack.addLast(rootDocId to "")

            suspend fun emitProgressIfNeeded(label: String, force: Boolean = false) {
                val now = System.currentTimeMillis()
                val countDelta = scanned - lastProgressCount
                val timeDelta = now - lastProgressAtMs
                if (!force &&
                    countDelta < PROGRESS_EMIT_EVERY_FILES &&
                    timeDelta < PROGRESS_EMIT_MIN_INTERVAL_MS
                ) {
                    return
                }
                lastProgressCount = scanned
                lastProgressAtMs = now
                onProgress(scanned, label)
            }

            suspend fun flushPendingIfNeeded(force: Boolean = false) {
                if (!force && pending.size < UPSERT_BATCH_SIZE) return
                if (pending.isEmpty()) return
                trackDao.upsertAll(pending.toList())
                upserted += pending.size
                pending.clear()
            }

            while (stack.isNotEmpty()) {
                coroutineContext.ensureActive()
                val (dirDocId, dirPath) = stack.removeLast()
                val children = listChildren(treeUri, dirDocId)
                val lrcByBase = HashMap<String, String>()
                val audioChildren = ArrayList<Child>()

                for (child in children) {
                    when {
                        child.mime == DocumentsContract.Document.MIME_TYPE_DIR -> {
                            val childPath = if (dirPath.isEmpty()) child.name else "$dirPath/${child.name}"
                            stack.addLast(child.docId to childPath)
                        }
                        child.name.substringAfterLast('.', "").equals("lrc", ignoreCase = true) -> {
                            lrcByBase[child.name.substringBeforeLast('.').lowercase()] = child.docId
                        }
                        isAudio(child.name, child.mime) -> audioChildren.add(child)
                    }
                }

                for (batch in audioChildren.chunked(FILE_TASK_BATCH_SIZE)) {
                    val jobs = ArrayList<Deferred<TrackEntity?>>(batch.size)
                    for (child in batch) {
                        coroutineContext.ensureActive()
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, child.docId)
                        val docUriString = docUri.toString()
                        seenUris.add(docUriString)
                        scanned++

                        val prior = priorSignatures[docUriString]
                        if (prior != null &&
                            prior.lastModified == child.lastModified &&
                            prior.sizeBytes == child.size
                        ) {
                            emitProgressIfNeeded(child.name)
                            continue
                        }

                        val lrcDocId = lrcByBase[child.name.substringBeforeLast('.').lowercase()]
                        jobs += async(extractorDispatcher) {
                            runCatching {
                                readTrackCore(
                                    docUri = docUri,
                                    docUriString = docUriString,
                                    treeUri = treeUri,
                                    treeUriString = treeUriString,
                                    name = child.name,
                                    parentPath = dirPath,
                                    size = child.size,
                                    lastModified = child.lastModified,
                                    lrcDocId = lrcDocId,
                                )
                            }.getOrElse {
                                failed.incrementAndGet()
                                null
                            }
                        }
                        emitProgressIfNeeded(child.name)
                    }

                    jobs.awaitAll().forEach { entity ->
                        if (entity != null) {
                            pending += entity
                            flushPendingIfNeeded()
                        }
                    }
                }
            }

            flushPendingIfNeeded(force = true)

            val stale = priorSignatures.keys - seenUris
            if (stale.isNotEmpty()) {
                stale.chunked(400).forEach { trackDao.deleteByUris(it) }
            }

            emitProgressIfNeeded(label = "", force = true)
            ScanResult(
                scanned = scanned,
                upserted = upserted,
                removed = stale.size,
                failed = failed.get(),
            )
        }
    }

    suspend fun enrichPendingArtwork(treeUriString: String): ArtEnrichmentResult = withContext(Dispatchers.IO) {
        coroutineScope {
            val artDir = File(context.cacheDir, "art").apply { mkdirs() }
            val generatedAlbumThumbs = ConcurrentHashMap.newKeySet<String>()
            val pendingTracks = trackDao.getPendingArtScanTracks(treeUriString)
            val artDispatcher = Dispatchers.IO.limitedParallelism(ART_ENRICH_PARALLELISM)
            val failed = AtomicInteger(0)
            var processed = 0
            val pendingWrites = ArrayList<TrackEntity>(UPSERT_BATCH_SIZE)

            suspend fun flushWrites(force: Boolean = false) {
                if (pendingWrites.isEmpty()) return
                if (!force && pendingWrites.size < UPSERT_BATCH_SIZE) return
                trackDao.upsertAll(pendingWrites.toList())
                processed += pendingWrites.size
                pendingWrites.clear()
            }

            for (batch in pendingTracks.chunked(FILE_TASK_BATCH_SIZE)) {
                coroutineContext.ensureActive()
                val enriched = batch.map { track ->
                    async(artDispatcher) {
                        runCatching {
                            enrichTrackArt(track, artDir, generatedAlbumThumbs)
                        }.getOrElse {
                            failed.incrementAndGet()
                            track.copy(artScanPending = false, scannedAt = System.currentTimeMillis())
                        }
                    }
                }.awaitAll()

                pendingWrites.addAll(enriched)
                flushWrites()
            }
            flushWrites(force = true)

            ArtEnrichmentResult(processed = processed, failed = failed.get())
        }
    }

    private fun listChildren(treeUri: Uri, parentDocId: String): List<Child> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
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
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                out += Child(
                    docId = cursor.getString(0),
                    name = cursor.getString(1) ?: continue,
                    mime = cursor.getString(2) ?: "",
                    size = if (cursor.isNull(3)) 0L else cursor.getLong(3),
                    lastModified = if (cursor.isNull(4)) 0L else cursor.getLong(4),
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

    private fun readLrcState(treeUri: Uri, lrcDocId: String): Boolean = runCatching {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, lrcDocId)
        context.contentResolver.openInputStream(uri)?.use { input ->
            val buf = ByteArray(4096)
            val read = input.read(buf)
            if (read <= 0) return@use false
            SYNCED_LRC.containsMatchIn(String(buf, 0, read, Charsets.UTF_8))
        } ?: false
    }.getOrDefault(false)

    private fun readTrackCore(
        docUri: Uri,
        docUriString: String,
        treeUri: Uri,
        treeUriString: String,
        name: String,
        parentPath: String,
        size: Long,
        lastModified: Long,
        lrcDocId: String?,
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
            val composer = key(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            val compilation = key(MediaMetadataRetriever.METADATA_KEY_COMPILATION).let {
                it == "1" || it.equals("true", ignoreCase = true)
            }
            val groupArtist = (albumArtist ?: artist ?: "").lowercase()
            val groupAlbum = (album ?: parentPath.substringAfterLast('/')).lowercase()
            val albumKey = "$groupArtist\u0001$groupAlbum"
            val albumLabel = album ?: parentPath.substringAfterLast('/').ifEmpty { "Unknown album" }
            val hasSidecar = lrcDocId != null
            val sidecarSynced = lrcDocId?.let { readLrcState(treeUri, it) } == true
            val artistUnknown = artist == null ||
                artist.lowercase() in setOf("unknown", "unknown artist", "<unknown>")
            val coreComplete = !title.isNullOrBlank() &&
                !artist.isNullOrBlank() &&
                !album.isNullOrBlank()

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
                composer = composer,
                comment = null,
                compilation = compilation,
                albumKey = albumKey,
                albumLabel = albumLabel,
                hasEmbeddedArt = false,
                artWidth = null,
                artHeight = null,
                thumbnailPath = null,
                artScanPending = true,
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

    private fun enrichTrackArt(
        track: TrackEntity,
        artDir: File,
        generatedAlbumThumbs: MutableSet<String>,
    ): TrackEntity {
        val mmr = MediaMetadataRetriever()
        try {
            val docUri = track.documentUri.toUri()
            mmr.setDataSource(context, docUri)
            val picture = mmr.embeddedPicture
            if (picture == null || picture.isEmpty()) {
                return track.copy(
                    hasEmbeddedArt = false,
                    artWidth = null,
                    artHeight = null,
                    thumbnailPath = null,
                    artScanPending = false,
                    scannedAt = System.currentTimeMillis(),
                )
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(picture, 0, picture.size, bounds)
            val thumbPath = if (generatedAlbumThumbs.add(track.albumKey)) {
                writeThumbnail(picture, bounds, track.documentUri, artDir)
            } else {
                track.thumbnailPath
            }

            return track.copy(
                hasEmbeddedArt = true,
                artWidth = bounds.outWidth.takeIf { it > 0 },
                artHeight = bounds.outHeight.takeIf { it > 0 },
                thumbnailPath = thumbPath,
                artScanPending = false,
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
        artDir: File,
    ): String? = runCatching {
        val target = 192
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > target * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeByteArray(picture, 0, picture.size, opts)
            ?: return@runCatching null
        val file = File(artDir, sha1(docUriString) + ".jpg")
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
        val match = Regex("""\d{4}""").find(raw)
        return match?.value ?: raw.takeIf { it.isNotBlank() }
    }

    private fun sha1(value: String): String =
        MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
