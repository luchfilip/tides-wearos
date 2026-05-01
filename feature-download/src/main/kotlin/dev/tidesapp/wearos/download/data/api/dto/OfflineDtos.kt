package dev.tidesapp.wearos.download.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionResponse(
    val sessionId: String = "",
    val userId: Long = 0,
    val countryCode: String = "",
    val client: SessionClientInfo = SessionClientInfo(),
)

@Serializable
data class SessionClientInfo(
    val id: Long = 0,
    val name: String = "",
    val authorizedForOffline: Boolean = false,
    val authorizedForOfflineDate: String? = null,
)

@Serializable
data class OfflineClientsResponse(
    val limit: Int = 0,
    val offset: Int = 0,
    val totalNumberOfItems: Int = 0,
    val items: List<SessionClientInfo> = emptyList(),
)

@Serializable
data class OfflinePlaybackInfoResponse(
    val trackId: Long = 0,
    val assetPresentation: String = "",
    val audioMode: String = "",
    val audioQuality: String = "",
    val streamingSessionId: String = "",
    val manifestMimeType: String = "",
    val manifestHash: String = "",
    val manifest: String = "",
    val albumReplayGain: Double = 0.0,
    val albumPeakAmplitude: Double = 0.0,
    val trackReplayGain: Double = 0.0,
    val trackPeakAmplitude: Double = 0.0,
    val offlineRevalidateAt: Long = 0,
    val offlineValidUntil: Long = 0,
    val bitDepth: Int = 0,
    val sampleRate: Int = 0,
)
