package dev.gitfudge.audora.data.lyrics

import androidx.core.net.toUri
import dev.gitfudge.audora.data.db.TrackDao
import dev.gitfudge.audora.data.db.TrackEntity
import javax.inject.Inject
import javax.inject.Singleton

enum class LyricsSyncStatus { Saved, NoMatch, Failed }

@Singleton
class LyricsSyncRepository @Inject constructor(
    private val lrclibRepo: LrclibRepository,
    private val lrcWriter: LrcWriter,
    private val trackDao: TrackDao,
) {
    suspend fun fetchAndSave(track: TrackEntity): LyricsSyncStatus {
        val fetched = runCatching {
            lrclibRepo.fetch(
                title = track.title ?: track.displayName.substringBeforeLast('.'),
                artist = track.artist ?: "",
                album = track.album,
                durationMs = track.durationMs,
            )
        }.getOrElse {
            markAttempted(track)
            return LyricsSyncStatus.Failed
        }

        if (fetched == null || fetched.instrumental ||
            (fetched.syncedLyrics.isNullOrBlank() && fetched.plainLyrics.isNullOrBlank())
        ) {
            markAttempted(track)
            return LyricsSyncStatus.NoMatch
        }

        return runCatching {
            val lyricsText = fetched.syncedLyrics ?: fetched.plainLyrics!!
            val isSynced = !fetched.syncedLyrics.isNullOrBlank()
            lrcWriter.write(
                treeUri = track.treeUri.toUri(),
                audioDocUri = track.documentUri.toUri(),
                audioDisplayName = track.displayName,
                lrcContent = lyricsText,
            )
            trackDao.upsertAll(
                listOf(
                    track.copy(
                        hasSidecarLrc = true,
                        sidecarLrcSynced = isSynced,
                        lyricsFetchAttempted = true,
                        scannedAt = System.currentTimeMillis(),
                    ),
                ),
            )
            LyricsSyncStatus.Saved
        }.getOrElse {
            markAttempted(track)
            LyricsSyncStatus.Failed
        }
    }

    private suspend fun markAttempted(track: TrackEntity) {
        trackDao.upsertAll(
            listOf(
                track.copy(
                    lyricsFetchAttempted = true,
                    scannedAt = System.currentTimeMillis(),
                ),
            ),
        )
    }
}
