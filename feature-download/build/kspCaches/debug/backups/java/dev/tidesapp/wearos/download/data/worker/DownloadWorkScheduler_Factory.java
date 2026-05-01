package dev.tidesapp.wearos.download.data.worker;

import androidx.work.WorkManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository;
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
public final class DownloadWorkScheduler_Factory implements Factory<DownloadWorkScheduler> {
  private final Provider<WorkManager> workManagerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private DownloadWorkScheduler_Factory(Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.workManagerProvider = workManagerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DownloadWorkScheduler get() {
    return newInstance(workManagerProvider.get(), settingsRepositoryProvider.get());
  }

  public static DownloadWorkScheduler_Factory create(Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DownloadWorkScheduler_Factory(workManagerProvider, settingsRepositoryProvider);
  }

  public static DownloadWorkScheduler newInstance(WorkManager workManager,
      SettingsRepository settingsRepository) {
    return new DownloadWorkScheduler(workManager, settingsRepository);
  }
}
