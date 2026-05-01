package dev.tidesapp.wearos.download.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.db.DownloadDatabase;
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao;
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
public final class DownloadProvidesModule_ProvideDownloadedTrackDaoFactory implements Factory<DownloadedTrackDao> {
  private final Provider<DownloadDatabase> databaseProvider;

  private DownloadProvidesModule_ProvideDownloadedTrackDaoFactory(
      Provider<DownloadDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DownloadedTrackDao get() {
    return provideDownloadedTrackDao(databaseProvider.get());
  }

  public static DownloadProvidesModule_ProvideDownloadedTrackDaoFactory create(
      Provider<DownloadDatabase> databaseProvider) {
    return new DownloadProvidesModule_ProvideDownloadedTrackDaoFactory(databaseProvider);
  }

  public static DownloadedTrackDao provideDownloadedTrackDao(DownloadDatabase database) {
    return Preconditions.checkNotNullFromProvides(DownloadProvidesModule.INSTANCE.provideDownloadedTrackDao(database));
  }
}
