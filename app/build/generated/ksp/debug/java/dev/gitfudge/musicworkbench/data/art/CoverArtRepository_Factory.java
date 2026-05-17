package dev.gitfudge.musicworkbench.data.art;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CoverArtRepository_Factory implements Factory<CoverArtRepository> {
  private final Provider<MusicBrainzApi> musicBrainzApiProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<Json> jsonProvider;

  public CoverArtRepository_Factory(Provider<MusicBrainzApi> musicBrainzApiProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<Json> jsonProvider) {
    this.musicBrainzApiProvider = musicBrainzApiProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public CoverArtRepository get() {
    return newInstance(musicBrainzApiProvider.get(), okHttpClientProvider.get(), jsonProvider.get());
  }

  public static CoverArtRepository_Factory create(Provider<MusicBrainzApi> musicBrainzApiProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<Json> jsonProvider) {
    return new CoverArtRepository_Factory(musicBrainzApiProvider, okHttpClientProvider, jsonProvider);
  }

  public static CoverArtRepository newInstance(MusicBrainzApi musicBrainzApi,
      OkHttpClient okHttpClient, Json json) {
    return new CoverArtRepository(musicBrainzApi, okHttpClient, json);
  }
}
