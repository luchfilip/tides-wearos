package dev.tidesapp.wearos.download.data.manifest

data class SegmentInfo(
    val initUrl: String,
    val mediaUrls: List<String>,
    val codec: String,
    val bandwidth: Long,
    val sampleRate: Int,
    val durationSeconds: Double,
)
