package dev.tidesapp.wearos.download.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.tidesapp.wearos.download.data.db.DownloadDatabase;
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
public final class DownloadProvidesModule_ProvideDownloadDatabaseFactory implements Factory<DownloadDatabase> {
  private final Provider<Context> contextProvider;

  private DownloadProvidesModule_ProvideDownloadDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DownloadDatabase get() {
    return provideDownloadDatabase(contextProvider.get());
  }

  public static DownloadProvidesModule_ProvideDownloadDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DownloadProvidesModule_ProvideDownloadDatabaseFactory(contextProvider);
  }

  public static DownloadDatabase provideDownloadDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DownloadProvidesModule.INSTANCE.provideDownloadDatabase(context));
  }
}
