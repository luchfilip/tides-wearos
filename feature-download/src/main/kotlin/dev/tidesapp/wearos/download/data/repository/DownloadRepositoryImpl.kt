package dev.tidesapp.wearos.download.data.repository

import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionEntity
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao
import dev.tidesapp.wearos.download.data.db.DownloadedTrackEntity
import dev.tidesapp.wearos.download.data.db.toDomain
import dev.tidesapp.wearos.download.data.download.TrackDownloadResult
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val trackDao: DownloadedTrackDao,
    private val collectionDao: DownloadedCollectionDao,
    private val settingsRepository: SettingsRepository,
) : DownloadRepository {

    override fun getDownloadedCollections(): Flow<List<DownloadedCollection>> =
        collectionDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getDownloadedTracksForCollection(collectionId: String): Flow<List<DownloadedTrack>> =
        trackDao.getByCollectionId(collectionId).map { entities -> entities.map { it.toDomain() } }

    override fun getStorageInfo(): Flow<StorageInfo> = combine(
        trackDao.getTotalSizeBytes(),
        trackDao.getTrackCount(),
        settingsRepository.getStorageLimitBytes(),
    ) { used, count, limit ->
        StorageInfo(usedBytes = used, limitBytes = limit, trackCount = count)
    }

    override suspend fun isTrackDownloaded(trackId: Long): Boolean =
        trackDao.isTrackDownloaded(trackId)

    override suspend fun getDownloadedTrack(trackId: Long): DownloadedTrack? =
        trackDao.getByTrackId(trackId)?.toDomain()

    override suspend fun queueCollectionDownload(
        collectionId: String,
        type: CollectionType,
        title: String,
        imageUrl: String,
        tracks: List<TrackItem>,
    ) {
        val now = System.currentTimeMillis()

        val collectionEntity = DownloadedCollectionEntity(
            id = collectionId,
            type = type.name,
            title = title,
            imageUrl = imageUrl,
            trackCount = tracks.size,
            downloadedTrackCount = 0,
            totalSizeBytes = 0L,
            downloadedAt = now,
            state = DownloadState.PENDING.name,
        )
        collectionDao.insert(collectionEntity)

        val trackEntities = tracks.map { track ->
            DownloadedTrackEntity(
                trackId = track.id.toLong(),
                title = track.title,
                artistName = track.artistName,
                albumTitle = track.albumTitle,
                imageUrl = track.imageUrl.orEmpty(),
                duration = track.duration,
                trackNumber = track.trackNumber,
                filePath = "",
                fileSize = 0L,
                audioQuality = "",
                manifestHash = "",
                offlineRevalidateAt = 0L,
                offlineValidUntil = 0L,
                downloadedAt = now,
                state = DownloadState.PENDING.name,
                collectionId = collectionId,
                collectionType = type.name,
            )
        }
        trackDao.insertAll(trackEntities)
    }

    override suspend fun markTrackCompleted(trackId: Long, result: TrackDownloadResult) {
        trackDao.updateCompleted(
            trackId = trackId,
            state = DownloadState.COMPLETED.name,
            filePath = result.filePath,
            fileSize = result.fileSize,
            manifestHash = result.manifestHash,
            revalidateAt = result.offlineRevalidateAt,
            validUntil = result.offlineValidUntil,
        )

        recalculateCollectionProgress(trackId)
    }

    override suspend fun markTrackFailed(trackId: Long, error: String) {
        trackDao.updateState(trackId, DownloadState.FAILED.name)
        recalculateCollectionProgress(trackId)
    }

    override suspend fun deleteCollection(collectionId: String, type: CollectionType) {
        // Delete physical files for all tracks in the collection
        val tracks = trackDao.getByCollectionId(collectionId).first()
        for (track in tracks) {
            deleteFileIfExists(track.filePath)
        }

        trackDao.deleteByCollectionId(collectionId)
        collectionDao.deleteById(collectionId, type.name)
    }

    override suspend fun deleteTrack(trackId: Long) {
        val track = trackDao.getByTrackId(trackId)
        if (track != null) {
            deleteFileIfExists(track.filePath)
        }
        trackDao.deleteByTrackId(trackId)
    }

    override suspend fun getExpiredTracks(currentTimeSeconds: Long): List<DownloadedTrack> =
        trackDao.getExpiredTracks(currentTimeSeconds).map { it.toDomain() }

    override suspend fun getTracksNeedingRevalidation(currentTimeSeconds: Long): List<DownloadedTrack> =
        trackDao.getTracksNeedingRevalidation(currentTimeSeconds).map { it.toDomain() }

    override suspend fun getPendingTracksForCollection(collectionId: String): List<DownloadedTrack> =
        trackDao.getPendingByCollectionId(collectionId).map { it.toDomain() }

    private suspend fun recalculateCollectionProgress(trackId: Long) {
        val trackEntity = trackDao.getByTrackId(trackId) ?: return
        val collectionId = trackEntity.collectionId
        val collectionType = trackEntity.collectionType

        val collection = collectionDao.getById(collectionId, collectionType) ?: return
        val allTracks = trackDao.getByCollectionId(collectionId).first()

        val completedTracks = allTracks.filter { it.state == DownloadState.COMPLETED.name }
        val failedTracks = allTracks.filter { it.state == DownloadState.FAILED.name }
        val pendingTracks = allTracks.filter {
            it.state == DownloadState.PENDING.name || it.state == DownloadState.DOWNLOADING.name
        }
        val completedCount = completedTracks.size
        val totalSize = completedTracks.sumOf { it.fileSize }

        val newState = when {
            completedCount >= collection.trackCount -> DownloadState.COMPLETED.name
            pendingTracks.isEmpty() && failedTracks.isNotEmpty() && completedTracks.isEmpty() ->
                DownloadState.FAILED.name
            pendingTracks.isEmpty() && failedTracks.isNotEmpty() ->
                DownloadState.COMPLETED.name // partial success
            else -> DownloadState.DOWNLOADING.name
        }

        collectionDao.update(
            collection.copy(
                downloadedTrackCount = completedCount,
                totalSizeBytes = totalSize,
                state = newState,
            ),
        )
    }

    private fun deleteFileIfExists(filePath: String) {
        if (filePath.isNotBlank()) {
            File(filePath).delete()
        }
    }
}
