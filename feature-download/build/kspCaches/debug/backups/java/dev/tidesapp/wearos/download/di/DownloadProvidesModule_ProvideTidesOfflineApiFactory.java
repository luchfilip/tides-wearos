package dev.tidesapp.wearos.download.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class DownloadProvidesModule_ProvideTidesOfflineApiFactory implements Factory<TidesOfflineApi> {
  private final Provider<Retrofit> retrofitProvider;

  private DownloadProvidesModule_ProvideTidesOfflineApiFactory(
      Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TidesOfflineApi get() {
    return provideTidesOfflineApi(retrofitProvider.get());
  }

  public static DownloadProvidesModule_ProvideTidesOfflineApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new DownloadProvidesModule_ProvideTidesOfflineApiFactory(retrofitProvider);
  }

  public static TidesOfflineApi provideTidesOfflineApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(DownloadProvidesModule.INSTANCE.provideTidesOfflineApi(retrofit));
  }
}
