package dev.gitfudge.musicworkbench.ui.album;

import androidx.lifecycle.SavedStateHandle;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.db.TrackDao;
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class AlbumDetailViewModel_Factory implements Factory<AlbumDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateProvider;

  private final Provider<SettingsRepository> settingsProvider;

  private final Provider<TrackDao> trackDaoProvider;

  public AlbumDetailViewModel_Factory(Provider<SavedStateHandle> savedStateProvider,
      Provider<SettingsRepository> settingsProvider, Provider<TrackDao> trackDaoProvider) {
    this.savedStateProvider = savedStateProvider;
    this.settingsProvider = settingsProvider;
    this.trackDaoProvider = trackDaoProvider;
  }

  @Override
  public AlbumDetailViewModel get() {
    return newInstance(savedStateProvider.get(), settingsProvider.get(), trackDaoProvider.get());
  }

  public static AlbumDetailViewModel_Factory create(Provider<SavedStateHandle> savedStateProvider,
      Provider<SettingsRepository> settingsProvider, Provider<TrackDao> trackDaoProvider) {
    return new AlbumDetailViewModel_Factory(savedStateProvider, settingsProvider, trackDaoProvider);
  }

  public static AlbumDetailViewModel newInstance(SavedStateHandle savedState,
      SettingsRepository settings, TrackDao trackDao) {
    return new AlbumDetailViewModel(savedState, settings, trackDao);
  }
}
