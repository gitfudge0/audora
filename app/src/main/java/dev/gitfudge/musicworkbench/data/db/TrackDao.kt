package dev.gitfudge.musicworkbench.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @RawQuery(observedEntities = [TrackEntity::class])
    fun observeFiltered(query: SupportSQLiteQuery): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE documentUri = :uri LIMIT 1")
    fun observeTrack(uri: String): Flow<TrackEntity?>

    @Query("SELECT COUNT(*) FROM tracks WHERE treeUri = :treeUri")
    fun observeCount(treeUri: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tracks: List<TrackEntity>)

    @Query("SELECT documentUri, lastModified, sizeBytes FROM tracks WHERE treeUri = :treeUri")
    suspend fun signatures(treeUri: String): List<TrackSignature>

    @Query("DELETE FROM tracks WHERE documentUri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)

    @Query("DELETE FROM tracks WHERE treeUri = :treeUri")
    suspend fun clearTree(treeUri: String)

    /**
     * Returns all tracks that have no embedded art, or whose art is below
     * [lowResThresholdPx] in both dimensions.
     */
    @Query(
        """SELECT * FROM tracks WHERE treeUri = :treeUri
           AND (hasEmbeddedArt = 0
                OR (artWidth IS NOT NULL AND artHeight IS NOT NULL
                    AND MAX(artWidth, artHeight) < :lowResThresholdPx))""",
    )
    suspend fun getTracksNeedingArt(treeUri: String, lowResThresholdPx: Int): List<TrackEntity>

    // ── Album aggregation ─────────────────────────────────────────────────────

    /**
     * One row per album (grouped by albumKey). Only includes tracks that have
     * a real album tag — tracks with blank album fall into the Unfiled bucket.
     */
    @Query(
        """SELECT
              albumKey                                            AS albumKey,
              MIN(albumLabel)                                     AS albumLabel,
              COUNT(*)                                            AS trackCount,
              COUNT(DISTINCT COALESCE(NULLIF(albumArtist, ''), NULLIF(artist, ''), '')) AS distinctArtistCount,
              MIN(COALESCE(NULLIF(albumArtist, ''), NULLIF(artist, ''), 'Unknown artist')) AS firstArtist,
              MIN(year)                                           AS year,
              MAX(thumbnailPath)                                  AS coverThumbnailPath,

              SUM(CASE WHEN hasEmbeddedArt = 1 THEN 1 ELSE 0 END) AS withArt,
              SUM(CASE WHEN hasEmbeddedArt = 1
                        AND artWidth IS NOT NULL AND artHeight IS NOT NULL
                        AND MAX(artWidth, artHeight) < :lowResThresholdPx
                    THEN 1 ELSE 0 END)                            AS lowResArt,

              SUM(CASE WHEN hasSidecarLrc = 1 THEN 1 ELSE 0 END)  AS withLyrics,
              SUM(CASE WHEN hasSidecarLrc = 1 AND sidecarLrcSynced = 1 THEN 1 ELSE 0 END) AS syncedLyrics,

              SUM(CASE WHEN coreTagsComplete = 1 AND artistUnknown = 0 THEN 1 ELSE 0 END) AS okTags
          FROM tracks
          WHERE treeUri = :treeUri
            AND album IS NOT NULL AND album <> ''
          GROUP BY albumKey
          ORDER BY MIN(albumLabel) COLLATE NOCASE ASC""",
    )
    fun observeAlbumRows(treeUri: String, lowResThresholdPx: Int): Flow<List<AlbumRow>>

    @Query(
        """SELECT * FROM tracks
           WHERE treeUri = :treeUri AND albumKey = :albumKey
           ORDER BY discNumber ASC, trackNumber ASC, displayName ASC""",
    )
    fun observeTracksInAlbum(treeUri: String, albumKey: String): Flow<List<TrackEntity>>

    @Query(
        """SELECT * FROM tracks
           WHERE treeUri = :treeUri AND (album IS NULL OR album = '')
           ORDER BY COALESCE(artist, '') COLLATE NOCASE ASC,
                    COALESCE(title, displayName) COLLATE NOCASE ASC""",
    )
    fun observeUnfiledTracks(treeUri: String): Flow<List<TrackEntity>>

    @Query(
        """SELECT COUNT(*) FROM tracks
           WHERE treeUri = :treeUri AND (album IS NULL OR album = '')""",
    )
    fun observeUnfiledCount(treeUri: String): Flow<Int>
}

/**
 * Raw aggregation row for one album. Mapped into the
 * domain-level [dev.gitfudge.musicworkbench.domain.AlbumSummary] in the view model.
 */
data class AlbumRow(
    val albumKey: String,
    val albumLabel: String,
    val trackCount: Int,
    val distinctArtistCount: Int,
    val firstArtist: String,
    val year: String?,
    val coverThumbnailPath: String?,
    val withArt: Int,
    val lowResArt: Int,
    val withLyrics: Int,
    val syncedLyrics: Int,
    val okTags: Int,
)

/** Lightweight projection used to skip re-reading unchanged files on rescan. */
data class TrackSignature(
    val documentUri: String,
    val lastModified: Long,
    val sizeBytes: Long,
)
