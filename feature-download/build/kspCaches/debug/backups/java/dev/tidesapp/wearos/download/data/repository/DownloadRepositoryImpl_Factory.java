package dev.tidesapp.wearos.download.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao;
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao;
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
public final class DownloadRepositoryImpl_Factory implements Factory<DownloadRepositoryImpl> {
  private final Provider<DownloadedTrackDao> trackDaoProvider;

  private final Provider<DownloadedCollectionDao> collectionDaoProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private DownloadRepositoryImpl_Factory(Provider<DownloadedTrackDao> trackDaoProvider,
      Provider<DownloadedCollectionDao> collectionDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.trackDaoProvider = trackDaoProvider;
    this.collectionDaoProvider = collectionDaoProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DownloadRepositoryImpl get() {
    return newInstance(trackDaoProvider.get(), collectionDaoProvider.get(), settingsRepositoryProvider.get());
  }

  public static DownloadRepositoryImpl_Factory create(Provider<DownloadedTrackDao> trackDaoProvider,
      Provider<DownloadedCollectionDao> collectionDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DownloadRepositoryImpl_Factory(trackDaoProvider, collectionDaoProvider, settingsRepositoryProvider);
  }

  public static DownloadRepositoryImpl newInstance(DownloadedTrackDao trackDao,
      DownloadedCollectionDao collectionDao, SettingsRepository settingsRepository) {
    return new DownloadRepositoryImpl(trackDao, collectionDao, settingsRepository);
  }
}
