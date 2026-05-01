package dev.tidesapp.wearos.download.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.db.DownloadDatabase;
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao;
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
public final class DownloadProvidesModule_ProvideDownloadedCollectionDaoFactory implements Factory<DownloadedCollectionDao> {
  private final Provider<DownloadDatabase> databaseProvider;

  private DownloadProvidesModule_ProvideDownloadedCollectionDaoFactory(
      Provider<DownloadDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DownloadedCollectionDao get() {
    return provideDownloadedCollectionDao(databaseProvider.get());
  }

  public static DownloadProvidesModule_ProvideDownloadedCollectionDaoFactory create(
      Provider<DownloadDatabase> databaseProvider) {
    return new DownloadProvidesModule_ProvideDownloadedCollectionDaoFactory(databaseProvider);
  }

  public static DownloadedCollectionDao provideDownloadedCollectionDao(DownloadDatabase database) {
    return Preconditions.checkNotNullFromProvides(DownloadProvidesModule.INSTANCE.provideDownloadedCollectionDao(database));
  }
}
