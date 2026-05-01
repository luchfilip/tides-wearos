package dev.tidesapp.wearos.download.data.worker

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.data.download.TrackDownloadResult
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Result of a revalidation pass.
 */
enum class RevalidationResult {
    SUCCESS,
    RETRY,
}

/**
 * Testable extraction of the revalidation logic used by [RevalidationWorker].
 *
 * Checks expired tracks and tracks needing license renewal, calling back
 * to the Tidal API to refresh offline timestamps when the manifest hash
 * still matches. If the hash has changed the track is marked failed so
 * the download worker will re-download it.
 */
class RevalidationLogic @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val offlineApi: TidesOfflineApi,
    private val credentialsProvider: CredentialsProvider,
) {

    /**
     * Executes one revalidation pass for all downloaded tracks.
     *
     * 1. Marks fully-expired tracks as failed.
     * 2. For tracks whose revalidation window has arrived, fetches
     *    fresh playback info from the API.
     *    - If the manifest hash matches, updates the offline timestamps.
     *    - If the hash differs, marks the track failed so it gets
     *      re-downloaded.
     *    - Network errors are silently skipped (the track will be
     *      retried on the next periodic run).
     *
     * @param currentTimeSeconds epoch seconds used as "now".
     * @return [RevalidationResult.SUCCESS] on completion, or
     *         [RevalidationResult.RETRY] if credentials are unavailable.
     */
    suspend fun execute(currentTimeSeconds: Long): RevalidationResult {
        val token = try {
            getBearerToken()
        } catch (_: RuntimeException) {
            return RevalidationResult.RETRY
        }

        // Mark expired tracks.
        val expired = downloadRepository.getExpiredTracks(currentTimeSeconds)
        for (track in expired) {
            downloadRepository.markTrackFailed(track.trackId, "Offline license expired")
        }

        // Revalidate tracks whose window has arrived.
        val needsRevalidation = downloadRepository.getTracksNeedingRevalidation(currentTimeSeconds)
        for (track in needsRevalidation) {
            try {
                val playbackInfo = offlineApi.getOfflinePlaybackInfo(
                    token = "Bearer $token",
                    trackId = track.trackId.toString(),
                    audioQuality = track.audioQuality,
                    streamingSessionId = UUID.randomUUID().toString(),
                )

                if (playbackInfo.manifestHash == track.manifestHash) {
                    // Hash matches -- update timestamps.
                    downloadRepository.markTrackCompleted(
                        track.trackId,
                        TrackDownloadResult(
                            filePath = track.filePath,
                            fileSize = track.fileSize,
                            manifestHash = track.manifestHash,
                            offlineRevalidateAt = playbackInfo.offlineRevalidateAt,
                            offlineValidUntil = playbackInfo.offlineValidUntil,
                            audioQuality = track.audioQuality,
                            bitDepth = playbackInfo.bitDepth,
                            sampleRate = playbackInfo.sampleRate,
                        ),
                    )
                } else {
                    // Content changed -- mark for re-download.
                    downloadRepository.markTrackFailed(
                        track.trackId,
                        "Content changed, re-download needed",
                    )
                }
            } catch (_: Exception) {
                // Network error -- skip, will retry next period.
            }
        }

        return RevalidationResult.SUCCESS
    }

    private suspend fun getBearerToken(): String {
        val result = credentialsProvider.getCredentials(null)
        return result.successData?.token
            ?: throw RuntimeException("Failed to obtain credentials")
    }
}
