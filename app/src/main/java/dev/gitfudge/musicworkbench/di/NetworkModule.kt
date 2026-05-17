package dev.gitfudge.musicworkbench.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.gitfudge.musicworkbench.data.art.MusicBrainzApi
import dev.gitfudge.musicworkbench.data.lyrics.LrclibApi
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

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
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

    @Provides
    @Singleton
    @Named("musicbrainz")
    fun provideMusicBrainzRetrofit(json: Json, okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://musicbrainz.org/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideMusicBrainzApi(@Named("musicbrainz") retrofit: Retrofit): MusicBrainzApi =
        retrofit.create(MusicBrainzApi::class.java)
}
