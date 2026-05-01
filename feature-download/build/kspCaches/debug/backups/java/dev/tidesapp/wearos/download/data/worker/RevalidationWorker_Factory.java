package dev.tidesapp.wearos.download.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.tidal.sdk.auth.CredentialsProvider;
import dagger.internal.DaggerGenerated;
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
public final class RevalidationWorker_Factory {
  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<TidesOfflineApi> offlineApiProvider;

  private final Provider<CredentialsProvider> credentialsProvider;

  private RevalidationWorker_Factory(Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<TidesOfflineApi> offlineApiProvider,
      Provider<CredentialsProvider> credentialsProvider) {
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.offlineApiProvider = offlineApiProvider;
    this.credentialsProvider = credentialsProvider;
  }

  public RevalidationWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, downloadRepositoryProvider.get(), offlineApiProvider.get(), credentialsProvider.get());
  }

  public static RevalidationWorker_Factory create(
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<TidesOfflineApi> offlineApiProvider,
      Provider<CredentialsProvider> credentialsProvider) {
    return new RevalidationWorker_Factory(downloadRepositoryProvider, offlineApiProvider, credentialsProvider);
  }

  public static RevalidationWorker newInstance(Context context, WorkerParameters params,
      DownloadRepository downloadRepository, TidesOfflineApi offlineApi,
      CredentialsProvider credentialsProvider) {
    return new RevalidationWorker(context, params, downloadRepository, offlineApi, credentialsProvider);
  }
}
