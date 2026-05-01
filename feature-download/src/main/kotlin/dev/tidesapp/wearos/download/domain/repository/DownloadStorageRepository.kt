package dev.tidesapp.wearos.download.domain.repository

import dev.tidesapp.wearos.download.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DownloadStorageRepository {
    fun getStorageInfo(): Flow<StorageInfo>
    suspend fun getDownloadDirectory(): File
    suspend fun hasSpaceForTrack(estimatedBytes: Long): Boolean
    suspend fun clearAllDownloads()
}
