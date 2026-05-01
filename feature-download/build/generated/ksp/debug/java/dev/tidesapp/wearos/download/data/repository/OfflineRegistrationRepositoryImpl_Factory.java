package dev.tidesapp.wearos.download.data.repository;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import com.tidal.sdk.auth.CredentialsProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi;
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
public final class OfflineRegistrationRepositoryImpl_Factory implements Factory<OfflineRegistrationRepositoryImpl> {
  private final Provider<TidesOfflineApi> offlineApiProvider;

  private final Provider<CredentialsProvider> credentialsProvider;

  private final Provider<DataStore<Preferences>> dataStoreProvider;

  private OfflineRegistrationRepositoryImpl_Factory(Provider<TidesOfflineApi> offlineApiProvider,
      Provider<CredentialsProvider> credentialsProvider,
      Provider<DataStore<Preferences>> dataStoreProvider) {
    this.offlineApiProvider = offlineApiProvider;
    this.credentialsProvider = credentialsProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public OfflineRegistrationRepositoryImpl get() {
    return newInstance(offlineApiProvider.get(), credentialsProvider.get(), dataStoreProvider.get());
  }

  public static OfflineRegistrationRepositoryImpl_Factory create(
      Provider<TidesOfflineApi> offlineApiProvider,
      Provider<CredentialsProvider> credentialsProvider,
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new OfflineRegistrationRepositoryImpl_Factory(offlineApiProvider, credentialsProvider, dataStoreProvider);
  }

  public static OfflineRegistrationRepositoryImpl newInstance(TidesOfflineApi offlineApi,
      CredentialsProvider credentialsProvider, DataStore<Preferences> dataStore) {
    return new OfflineRegistrationRepositoryImpl(offlineApi, credentialsProvider, dataStore);
  }
}
