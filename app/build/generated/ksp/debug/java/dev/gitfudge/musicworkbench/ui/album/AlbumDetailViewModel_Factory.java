package dev.gitfudge.musicworkbench.ui.album;

import androidx.lifecycle.SavedStateHandle;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.art.CoverArtRepository;
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
public final class AlbumDetailViewModel_Factory implements Factory<AlbumDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateProvider;

  private final Provider<SettingsRepository> settingsProvider;

  private final Provider<TrackDao> trackDaoProvider;

  private final Provider<TagWriter> tagWriterProvider;

  private final Provider<CoverArtRepository> coverArtRepositoryProvider;

  private final Provider<LrclibRepository> lrclibRepoProvider;

  private final Provider<LrcWriter> lrcWriterProvider;

  public AlbumDetailViewModel_Factory(Provider<SavedStateHandle> savedStateProvider,
      Provider<SettingsRepository> settingsProvider, Provider<TrackDao> trackDaoProvider,
      Provider<TagWriter> tagWriterProvider,
      Provider<CoverArtRepository> coverArtRepositoryProvider,
      Provider<LrclibRepository> lrclibRepoProvider, Provider<LrcWriter> lrcWriterProvider) {
    this.savedStateProvider = savedStateProvider;
    this.settingsProvider = settingsProvider;
    this.trackDaoProvider = trackDaoProvider;
    this.tagWriterProvider = tagWriterProvider;
    this.coverArtRepositoryProvider = coverArtRepositoryProvider;
    this.lrclibRepoProvider = lrclibRepoProvider;
    this.lrcWriterProvider = lrcWriterProvider;
  }

  @Override
  public AlbumDetailViewModel get() {
    return newInstance(savedStateProvider.get(), settingsProvider.get(), trackDaoProvider.get(), tagWriterProvider.get(), coverArtRepositoryProvider.get(), lrclibRepoProvider.get(), lrcWriterProvider.get());
  }

  public static AlbumDetailViewModel_Factory create(Provider<SavedStateHandle> savedStateProvider,
      Provider<SettingsRepository> settingsProvider, Provider<TrackDao> trackDaoProvider,
      Provider<TagWriter> tagWriterProvider,
      Provider<CoverArtRepository> coverArtRepositoryProvider,
      Provider<LrclibRepository> lrclibRepoProvider, Provider<LrcWriter> lrcWriterProvider) {
    return new AlbumDetailViewModel_Factory(savedStateProvider, settingsProvider, trackDaoProvider, tagWriterProvider, coverArtRepositoryProvider, lrclibRepoProvider, lrcWriterProvider);
  }

  public static AlbumDetailViewModel newInstance(SavedStateHandle savedState,
      SettingsRepository settings, TrackDao trackDao, TagWriter tagWriter,
      CoverArtRepository coverArtRepository, LrclibRepository lrclibRepo, LrcWriter lrcWriter) {
    return new AlbumDetailViewModel(savedState, settings, trackDao, tagWriter, coverArtRepository, lrclibRepo, lrcWriter);
  }
}
