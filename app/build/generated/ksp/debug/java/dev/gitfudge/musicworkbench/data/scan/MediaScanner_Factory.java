package dev.gitfudge.musicworkbench.data.scan;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.gitfudge.musicworkbench.data.db.TrackDao;
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
public final class MediaScanner_Factory implements Factory<MediaScanner> {
  private final Provider<Context> contextProvider;

  private final Provider<TrackDao> trackDaoProvider;

  public MediaScanner_Factory(Provider<Context> contextProvider,
      Provider<TrackDao> trackDaoProvider) {
    this.contextProvider = contextProvider;
    this.trackDaoProvider = trackDaoProvider;
  }

  @Override
  public MediaScanner get() {
    return newInstance(contextProvider.get(), trackDaoProvider.get());
  }

  public static MediaScanner_Factory create(Provider<Context> contextProvider,
      Provider<TrackDao> trackDaoProvider) {
    return new MediaScanner_Factory(contextProvider, trackDaoProvider);
  }

  public static MediaScanner newInstance(Context context, TrackDao trackDao) {
    return new MediaScanner(context, trackDao);
  }
}
