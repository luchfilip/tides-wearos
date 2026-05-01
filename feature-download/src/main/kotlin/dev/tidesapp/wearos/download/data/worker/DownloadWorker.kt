package dev.tidesapp.wearos.download.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.tidesapp.wearos.download.data.download.TrackDownloader
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository
import dev.tidesapp.wearos.download.domain.repository.OfflineRegistrationRepository
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

/**
 * Downloads all pending tracks for a single collection (album or playlist).
 *
 * The worker is enqueued by [DownloadWorkScheduler] and executes the
 * following steps:
 *
 * 1. Ensures the device is registered for offline playback.
 * 2. Registers the target collection for offline access with Tidal.
 * 3. Iterates over every pending track, downloading each one and
 *    marking it completed or failed in the local database.
 *
 * Storage is checked before each track. If there is not enough space
 * the track is marked failed with a storage error and the worker
 * continues to the next track (which also gets the storage check).
 *
 * The worker returns [Result.retry] when the offline authorization or
 * collection registration call fails (transient network errors), and
 * [Result.success] once every track has been attempted — even if some
 * individual tracks failed.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downloadRepository: DownloadRepository,
    private val offlineRegistration: OfflineRegistrationRepository,
    private val trackDownloader: TrackDownloader,
    private val storageRepository: DownloadStorageRepository,
    private val settingsRepository: SettingsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val collectionId = inputData.getString(KEY_COLLECTION_ID)
            ?: return Result.failure()
        val collectionType = inputData.getString(KEY_COLLECTION_TYPE)
            ?.let { CollectionType.valueOf(it) }
            ?: return Result.failure()

        // Step 1: try offline registration; fall back to STREAM mode if unsupported.
        val useStreamMode = try {
            offlineRegistration.ensureOfflineAuthorized().getOrThrow()
            offlineRegistration.registerCollectionOffline(
                collectionId, collectionType,
            ).getOrThrow()
            false // offline mode works
        } catch (_: Exception) {
            true // fall back to STREAM mode
        }

        // Step 2: resolve settings.
        val quality = settingsRepository.getDownloadQuality().first()
        val outputDir = storageRepository.getDownloadDirectory()

        // Step 3: download each pending track.
        val pendingTracks = downloadRepository.getPendingTracksForCollection(collectionId)

        for (track in pendingTracks) {
            val estimatedSize = estimateTrackSize(quality.name)
            if (!storageRepository.hasSpaceForTrack(estimatedSize)) {
                downloadRepository.markTrackFailed(track.trackId, "Insufficient storage")
                continue
            }

            try {
                val result = trackDownloader.downloadTrack(
                    trackId = track.trackId,
                    audioQuality = quality.name,
                    outputDir = outputDir,
                    useStreamMode = useStreamMode,
                )
                downloadRepository.markTrackCompleted(track.trackId, result)
            } catch (e: Exception) {
                downloadRepository.markTrackFailed(
                    track.trackId,
                    e.message ?: "Download failed",
                )
            }
        }

        return Result.success()
    }

    /**
     * Returns a conservative byte estimate for a ~5-minute track at the
     * given quality level. Used for a pre-download storage check so we
     * don't start a download that will fail mid-way.
     */
    private fun estimateTrackSize(quality: String): Long = when (quality) {
        "LOSSLESS" -> 40_000_000L // ~40 MB (FLAC)
        "HIGH" -> 10_000_000L     // ~10 MB (AAC 320 kbps)
        else -> 5_000_000L        // ~5 MB  (AAC 96 kbps)
    }

    companion object {
        const val KEY_COLLECTION_ID = "collection_id"
        const val KEY_COLLECTION_TYPE = "collection_type"
    }
}
