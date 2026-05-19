package dev.gitfudge.audora.data.lyrics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrclibRepository @Inject constructor(
    private val api: LrclibApi,
) {
    /**
     * Tries a precise lookup (title + artist + album + duration) first, then
     * falls back to a looser lookup (title + artist only) on 404.
     */
    suspend fun fetch(
        title: String,
        artist: String,
        album: String?,
        durationMs: Long?,
    ): LrclibResult? {
        val durationSec = durationMs?.let { it / 1000 }

        runCatching {
            val r = api.get(title, artist, album, durationSec)
            if (r.isSuccessful) return r.body()
        }

        if (album != null || durationSec != null) {
            runCatching {
                val r = api.get(title, artist)
                if (r.isSuccessful) return r.body()
            }
        }

        return null
    }
}
