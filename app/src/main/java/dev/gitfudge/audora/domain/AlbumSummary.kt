package dev.gitfudge.audora.domain

/**
 * Aggregated view of one album, derived from a `GROUP BY albumKey` query
 * against the tracks table. Used by the Albums tab and Album Detail screen.
 */
data class AlbumSummary(
    val albumKey: String,
    val albumLabel: String,
    /** Album artist if every track agrees, otherwise "Various Artists". */
    val artistLabel: String,
    val trackCount: Int,
    val year: String?,
    /** Path to a thumbnail file for the cover (any one track that has it). */
    val coverThumbnailPath: String?,

    /** Rollup statuses across the album. */
    val artStatus: AlbumArtStatus,
    val lyricsStatus: AlbumLyricsStatus,
    val tagStatus: AlbumTagStatus,

    /** True when album artist varies across tracks. */
    val mixedArtist: Boolean,
)

enum class AlbumArtStatus {
    /** Artwork is still being scanned for one or more tracks. */
    PENDING,
    /** All tracks have OK art. */
    ALL_OK,
    /** All tracks missing art. */
    ALL_MISSING,
    /** Mix of OK / missing / low-res across tracks. */
    PARTIAL,
    /** All tracks have art but at least one is low res. */
    LOW_RES,
}

enum class AlbumLyricsStatus {
    /** Every track has synced lyrics. */
    ALL_SYNCED,
    /** Every track has lyrics (plain or synced) but not all synced. */
    ALL_PRESENT,
    /** Some tracks have lyrics, some don't. */
    PARTIAL,
    /** No track has lyrics. */
    NONE,
}

enum class AlbumTagStatus {
    /** All core tags complete and artists known. */
    ALL_OK,
    /** Some tracks have incomplete tags. */
    PARTIAL,
    /** Every track has incomplete tags / unknown artist. */
    ALL_BAD,
}

enum class LibraryTab { ALBUMS, TRACKS }
