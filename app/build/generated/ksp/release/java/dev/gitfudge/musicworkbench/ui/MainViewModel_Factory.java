package dev.gitfudge.musicworkbench.ui;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepository> settingsProvider;

  public MainViewModel_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsProvider) {
    this.contextProvider = contextProvider;
    this.settingsProvider = settingsProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(contextProvider.get(), settingsProvider.get());
  }

  public static MainViewModel_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepository> settingsProvider) {
    return new MainViewModel_Factory(contextProvider, settingsProvider);
  }

  public static MainViewModel newInstance(Context context, SettingsRepository settings) {
    return new MainViewModel(context, settings);
  }
}
