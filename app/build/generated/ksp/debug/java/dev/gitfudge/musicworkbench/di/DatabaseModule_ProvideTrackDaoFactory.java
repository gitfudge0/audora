package dev.gitfudge.musicworkbench.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.db.LibraryDatabase;
import dev.gitfudge.musicworkbench.data.db.TrackDao;
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
public final class DatabaseModule_ProvideTrackDaoFactory implements Factory<TrackDao> {
  private final Provider<LibraryDatabase> dbProvider;

  public DatabaseModule_ProvideTrackDaoFactory(Provider<LibraryDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TrackDao get() {
    return provideTrackDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTrackDaoFactory create(Provider<LibraryDatabase> dbProvider) {
    return new DatabaseModule_ProvideTrackDaoFactory(dbProvider);
  }

  public static TrackDao provideTrackDao(LibraryDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTrackDao(db));
  }
}
