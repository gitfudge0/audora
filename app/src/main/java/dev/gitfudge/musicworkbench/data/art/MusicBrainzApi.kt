package dev.gitfudge.musicworkbench.data.art

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

@Serializable
data class MbReleaseSearchResult(
    val releases: List<MbRelease> = emptyList(),
)

@Serializable
data class MbRelease(
    val id: String = "",
    val title: String = "",
    val score: Int = 0,
    @SerialName("artist-credit")
    val artistCredit: List<MbArtistCredit> = emptyList(),
)

@Serializable
data class MbArtistCredit(
    val name: String = "",
)

interface MusicBrainzApi {
    @Headers("User-Agent: MusicWorkbench/0.1 ( https://github.com/gitfudge0/audio-metadata )")
    @GET("ws/2/release")
    suspend fun searchReleases(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 5,
    ): Response<MbReleaseSearchResult>
}
