package dev.gitfudge.musicworkbench.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
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
public final class NetworkModule_ProvideRetrofitFactory implements Factory<Retrofit> {
  private final Provider<Json> jsonProvider;

  private final Provider<OkHttpClient> okHttpProvider;

  public NetworkModule_ProvideRetrofitFactory(Provider<Json> jsonProvider,
      Provider<OkHttpClient> okHttpProvider) {
    this.jsonProvider = jsonProvider;
    this.okHttpProvider = okHttpProvider;
  }

  @Override
  public Retrofit get() {
    return provideRetrofit(jsonProvider.get(), okHttpProvider.get());
  }

  public static NetworkModule_ProvideRetrofitFactory create(Provider<Json> jsonProvider,
      Provider<OkHttpClient> okHttpProvider) {
    return new NetworkModule_ProvideRetrofitFactory(jsonProvider, okHttpProvider);
  }

  public static Retrofit provideRetrofit(Json json, OkHttpClient okHttp) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideRetrofit(json, okHttp));
  }
}
