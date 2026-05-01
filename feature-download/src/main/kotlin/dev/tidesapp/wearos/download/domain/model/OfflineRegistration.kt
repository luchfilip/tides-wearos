package dev.tidesapp.wearos.download.domain.model

data class OfflineRegistration(
    val clientId: Long,
    val authorizedForOffline: Boolean,
    val authorizedAt: Long?,
)
