package dev.gitfudge.musicworkbench.ui.detail;

import androidx.lifecycle.SavedStateHandle;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.db.TrackDao;
import dev.gitfudge.musicworkbench.data.lyrics.LrcWriter;
import dev.gitfudge.musicworkbench.data.lyrics.LrclibRepository;
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository;
import dev.gitfudge.musicworkbench.data.tags.TagWriter;
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
public final class TrackDetailViewModel_Factory implements Factory<TrackDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<TrackDao> trackDaoProvider;

  private final Provider<TagWriter> tagWriterProvider;

  private final Provider<LrclibRepository> lrclibRepoProvider;

  private final Provider<LrcWriter> lrcWriterProvider;

  private final Provider<SettingsRepository> settingsProvider;

  public TrackDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TrackDao> trackDaoProvider, Provider<TagWriter> tagWriterProvider,
      Provider<LrclibRepository> lrclibRepoProvider, Provider<LrcWriter> lrcWriterProvider,
      Provider<SettingsRepository> settingsProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.trackDaoProvider = trackDaoProvider;
    this.tagWriterProvider = tagWriterProvider;
    this.lrclibRepoProvider = lrclibRepoProvider;
    this.lrcWriterProvider = lrcWriterProvider;
    this.settingsProvider = settingsProvider;
  }

  @Override
  public TrackDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), trackDaoProvider.get(), tagWriterProvider.get(), lrclibRepoProvider.get(), lrcWriterProvider.get(), settingsProvider.get());
  }

  public static TrackDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider, Provider<TrackDao> trackDaoProvider,
      Provider<TagWriter> tagWriterProvider, Provider<LrclibRepository> lrclibRepoProvider,
      Provider<LrcWriter> lrcWriterProvider, Provider<SettingsRepository> settingsProvider) {
    return new TrackDetailViewModel_Factory(savedStateHandleProvider, trackDaoProvider, tagWriterProvider, lrclibRepoProvider, lrcWriterProvider, settingsProvider);
  }

  public static TrackDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      TrackDao trackDao, TagWriter tagWriter, LrclibRepository lrclibRepo, LrcWriter lrcWriter,
      SettingsRepository settings) {
    return new TrackDetailViewModel(savedStateHandle, trackDao, tagWriter, lrclibRepo, lrcWriter, settings);
  }
}
