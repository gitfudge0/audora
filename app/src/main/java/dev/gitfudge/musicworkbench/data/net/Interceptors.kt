package dev.gitfudge.musicworkbench.data.net

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Sets a descriptive User-Agent. MusicBrainz *requires* a meaningful UA
 * (app name, version, contact) and will rate-limit / block generic clients.
 */
class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build(),
        )
}

/**
 * Enforces a minimum interval between requests on a client. MusicBrainz caps
 * anonymous use at ~1 request/second; exceeding it gets the app throttled.
 * Serialized on a lock so concurrent batch fetches still respect the cap.
 */
class RateLimitInterceptor(private val minIntervalMs: Long) : Interceptor {
    private val lock = Any()
    @Volatile private var lastRequestAt = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            val wait = minIntervalMs - (now - lastRequestAt)
            if (wait > 0) {
                try {
                    Thread.sleep(wait)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted while rate-limiting", e)
                }
            }
            lastRequestAt = System.currentTimeMillis()
        }
        return chain.proceed(chain.request())
    }
}

/**
 * Retries idempotent GETs on transient I/O failures and 5xx/429 with simple
 * linear backoff. Keeps batch fetches from collapsing on a flaky connection.
 */
class RetryInterceptor(
    private val maxAttempts: Int = 3,
    private val backoffMs: Long = 800,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastError: IOException? = null
        repeat(maxAttempts) { attempt ->
            try {
                val response = chain.proceed(request)
                if (response.code in listOf(429, 500, 502, 503, 504) && attempt < maxAttempts - 1) {
                    response.close()
                    Thread.sleep(backoffMs * (attempt + 1))
                    return@repeat
                }
                return response
            } catch (e: IOException) {
                lastError = e
                if (attempt < maxAttempts - 1) {
                    try {
                        Thread.sleep(backoffMs * (attempt + 1))
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Interrupted during retry backoff", ie)
                    }
                }
            }
        }
        throw lastError ?: IOException("Request failed after $maxAttempts attempts")
    }
}
