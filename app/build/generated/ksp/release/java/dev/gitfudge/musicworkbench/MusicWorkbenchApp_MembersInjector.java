package dev.gitfudge.musicworkbench;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MusicWorkbenchApp_MembersInjector implements MembersInjector<MusicWorkbenchApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public MusicWorkbenchApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<MusicWorkbenchApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new MusicWorkbenchApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(MusicWorkbenchApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("dev.gitfudge.musicworkbench.MusicWorkbenchApp.workerFactory")
  public static void injectWorkerFactory(MusicWorkbenchApp instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
