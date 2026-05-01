package dev.tidesapp.wearos.download.domain.model

data class StorageInfo(
    val usedBytes: Long,
    val limitBytes: Long,
    val trackCount: Int,
)
