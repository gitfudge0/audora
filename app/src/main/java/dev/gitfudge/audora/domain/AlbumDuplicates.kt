package dev.gitfudge.audora.domain

import dev.gitfudge.audora.data.db.TrackEntity
import java.text.Normalizer

/**
 * Helpers for the "duplicate albums" feature. An album shows up more than once
 * in the list whenever its tracks disagree on the fields that make up
 * [buildAlbumKey] — album text or album-artist/artist. These functions find the
 * likely-same albums and explain *why* they were split so the user can combine
 * them by overwriting the offending tags.
 */

/**
 * Collapses cosmetic differences in an album title so "Greatest Hits ",
 * "greatest hits" and "Greatest Hits (Deluxe Edition)" all fold together.
 * Drops bracketed segments, strips diacritics and punctuation, lowercases and
 * collapses whitespace.
 */
fun normalizeAlbumTitle(label: String?): String {
    if (label.isNullOrBlank()) return ""
    val noBrackets = label.replace(Regex("[\\(\\[\\{].*?[\\)\\]\\}]"), " ")
    val decomposed = Normalizer.normalize(noBrackets, Normalizer.Form.NFKD)
        .replace(Regex("\\p{Mn}+"), "")
    return decomposed
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
}

/**
 * The album keys that share a normalized title with at least one other album —
 * i.e. the rows worth showing under the Duplicates filter.
 */
fun duplicateSuspectKeys(albums: List<AlbumSummary>): Set<String> =
    albums
        .groupBy { normalizeAlbumTitle(it.albumLabel) }
        .filterKeys { it.isNotEmpty() }
        .values
        .filter { it.size > 1 }
        .flatMap { group -> group.map { it.albumKey } }
        .toSet()

/** One constituent album within a combine operation. */
data class CombineGroup(
    val albumKey: String,
    val albumText: String,
    val albumArtist: String?,
    val artistSample: String?,
    val trackCount: Int,
)

/**
 * A proposed merge of [groups] into one album, with the human-readable
 * [reasons] the rows were separate and a best-guess canonical Album /
 * Album artist to pre-fill the editor with.
 */
data class CombinePlan(
    val groups: List<CombineGroup>,
    val reasons: List<String>,
    val canonicalAlbum: String,
    val canonicalAlbumArtist: String,
)

private fun <T> Iterable<T>.mostCommon(): T? =
    groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

private fun quote(s: String?): String = if (s.isNullOrEmpty()) "(none)" else "“$s”"

/**
 * Builds the [CombinePlan] for the union of [tracks] (typically every track in
 * the albums the user multi-selected).
 */
fun buildCombinePlan(tracks: List<TrackEntity>): CombinePlan {
    val byKey = tracks.groupBy { it.albumKey }
    val groups = byKey.map { (key, ts) ->
        CombineGroup(
            albumKey = key,
            albumText = ts.mapNotNull { it.album?.takeIf(String::isNotBlank) }.mostCommon()
                ?: ts.firstOrNull()?.albumLabel.orEmpty(),
            albumArtist = ts.mapNotNull { it.albumArtist?.takeIf(String::isNotBlank) }.mostCommon(),
            artistSample = ts.mapNotNull { it.artist?.takeIf(String::isNotBlank) }.distinct()
                .let { artists ->
                    when {
                        artists.isEmpty() -> null
                        artists.size == 1 -> artists.first()
                        else -> "${artists.take(2).joinToString(", ")}…"
                    }
                },
            trackCount = ts.size,
        )
    }.sortedByDescending { it.trackCount }

    val reasons = buildList {
        val albumTexts = tracks.map { it.album?.trim().orEmpty() }.distinct()
        if (albumTexts.size > 1) {
            add("Album name differs: " + albumTexts.joinToString(" vs ") { quote(it) })
        }
        val albumArtists = tracks.map { it.albumArtist?.trim().orEmpty() }.distinct()
        val artists = tracks.map { it.artist?.trim().orEmpty() }.distinct()
        if (albumArtists.size > 1) {
            add("Album artist differs: " + albumArtists.joinToString(" vs ") { quote(it) })
        } else if (albumArtists.singleOrNull().isNullOrEmpty() && artists.size > 1) {
            add(
                "No album artist set, so tracks were grouped by their own artist: " +
                    artists.filter { it.isNotEmpty() }.joinToString(", ") { quote(it) },
            )
        }
        if (isEmpty()) add("These albums differ only by an exact tag value.")
    }

    val canonicalAlbum = tracks.mapNotNull { it.album?.takeIf(String::isNotBlank) }
        .mostCommon().orEmpty()
    val canonicalAlbumArtist =
        tracks.mapNotNull { it.albumArtist?.takeIf(String::isNotBlank) }.mostCommon()
            ?: tracks.mapNotNull { it.artist?.takeIf(String::isNotBlank) }.mostCommon()
            ?: "Various Artists"

    return CombinePlan(groups, reasons, canonicalAlbum, canonicalAlbumArtist)
}
