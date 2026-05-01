package dev.tidesapp.wearos.download.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.download.TrackDownloader;
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository;
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository;
import dev.tidesapp.wearos.download.domain.repository.OfflineRegistrationRepository;
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository;
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
public final class DownloadWorker_Factory {
  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<OfflineRegistrationRepository> offlineRegistrationProvider;

  private final Provider<TrackDownloader> trackDownloaderProvider;

  private final Provider<DownloadStorageRepository> storageRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private DownloadWorker_Factory(Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<OfflineRegistrationRepository> offlineRegistrationProvider,
      Provider<TrackDownloader> trackDownloaderProvider,
      Provider<DownloadStorageRepository> storageRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.offlineRegistrationProvider = offlineRegistrationProvider;
    this.trackDownloaderProvider = trackDownloaderProvider;
    this.storageRepositoryProvider = storageRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public DownloadWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, downloadRepositoryProvider.get(), offlineRegistrationProvider.get(), trackDownloaderProvider.get(), storageRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static DownloadWorker_Factory create(
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<OfflineRegistrationRepository> offlineRegistrationProvider,
      Provider<TrackDownloader> trackDownloaderProvider,
      Provider<DownloadStorageRepository> storageRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DownloadWorker_Factory(downloadRepositoryProvider, offlineRegistrationProvider, trackDownloaderProvider, storageRepositoryProvider, settingsRepositoryProvider);
  }

  public static DownloadWorker newInstance(Context context, WorkerParameters params,
      DownloadRepository downloadRepository, OfflineRegistrationRepository offlineRegistration,
      TrackDownloader trackDownloader, DownloadStorageRepository storageRepository,
      SettingsRepository settingsRepository) {
    return new DownloadWorker(context, params, downloadRepository, offlineRegistration, trackDownloader, storageRepository, settingsRepository);
  }
}
