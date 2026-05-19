package dev.gitfudge.audora.data.lyrics

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

@Serializable
data class LrclibResult(
    val id: Long = 0,
    val trackName: String = "",
    val artistName: String = "",
    val albumName: String? = null,
    val duration: Double = 0.0,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null,
)

interface LrclibApi {
    @Headers("Lrclib-Client: Audora/0.1 (github.com/gitfudge0/audora)")
    @GET("api/get")
    suspend fun get(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Long? = null,
    ): Response<LrclibResult>
}
