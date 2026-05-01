package dev.tidesapp.wearos.download.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadStorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: DownloadedTrackDao,
    private val collectionDao: DownloadedCollectionDao,
    private val settingsRepository: SettingsRepository,
) : DownloadStorageRepository {

    override fun getStorageInfo(): Flow<StorageInfo> = combine(
        trackDao.getTotalSizeBytes(),
        trackDao.getTrackCount(),
        settingsRepository.getStorageLimitBytes(),
    ) { used, count, limit ->
        StorageInfo(used, limit, count)
    }

    override suspend fun getDownloadDirectory(): File {
        val dir = File(context.filesDir, "offline_tracks")
        dir.mkdirs()
        return dir
    }

    override suspend fun hasSpaceForTrack(estimatedBytes: Long): Boolean {
        val used = trackDao.getTotalSizeBytes().first()
        val limit = settingsRepository.getStorageLimitBytes().first()
        return (used + estimatedBytes) <= limit
    }

    override suspend fun clearAllDownloads() {
        trackDao.deleteAll()
        collectionDao.deleteAll()
        getDownloadDirectory().deleteRecursively()
        getDownloadDirectory().mkdirs()
    }
}
