package dev.tidesapp.wearos.download.data.worker;

import com.tidal.sdk.auth.CredentialsProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi;
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
public final class RevalidationLogic_Factory implements Factory<RevalidationLogic> {
  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<TidesOfflineApi> offlineApiProvider;

  private final Provider<CredentialsProvider> credentialsProvider;

  private RevalidationLogic_Factory(Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<TidesOfflineApi> offlineApiProvider,
      Provider<CredentialsProvider> credentialsProvider) {
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.offlineApiProvider = offlineApiProvider;
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  public RevalidationLogic get() {
    return newInstance(downloadRepositoryProvider.get(), offlineApiProvider.get(), credentialsProvider.get());
  }

  public static RevalidationLogic_Factory create(
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<TidesOfflineApi> offlineApiProvider,
      Provider<CredentialsProvider> credentialsProvider) {
    return new RevalidationLogic_Factory(downloadRepositoryProvider, offlineApiProvider, credentialsProvider);
  }

  public static RevalidationLogic newInstance(DownloadRepository downloadRepository,
      TidesOfflineApi offlineApi, CredentialsProvider credentialsProvider) {
    return new RevalidationLogic(downloadRepository, offlineApi, credentialsProvider);
  }
}
