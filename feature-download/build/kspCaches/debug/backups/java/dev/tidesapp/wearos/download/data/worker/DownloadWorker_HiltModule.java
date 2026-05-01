package dev.tidesapp.wearos.download.data.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = DownloadWorker.class
)
public interface DownloadWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("dev.tidesapp.wearos.download.data.worker.DownloadWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(DownloadWorker_AssistedFactory factory);
}
