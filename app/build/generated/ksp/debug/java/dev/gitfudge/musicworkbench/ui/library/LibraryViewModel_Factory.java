package dev.gitfudge.musicworkbench.ui.library;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.art.CoverArtRepository;
import dev.gitfudge.musicworkbench.data.db.TrackDao;
import dev.gitfudge.musicworkbench.data.lyrics.LrcWriter;
import dev.gitfudge.musicworkbench.data.lyrics.LrclibRepository;
import dev.gitfudge.musicworkbench.data.scan.MediaScanner;
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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<SettingsRepository> settingsProvider;

  private final Provider<TrackDao> trackDaoProvider;

  private final Provider<MediaScanner> scannerProvider;

  private final Provider<LrclibRepository> lrclibRepoProvider;

  private final Provider<LrcWriter> lrcWriterProvider;

  private final Provider<TagWriter> tagWriterProvider;

  private final Provider<CoverArtRepository> coverArtRepositoryProvider;

  public LibraryViewModel_Factory(Provider<SettingsRepository> settingsProvider,
      Provider<TrackDao> trackDaoProvider, Provider<MediaScanner> scannerProvider,
      Provider<LrclibRepository> lrclibRepoProvider, Provider<LrcWriter> lrcWriterProvider,
      Provider<TagWriter> tagWriterProvider,
      Provider<CoverArtRepository> coverArtRepositoryProvider) {
    this.settingsProvider = settingsProvider;
    this.trackDaoProvider = trackDaoProvider;
    this.scannerProvider = scannerProvider;
    this.lrclibRepoProvider = lrclibRepoProvider;
    this.lrcWriterProvider = lrcWriterProvider;
    this.tagWriterProvider = tagWriterProvider;
    this.coverArtRepositoryProvider = coverArtRepositoryProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(settingsProvider.get(), trackDaoProvider.get(), scannerProvider.get(), lrclibRepoProvider.get(), lrcWriterProvider.get(), tagWriterProvider.get(), coverArtRepositoryProvider.get());
  }

  public static LibraryViewModel_Factory create(Provider<SettingsRepository> settingsProvider,
      Provider<TrackDao> trackDaoProvider, Provider<MediaScanner> scannerProvider,
      Provider<LrclibRepository> lrclibRepoProvider, Provider<LrcWriter> lrcWriterProvider,
      Provider<TagWriter> tagWriterProvider,
      Provider<CoverArtRepository> coverArtRepositoryProvider) {
    return new LibraryViewModel_Factory(settingsProvider, trackDaoProvider, scannerProvider, lrclibRepoProvider, lrcWriterProvider, tagWriterProvider, coverArtRepositoryProvider);
  }

  public static LibraryViewModel newInstance(SettingsRepository settings, TrackDao trackDao,
      MediaScanner scanner, LrclibRepository lrclibRepo, LrcWriter lrcWriter, TagWriter tagWriter,
      CoverArtRepository coverArtRepository) {
    return new LibraryViewModel(settings, trackDao, scanner, lrclibRepo, lrcWriter, tagWriter, coverArtRepository);
  }
}
