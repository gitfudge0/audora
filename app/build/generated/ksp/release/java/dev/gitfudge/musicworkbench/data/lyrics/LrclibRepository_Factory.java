package dev.gitfudge.musicworkbench.data.lyrics;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class LrclibRepository_Factory implements Factory<LrclibRepository> {
  private final Provider<LrclibApi> apiProvider;

  public LrclibRepository_Factory(Provider<LrclibApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public LrclibRepository get() {
    return newInstance(apiProvider.get());
  }

  public static LrclibRepository_Factory create(Provider<LrclibApi> apiProvider) {
    return new LrclibRepository_Factory(apiProvider);
  }

  public static LrclibRepository newInstance(LrclibApi api) {
    return new LrclibRepository(api);
  }
}
