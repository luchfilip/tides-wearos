package dev.tidesapp.wearos.download.data.repository;

import android.content.Context;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DownloadStorageRepositoryImpl_Factory implements Factory<DownloadStorageRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<DownloadedTrackDao> trackDaoProvider;

  private final Provider<DownloadedCollectionDao> collectionDaoProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private DownloadStorageRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<DownloadedTrackDao> trackDaoProvider,
      Provider<DownloadedCollectionDao> collectionDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.trackDaoProvider = trackDaoProvider;
    this.collectionDaoProvider = collectionDaoProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DownloadStorageRepositoryImpl get() {
    return newInstance(contextProvider.get(), trackDaoProvider.get(), collectionDaoProvider.get(), settingsRepositoryProvider.get());
  }

  public static DownloadStorageRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<DownloadedTrackDao> trackDaoProvider,
      Provider<DownloadedCollectionDao> collectionDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DownloadStorageRepositoryImpl_Factory(contextProvider, trackDaoProvider, collectionDaoProvider, settingsRepositoryProvider);
  }

  public static DownloadStorageRepositoryImpl newInstance(Context context,
      DownloadedTrackDao trackDao, DownloadedCollectionDao collectionDao,
      SettingsRepository settingsRepository) {
    return new DownloadStorageRepositoryImpl(context, trackDao, collectionDao, settingsRepository);
  }
}
