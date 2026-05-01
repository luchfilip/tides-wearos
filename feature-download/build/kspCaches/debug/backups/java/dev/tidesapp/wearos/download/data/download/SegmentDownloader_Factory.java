package dev.tidesapp.wearos.download.data.download;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class SegmentDownloader_Factory implements Factory<SegmentDownloader> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private SegmentDownloader_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public SegmentDownloader get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static SegmentDownloader_Factory create(Provider<OkHttpClient> okHttpClientProvider) {
    return new SegmentDownloader_Factory(okHttpClientProvider);
  }

  public static SegmentDownloader newInstance(OkHttpClient okHttpClient) {
    return new SegmentDownloader(okHttpClient);
  }
}
