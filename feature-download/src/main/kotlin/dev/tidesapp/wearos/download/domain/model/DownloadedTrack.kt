package dev.tidesapp.wearos.download.domain.model

data class DownloadedTrack(
    val trackId: Long,
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
    val state: DownloadState,
    val collectionId: String,
    val collectionType: CollectionType,
)
