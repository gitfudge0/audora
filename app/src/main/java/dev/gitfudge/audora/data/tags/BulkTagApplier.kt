package dev.gitfudge.audora.data.tags

import androidx.core.net.toUri
import dev.gitfudge.audora.data.db.TrackDao
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.domain.BulkTagEdits
import dev.gitfudge.audora.domain.BulkTagField
import dev.gitfudge.audora.domain.buildAlbumKey
import javax.inject.Inject
import javax.inject.Singleton

data class BulkApplyResult(val ok: Int, val failed: Int)

/**
 * Applies a set of field overwrites to many tracks. Mirrors the per-track
 * write loop the album tag editor used to inline (file write via [TagWriter]
 * with the [TrackEntity.lastModified] stale-file guard, then DB upsert with a
 * recomputed album key). One bad file fails only that track.
 */
@Singleton
class BulkTagApplier @Inject constructor(
    private val tagWriter: TagWriter,
    private val trackDao: TrackDao,
) {
    suspend fun apply(
        tracks: List<TrackEntity>,
        edits: BulkTagEdits,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): BulkApplyResult {
        if (edits.isEmpty() || tracks.isEmpty()) return BulkApplyResult(0, 0)
        var ok = 0
        var failed = 0
        val total = tracks.size

        tracks.forEachIndexed { index, track ->
            runCatching {
                fun resolved(field: BulkTagField, current: String?): String =
                    edits[field] ?: current ?: ""

                val newArtist = resolved(BulkTagField.ARTIST, track.artist)
                val newAlbum = resolved(BulkTagField.ALBUM, track.album)
                val newAlbumArtist = resolved(BulkTagField.ALBUM_ARTIST, track.albumArtist)
                val newYear = resolved(BulkTagField.YEAR, track.year)
                val newGenre = resolved(BulkTagField.GENRE, track.genre)
                val newComposer = resolved(BulkTagField.COMPOSER, track.composer)
                val newDisc = resolved(BulkTagField.DISC_NUMBER, track.discNumber?.toString())
                val newCompilation =
                    edits[BulkTagField.COMPILATION]?.let { it == "true" } ?: track.compilation

                val tagEdits = TagEdits(
                    title = track.title ?: "",
                    artist = newArtist,
                    album = newAlbum,
                    albumArtist = newAlbumArtist,
                    trackNumber = track.trackNumber?.toString() ?: "",
                    discNumber = newDisc,
                    year = newYear,
                    genre = newGenre,
                    composer = newComposer,
                    comment = track.comment ?: "",
                    compilation = newCompilation,
                )
                val sig = tagWriter.write(
                    track.documentUri.toUri(), track.displayName, tagEdits,
                    expected = FileSignature(track.lastModified, track.sizeBytes),
                )

                val resolvedArtist = newArtist.ifBlank { track.artist }
                val resolvedAlbum = newAlbum.ifBlank { track.album }
                val resolvedAlbumArtist = newAlbumArtist.ifBlank { track.albumArtist }
                val newKey = buildAlbumKey(
                    albumArtist = resolvedAlbumArtist,
                    artist = resolvedArtist,
                    album = resolvedAlbum,
                )
                trackDao.upsertAll(
                    listOf(
                        track.copy(
                            artist = resolvedArtist,
                            album = resolvedAlbum,
                            albumArtist = resolvedAlbumArtist,
                            year = newYear.ifBlank { track.year },
                            genre = newGenre.ifBlank { track.genre },
                            composer = newComposer.ifBlank { track.composer },
                            discNumber = newDisc.toIntOrNull() ?: track.discNumber,
                            compilation = newCompilation,
                            albumKey = newKey,
                            albumLabel = resolvedAlbum ?: track.albumLabel,
                            lastModified = sig.lastModified,
                            sizeBytes = sig.sizeOr(track.sizeBytes),
                            scannedAt = System.currentTimeMillis(),
                        ),
                    ),
                )
                ok++
            }.onFailure { failed++ }
            onProgress(index + 1, total)
        }
        return BulkApplyResult(ok, failed)
    }
}
