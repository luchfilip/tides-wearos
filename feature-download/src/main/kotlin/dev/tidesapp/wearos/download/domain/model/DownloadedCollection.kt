package dev.tidesapp.wearos.download.domain.model

data class DownloadedCollection(
    val id: String,
    val type: CollectionType,
    val title: String,
    val imageUrl: String,
    val trackCount: Int,
    val downloadedTrackCount: Int,
    val totalSizeBytes: Long,
    val downloadedAt: Long,
    val state: DownloadState,
)
