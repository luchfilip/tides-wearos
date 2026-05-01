package dev.tidesapp.wearos.download.ui.downloadmanager;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler;
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository;
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository;
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
public final class DownloadManagerViewModel_Factory implements Factory<DownloadManagerViewModel> {
  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<DownloadStorageRepository> downloadStorageRepositoryProvider;

  private final Provider<DownloadWorkScheduler> downloadWorkSchedulerProvider;

  private DownloadManagerViewModel_Factory(Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<DownloadStorageRepository> downloadStorageRepositoryProvider,
      Provider<DownloadWorkScheduler> downloadWorkSchedulerProvider) {
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.downloadStorageRepositoryProvider = downloadStorageRepositoryProvider;
    this.downloadWorkSchedulerProvider = downloadWorkSchedulerProvider;
  }

  @Override
  public DownloadManagerViewModel get() {
    return newInstance(downloadRepositoryProvider.get(), downloadStorageRepositoryProvider.get(), downloadWorkSchedulerProvider.get());
  }

  public static DownloadManagerViewModel_Factory create(
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<DownloadStorageRepository> downloadStorageRepositoryProvider,
      Provider<DownloadWorkScheduler> downloadWorkSchedulerProvider) {
    return new DownloadManagerViewModel_Factory(downloadRepositoryProvider, downloadStorageRepositoryProvider, downloadWorkSchedulerProvider);
  }

  public static DownloadManagerViewModel newInstance(DownloadRepository downloadRepository,
      DownloadStorageRepository downloadStorageRepository,
      DownloadWorkScheduler downloadWorkScheduler) {
    return new DownloadManagerViewModel(downloadRepository, downloadStorageRepository, downloadWorkScheduler);
  }
}
