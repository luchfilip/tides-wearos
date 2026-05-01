package dev.tidesapp.wearos.download.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RevalidationWorker_AssistedFactory_Impl implements RevalidationWorker_AssistedFactory {
  private final RevalidationWorker_Factory delegateFactory;

  RevalidationWorker_AssistedFactory_Impl(RevalidationWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public RevalidationWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<RevalidationWorker_AssistedFactory> create(
      RevalidationWorker_Factory delegateFactory) {
    return InstanceFactory.create(new RevalidationWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<RevalidationWorker_AssistedFactory> createFactoryProvider(
      RevalidationWorker_Factory delegateFactory) {
    return InstanceFactory.create(new RevalidationWorker_AssistedFactory_Impl(delegateFactory));
  }
}
