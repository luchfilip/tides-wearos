package dev.tidesapp.wearos.download.ui.downloads;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository;
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
public final class DownloadsViewModel_Factory implements Factory<DownloadsViewModel> {
  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private DownloadsViewModel_Factory(Provider<DownloadRepository> downloadRepositoryProvider) {
    this.downloadRepositoryProvider = downloadRepositoryProvider;
  }

  @Override
  public DownloadsViewModel get() {
    return newInstance(downloadRepositoryProvider.get());
  }

  public static DownloadsViewModel_Factory create(
      Provider<DownloadRepository> downloadRepositoryProvider) {
    return new DownloadsViewModel_Factory(downloadRepositoryProvider);
  }

  public static DownloadsViewModel newInstance(DownloadRepository downloadRepository) {
    return new DownloadsViewModel(downloadRepository);
  }
}
