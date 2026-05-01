package dev.tidesapp.wearos.download.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedCollectionDao {

    @Query("SELECT * FROM downloaded_collections ORDER BY downloadedAt DESC")
    fun getAll(): Flow<List<DownloadedCollectionEntity>>

    @Query("SELECT * FROM downloaded_collections WHERE id = :id AND type = :type")
    suspend fun getById(id: String, type: String): DownloadedCollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedCollectionEntity)

    @Update
    suspend fun update(entity: DownloadedCollectionEntity)

    @Query("DELETE FROM downloaded_collections WHERE id = :id AND type = :type")
    suspend fun deleteById(id: String, type: String)

    @Query("DELETE FROM downloaded_collections")
    suspend fun deleteAll()
}
