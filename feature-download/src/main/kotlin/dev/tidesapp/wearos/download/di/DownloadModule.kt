package dev.tidesapp.wearos.download.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.data.db.DownloadDatabase
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao
import dev.tidesapp.wearos.download.data.repository.DownloadRepositoryImpl
import dev.tidesapp.wearos.download.data.repository.DownloadStorageRepositoryImpl
import dev.tidesapp.wearos.download.data.repository.OfflineRegistrationRepositoryImpl
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository
import dev.tidesapp.wearos.download.domain.repository.OfflineRegistrationRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadBindingsModule {

    @Binds
    abstract fun bindOfflineRegistrationRepository(
        impl: OfflineRegistrationRepositoryImpl,
    ): OfflineRegistrationRepository

    @Binds
    abstract fun bindDownloadRepository(
        impl: DownloadRepositoryImpl,
    ): DownloadRepository

    @Binds
    abstract fun bindDownloadStorageRepository(
        impl: DownloadStorageRepositoryImpl,
    ): DownloadStorageRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DownloadProvidesModule {

    @Provides
    @Singleton
    fun provideDownloadDatabase(
        @ApplicationContext context: Context,
    ): DownloadDatabase = Room.databaseBuilder(
        context,
        DownloadDatabase::class.java,
        "tides_downloads",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideDownloadedTrackDao(database: DownloadDatabase): DownloadedTrackDao =
        database.downloadedTrackDao()

    @Provides
    fun provideDownloadedCollectionDao(database: DownloadDatabase): DownloadedCollectionDao =
        database.downloadedCollectionDao()

    @Provides
    @Singleton
    fun provideTidesOfflineApi(retrofit: Retrofit): TidesOfflineApi =
        retrofit.create(TidesOfflineApi::class.java)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
