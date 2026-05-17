package dev.gitfudge.musicworkbench.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.art.MusicBrainzApi;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideMusicBrainzApiFactory implements Factory<MusicBrainzApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideMusicBrainzApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MusicBrainzApi get() {
    return provideMusicBrainzApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideMusicBrainzApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideMusicBrainzApiFactory(retrofitProvider);
  }

  public static MusicBrainzApi provideMusicBrainzApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideMusicBrainzApi(retrofit));
  }
}
