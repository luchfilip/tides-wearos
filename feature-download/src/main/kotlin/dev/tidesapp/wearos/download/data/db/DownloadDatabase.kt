package dev.tidesapp.wearos.download.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadedTrackEntity::class, DownloadedCollectionEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class DownloadDatabase : RoomDatabase() {
    abstract fun downloadedTrackDao(): DownloadedTrackDao
    abstract fun downloadedCollectionDao(): DownloadedCollectionDao
}
