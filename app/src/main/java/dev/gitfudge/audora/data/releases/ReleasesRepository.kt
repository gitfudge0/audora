package dev.gitfudge.audora.data.releases

import androidx.compose.runtime.Immutable
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class AppRelease(
    val version: String,
    val title: String,
    val notes: String,
    val url: String,
    val publishedAt: String?,
    val prerelease: Boolean,
    val apk: ReleaseApk?,
)

@Immutable
data class ReleaseApk(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long?,
)

@Singleton
class ReleasesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: GitHubReleasesApi,
    private val okHttp: OkHttpClient,
) {
    suspend fun releases(): Result<List<AppRelease>> = runCatching {
        val response = api.releases()
        check(response.isSuccessful) { "GitHub releases returned HTTP ${response.code()}" }
        response.body().orEmpty()
            .filterNot { it.draft }
            .map { dto ->
                AppRelease(
                    version = dto.tagName.removePrefix("v"),
                    title = dto.name?.takeIf { it.isNotBlank() } ?: dto.tagName,
                    notes = dto.body?.trim()?.takeIf { it.isNotBlank() }
                        ?: "No release notes published.",
                    url = dto.htmlUrl,
                    publishedAt = dto.publishedAt,
                    prerelease = dto.prerelease,
                    apk = dto.assets.firstOrNull { asset ->
                        asset.name.endsWith(".apk", ignoreCase = true) ||
                            asset.contentType == "application/vnd.android.package-archive"
                    }?.let { asset ->
                        ReleaseApk(
                            name = asset.name,
                            downloadUrl = asset.browserDownloadUrl,
                            sizeBytes = asset.size,
                        )
                    },
                )
            }
    }

    suspend fun downloadApk(release: AppRelease): Result<File> = runCatching {
        val apk = checkNotNull(release.apk) { "This release does not include an APK asset." }
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(apk.downloadUrl)
                .build()
            okHttp.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "APK download returned HTTP ${response.code}" }
                val body = checkNotNull(response.body) { "APK download returned an empty body." }
                val dir = File(context.cacheDir, "updates").apply { mkdirs() }
                val cleanName = apk.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
                val output = File(dir, cleanName.ifBlank { "audora-${release.version}.apk" })
                body.byteStream().use { input ->
                    output.outputStream().use { outputStream ->
                        input.copyTo(outputStream)
                    }
                }
                output
            }
        }
    }
}
