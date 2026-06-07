package dev.gitfudge.audora.data.art

import javax.inject.Inject
import javax.inject.Singleton

/**
 * A single recording match from MusicBrainz, shaped for the "Suggest tags →
 * From the web" picker. It is a pure populator: selecting one fills the existing
 * tag form (dirtying the propose → review diff → write-to-confirm pipeline). The
 * thumbnail URL points at the linked release's Cover Art Archive front, so the
 * candidate rows can show art exactly like the cover-art picker.
 */
data class MetadataCandidate(
    val recordingMbid: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val score: Int,
    val thumbnailUrl: String?,
)

/**
 * Searches MusicBrainz for recordings matching a free-text query and maps each
 * hit to a [MetadataCandidate]. Reuses [MusicBrainzApi] (already integrated for
 * cover art); keeps the call respectful (single request, bounded limit) per the
 * `data/net/Interceptors` conventions.
 */
@Singleton
class MetadataLookupRepository @Inject constructor(
    private val musicBrainzApi: MusicBrainzApi,
) {
    /** Returns up to [limit] recording candidates for [query], best score first. */
    suspend fun searchRecordings(query: String, limit: Int = 10): List<MetadataCandidate> {
        if (query.isBlank()) return emptyList()
        val response = musicBrainzApi.searchRecordings(query.trim(), limit = limit)
        if (!response.isSuccessful) return emptyList()
        val recordings = response.body()?.recordings ?: return emptyList()
        return recordings.map { rec ->
            val release = rec.releases.firstOrNull()
            MetadataCandidate(
                recordingMbid = rec.id,
                title = rec.title,
                artist = rec.artistCredit.joinToString(" ") { it.name }.ifBlank { "Unknown artist" },
                album = release?.title.orEmpty(),
                year = release?.date?.take(4).orEmpty(),
                score = rec.score,
                thumbnailUrl = release?.id?.let { "https://coverartarchive.org/release/$it/front-250" },
            )
        }.sortedByDescending { it.score }
    }
}
