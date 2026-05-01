package dev.tidesapp.wearos.download.data.manifest;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class DashManifestParser_Factory implements Factory<DashManifestParser> {
  @Override
  public DashManifestParser get() {
    return newInstance();
  }

  public static DashManifestParser_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DashManifestParser newInstance() {
    return new DashManifestParser();
  }

  private static final class InstanceHolder {
    static final DashManifestParser_Factory INSTANCE = new DashManifestParser_Factory();
  }
}
