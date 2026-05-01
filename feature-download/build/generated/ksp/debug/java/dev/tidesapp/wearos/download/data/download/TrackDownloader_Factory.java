package dev.tidesapp.wearos.download.data.download;

import com.tidal.sdk.auth.CredentialsProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi;
import dev.tidesapp.wearos.download.data.manifest.DashManifestParser;
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
public final class TrackDownloader_Factory implements Factory<TrackDownloader> {
  private final Provider<TidesOfflineApi> offlineApiProvider;

  private final Provider<DashManifestParser> manifestParserProvider;

  private final Provider<SegmentDownloader> segmentDownloaderProvider;

  private final Provider<CredentialsProvider> credentialsProvider;

  private TrackDownloader_Factory(Provider<TidesOfflineApi> offlineApiProvider,
      Provider<DashManifestParser> manifestParserProvider,
      Provider<SegmentDownloader> segmentDownloaderProvider,
      Provider<CredentialsProvider> credentialsProvider) {
    this.offlineApiProvider = offlineApiProvider;
    this.manifestParserProvider = manifestParserProvider;
    this.segmentDownloaderProvider = segmentDownloaderProvider;
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  public TrackDownloader get() {
    return newInstance(offlineApiProvider.get(), manifestParserProvider.get(), segmentDownloaderProvider.get(), credentialsProvider.get());
  }

  public static TrackDownloader_Factory create(Provider<TidesOfflineApi> offlineApiProvider,
      Provider<DashManifestParser> manifestParserProvider,
      Provider<SegmentDownloader> segmentDownloaderProvider,
      Provider<CredentialsProvider> credentialsProvider) {
    return new TrackDownloader_Factory(offlineApiProvider, manifestParserProvider, segmentDownloaderProvider, credentialsProvider);
  }

  public static TrackDownloader newInstance(TidesOfflineApi offlineApi,
      DashManifestParser manifestParser, SegmentDownloader segmentDownloader,
      CredentialsProvider credentialsProvider) {
    return new TrackDownloader(offlineApi, manifestParser, segmentDownloader, credentialsProvider);
  }
}
