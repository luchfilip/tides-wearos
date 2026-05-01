package dev.tidesapp.wearos.download.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedTrackDao {

    @Query("SELECT * FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun getByTrackId(trackId: Long): DownloadedTrackEntity?

    @Query("SELECT * FROM downloaded_tracks WHERE collectionId = :collectionId")
    fun getByCollectionId(collectionId: String): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT * FROM downloaded_tracks WHERE state = 'COMPLETED'")
    fun getAllCompleted(): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT * FROM downloaded_tracks WHERE state = :state")
    fun getByState(state: String): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM downloaded_tracks WHERE state = 'COMPLETED'")
    fun getTotalSizeBytes(): Flow<Long>

    @Query("SELECT COUNT(*) FROM downloaded_tracks WHERE state = 'COMPLETED'")
    fun getTrackCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_tracks WHERE trackId = :trackId AND state = 'COMPLETED')")
    suspend fun isTrackDownloaded(trackId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedTrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DownloadedTrackEntity>)

    @Query("UPDATE downloaded_tracks SET state = :state WHERE trackId = :trackId")
    suspend fun updateState(trackId: Long, state: String)

    @Query(
        "UPDATE downloaded_tracks SET state = :state, filePath = :filePath, fileSize = :fileSize, " +
            "manifestHash = :manifestHash, offlineRevalidateAt = :revalidateAt, " +
            "offlineValidUntil = :validUntil WHERE trackId = :trackId",
    )
    suspend fun updateCompleted(
        trackId: Long,
        state: String,
        filePath: String,
        fileSize: Long,
        manifestHash: String,
        revalidateAt: Long,
        validUntil: Long,
    )

    @Query("DELETE FROM downloaded_tracks WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Long)

    @Query("DELETE FROM downloaded_tracks WHERE collectionId = :collectionId")
    suspend fun deleteByCollectionId(collectionId: String)

    @Query("DELETE FROM downloaded_tracks")
    suspend fun deleteAll()

    @Query("SELECT * FROM downloaded_tracks WHERE offlineValidUntil < :currentTime AND state = 'COMPLETED'")
    suspend fun getExpiredTracks(currentTime: Long): List<DownloadedTrackEntity>

    @Query("SELECT * FROM downloaded_tracks WHERE offlineRevalidateAt < :currentTime AND state = 'COMPLETED'")
    suspend fun getTracksNeedingRevalidation(currentTime: Long): List<DownloadedTrackEntity>

    @Query("SELECT * FROM downloaded_tracks WHERE collectionId = :collectionId AND state = 'PENDING'")
    suspend fun getPendingByCollectionId(collectionId: String): List<DownloadedTrackEntity>
}
