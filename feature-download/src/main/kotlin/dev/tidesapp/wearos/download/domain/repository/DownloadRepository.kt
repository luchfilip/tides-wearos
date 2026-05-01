package dev.tidesapp.wearos.download.domain.repository

import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.download.data.download.TrackDownloadResult
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getDownloadedCollections(): Flow<List<DownloadedCollection>>
    fun getDownloadedTracksForCollection(collectionId: String): Flow<List<DownloadedTrack>>
    fun getStorageInfo(): Flow<StorageInfo>
    suspend fun isTrackDownloaded(trackId: Long): Boolean
    suspend fun getDownloadedTrack(trackId: Long): DownloadedTrack?
    suspend fun queueCollectionDownload(
        collectionId: String,
        type: CollectionType,
        title: String,
        imageUrl: String,
        tracks: List<TrackItem>,
    )
    suspend fun markTrackCompleted(trackId: Long, result: TrackDownloadResult)
    suspend fun markTrackFailed(trackId: Long, error: String)
    suspend fun deleteCollection(collectionId: String, type: CollectionType)
    suspend fun deleteTrack(trackId: Long)
    suspend fun getExpiredTracks(currentTimeSeconds: Long): List<DownloadedTrack>
    suspend fun getTracksNeedingRevalidation(currentTimeSeconds: Long): List<DownloadedTrack>
    suspend fun getPendingTracksForCollection(collectionId: String): List<DownloadedTrack>
}
