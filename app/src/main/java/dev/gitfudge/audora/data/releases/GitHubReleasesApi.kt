package dev.gitfudge.audora.data.releases

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

@Serializable
data class GitHubReleaseAssetDto(
    val name: String = "",
    @SerialName("browser_download_url")
    val browserDownloadUrl: String = "",
    val size: Long? = null,
    @SerialName("content_type")
    val contentType: String? = null,
)

@Serializable
data class GitHubReleaseDto(
    val name: String? = null,
    @SerialName("tag_name")
    val tagName: String = "",
    val body: String? = null,
    @SerialName("html_url")
    val htmlUrl: String = "",
    @SerialName("published_at")
    val publishedAt: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubReleaseAssetDto> = emptyList(),
)

interface GitHubReleasesApi {
    @GET("repos/gitfudge0/audora/releases")
    suspend fun releases(): Response<List<GitHubReleaseDto>>
}
