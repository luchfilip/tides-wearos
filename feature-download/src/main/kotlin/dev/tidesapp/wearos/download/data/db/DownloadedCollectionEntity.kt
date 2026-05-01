package dev.tidesapp.wearos.download.data.db

import androidx.room.Entity

@Entity(
    tableName = "downloaded_collections",
    primaryKeys = ["id", "type"],
)
data class DownloadedCollectionEntity(
    val id: String,
    val type: String,
    val title: String,
    val imageUrl: String,
    val trackCount: Int,
    val downloadedTrackCount: Int,
    val totalSizeBytes: Long,
    val downloadedAt: Long,
    val state: String,
)
