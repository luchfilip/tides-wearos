package dev.tidesapp.wearos.download.data.download

/**
 * Result of a successful track download.
 *
 * Contains the local file path plus metadata from the playback-info
 * response that the caller needs for database persistence and
 * offline license management.
 */
data class TrackDownloadResult(
    val filePath: String,
    val fileSize: Long,
    val manifestHash: String,
    val offlineRevalidateAt: Long,
    val offlineValidUntil: Long,
    val audioQuality: String,
    val bitDepth: Int,
    val sampleRate: Int,
)
