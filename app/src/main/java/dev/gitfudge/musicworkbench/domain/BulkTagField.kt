package dev.gitfudge.musicworkbench.domain

import dev.gitfudge.musicworkbench.data.db.TrackEntity

enum class BulkTagFieldKind { TEXT, NUMBER, BOOLEAN }

/**
 * A tag field that can be overwritten in bulk across a multi-selection.
 * [valueOf] reads the current value off a track so the editor can show the
 * distinct existing values among the selection.
 */
enum class BulkTagField(
    val label: String,
    val kind: BulkTagFieldKind,
) {
    ALBUM("Album", BulkTagFieldKind.TEXT),
    ALBUM_ARTIST("Album artist", BulkTagFieldKind.TEXT),
    ARTIST("Artist", BulkTagFieldKind.TEXT),
    YEAR("Year", BulkTagFieldKind.NUMBER),
    GENRE("Genre", BulkTagFieldKind.TEXT),
    COMPOSER("Composer", BulkTagFieldKind.TEXT),
    DISC_NUMBER("Disc number", BulkTagFieldKind.NUMBER),
    COMPILATION("Compilation", BulkTagFieldKind.BOOLEAN);

    fun valueOf(track: TrackEntity): String? = when (this) {
        ALBUM -> track.album
        ALBUM_ARTIST -> track.albumArtist
        ARTIST -> track.artist
        YEAR -> track.year
        GENRE -> track.genre
        COMPOSER -> track.composer
        DISC_NUMBER -> track.discNumber?.toString()
        COMPILATION -> if (track.compilation) "true" else "false"
    }

    companion object {
        /** Fields offered in the bulk editor, in display order. */
        val ALL: List<BulkTagField> = entries
    }
}

/** Present key = overwrite every selected track with this value. */
typealias BulkTagEdits = Map<BulkTagField, String>

/**
 * Distinct non-blank existing values for [field] across [tracks], capped so the
 * chip row stays reasonable.
 */
fun distinctValues(tracks: List<TrackEntity>, field: BulkTagField): List<String> =
    tracks.asSequence()
        .map { field.valueOf(it).orEmpty().trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .take(12)
        .toList()
