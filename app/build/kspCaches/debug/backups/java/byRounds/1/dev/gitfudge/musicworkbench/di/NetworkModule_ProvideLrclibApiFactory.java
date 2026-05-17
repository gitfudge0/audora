package dev.gitfudge.musicworkbench.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.lyrics.LrclibApi;
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
public final class NetworkModule_ProvideLrclibApiFactory implements Factory<LrclibApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideLrclibApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public LrclibApi get() {
    return provideLrclibApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideLrclibApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideLrclibApiFactory(retrofitProvider);
  }

  public static LrclibApi provideLrclibApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideLrclibApi(retrofit));
  }
}
