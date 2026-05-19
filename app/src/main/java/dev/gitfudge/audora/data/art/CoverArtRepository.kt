package dev.gitfudge.audora.data.art

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A single album art candidate returned from MusicBrainz/CAA or iTunes.
 *
 * [thumbnailUrl] is used for the picker UI (250px CAA crop or iTunes 100px).
 * [fullResUrl] is fetched when the user (or batch) confirms a selection.
 */
data class CoverArtCandidate(
    val mbid: String,
    val title: String,
    val artist: String,
    val score: Int,
    val thumbnailUrl: String,
    val fullResUrl: String,
    val source: String,
)

/**
 * Searches for album art candidates and downloads full-resolution images.
 *
 * Primary source: MusicBrainz + Cover Art Archive (no API key required).
 * Fallback: iTunes Search API (when MusicBrainz returns no results).
 */
@Singleton
class CoverArtRepository @Inject constructor(
    private val musicBrainzApi: MusicBrainzApi,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    /**
     * Returns up to 5 candidates for the given album/artist pair.
     * Tries MusicBrainz first; falls back to iTunes if MB returns nothing.
     */
    suspend fun searchCandidates(album: String, artist: String): List<CoverArtCandidate> {
        if (album.isBlank()) return emptyList()

        val mbCandidates = runCatching {
            val response = musicBrainzApi.searchReleases(buildMbQuery(album, artist), limit = 5)
            if (response.isSuccessful) {
                response.body()?.releases?.map { release ->
                    val mbid = release.id
                    val creditName = release.artistCredit.firstOrNull()?.name
                        ?: artist.ifBlank { "Unknown" }
                    CoverArtCandidate(
                        mbid = mbid,
                        title = release.title,
                        artist = creditName,
                        score = release.score,
                        thumbnailUrl = "https://coverartarchive.org/release/$mbid/front-250",
                        fullResUrl = "https://coverartarchive.org/release/$mbid/front",
                        source = "MusicBrainz",
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())

        if (mbCandidates.isNotEmpty()) return mbCandidates

        return runCatching { searchItunesCandidates(album, artist) }
            .getOrDefault(emptyList())
    }

    /**
     * Downloads the full-resolution image for [candidate].
     * For CAA candidates this follows a 307 redirect; OkHttp handles it automatically.
     */
    suspend fun downloadFullRes(candidate: CoverArtCandidate): ByteArray =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(candidate.fullResUrl).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Art download failed: HTTP ${response.code}")
                }
                response.body?.bytes() ?: throw IOException("Empty response body")
            }
        }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Builds a MusicBrainz Lucene query string, quoting both terms to reduce noise.
     */
    private fun buildMbQuery(album: String, artist: String): String {
        val escapedAlbum = album.replace("\"", "\\\"")
        return if (artist.isNotBlank()) {
            val escapedArtist = artist.replace("\"", "\\\"")
            "release:\"$escapedAlbum\" AND artist:\"$escapedArtist\""
        } else {
            "release:\"$escapedAlbum\""
        }
    }

    private suspend fun searchItunesCandidates(
        album: String,
        artist: String,
    ): List<CoverArtCandidate> = withContext(Dispatchers.IO) {
        val term = buildString {
            if (artist.isNotBlank()) {
                append(URLEncoder.encode(artist, "UTF-8"))
                append("+")
            }
            append(URLEncoder.encode(album, "UTF-8"))
        }
        val url = "https://itunes.apple.com/search?term=$term&entity=album&limit=5"
        val responseBody = okHttpClient.newCall(Request.Builder().url(url).build())
            .execute()
            .use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: return@withContext emptyList()
            }
        parseItunesResponse(responseBody)
    }

    private fun parseItunesResponse(body: String): List<CoverArtCandidate> = runCatching {
        val root = json.parseToJsonElement(body).jsonObject
        val results = root["results"]?.jsonArray ?: return@runCatching emptyList()
        results.mapNotNull { element ->
            val obj = element.jsonObject
            val artworkUrl = obj["artworkUrl100"]?.jsonPrimitive?.content
                ?: return@mapNotNull null
            val collectionId = obj["collectionId"]?.jsonPrimitive?.content ?: ""
            val collectionName = obj["collectionName"]?.jsonPrimitive?.content ?: ""
            val artistName = obj["artistName"]?.jsonPrimitive?.content ?: ""
            CoverArtCandidate(
                mbid = "itunes-$collectionId",
                title = collectionName,
                artist = artistName,
                score = 100,
                thumbnailUrl = artworkUrl,
                fullResUrl = artworkUrl.replace("100x100bb", "3000x3000bb"),
                source = "iTunes",
            )
        }
    }.getOrDefault(emptyList())
}
