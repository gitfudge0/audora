package dev.gitfudge.audora.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One scanned audio file. Status fields are derived at scan time so the
 * library list never has to lie about or recompute state on the fly.
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["documentUri"], unique = true),
        Index(value = ["albumKey"]),
        Index(value = ["treeUri"]),
        Index(value = ["treeUri", "albumKey"]),
        Index(value = ["treeUri", "artScanPending"]),
        Index(value = ["treeUri", "lastModified"]),
    ],
)
data class TrackEntity(
    @PrimaryKey val documentUri: String,
    val treeUri: String,
    val displayName: String,
    val parentPath: String,
    val format: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val durationMs: Long?,

    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: String?,
    val genre: String?,
    val composer: String?,
    val comment: String?,
    val compilation: Boolean,

    /** Grouping key: album artist + album, falls back to folder. */
    val albumKey: String,
    val albumLabel: String,

    val hasEmbeddedArt: Boolean,
    val artWidth: Int?,
    val artHeight: Int?,
    val thumbnailPath: String?,
    val artScanPending: Boolean,

    val hasSidecarLrc: Boolean,
    val sidecarLrcSynced: Boolean,
    val lyricsFetchAttempted: Boolean = false,

    val coreTagsComplete: Boolean,
    val artistUnknown: Boolean,

    val scannedAt: Long,
)
