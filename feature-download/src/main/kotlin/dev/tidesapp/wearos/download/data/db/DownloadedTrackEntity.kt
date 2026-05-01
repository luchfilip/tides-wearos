package dev.tidesapp.wearos.download.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "downloaded_tracks",
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["state"]),
    ],
)
data class DownloadedTrackEntity(
    @PrimaryKey val trackId: Long,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val imageUrl: String,
    val duration: Int,
    val trackNumber: Int,
    val filePath: String,
    val fileSize: Long,
    val audioQuality: String,
    val manifestHash: String,
    val offlineRevalidateAt: Long,
    val offlineValidUntil: Long,
    val downloadedAt: Long,
    val state: String,
    val collectionId: String,
    val collectionType: String,
)
