package dev.gitfudge.musicworkbench.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gitfudge.musicworkbench.BuildConfig
import dev.gitfudge.musicworkbench.data.art.MusicBrainzApi
import dev.gitfudge.musicworkbench.data.lyrics.LrclibApi
import dev.gitfudge.musicworkbench.data.net.RateLimitInterceptor
import dev.gitfudge.musicworkbench.data.net.RetryInterceptor
import dev.gitfudge.musicworkbench.data.net.UserAgentInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Identifies the app to upstream services. MusicBrainz requires this. */
    private val USER_AGENT =
        "MusicWorkbench/${BuildConfig.VERSION_NAME} ( https://gitfudge.dev )"

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    /** Shared base client: logging + UA + retry. */
    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(USER_AGENT))
        .addInterceptor(RetryInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
        )
        .build()

    @Provides
    @Singleton
    @Named("lrclib")
    fun provideRetrofit(json: Json, okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://lrclib.net/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideLrclibApi(@Named("lrclib") retrofit: Retrofit): LrclibApi =
        retrofit.create(LrclibApi::class.java)

    /**
     * Dedicated MusicBrainz client: same base behavior plus a hard ~1 req/sec
     * rate limit, per their access rules for anonymous clients.
     */
    @Provides
    @Singleton
    @Named("musicbrainz")
    fun provideMusicBrainzClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(USER_AGENT))
        .addInterceptor(RateLimitInterceptor(minIntervalMs = 1_100))
        .addInterceptor(RetryInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
        )
        .build()

    @Provides
    @Singleton
    @Named("musicbrainz")
    fun provideMusicBrainzRetrofit(
        json: Json,
        @Named("musicbrainz") okHttp: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://musicbrainz.org/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideMusicBrainzApi(@Named("musicbrainz") retrofit: Retrofit): MusicBrainzApi =
        retrofit.create(MusicBrainzApi::class.java)
}
