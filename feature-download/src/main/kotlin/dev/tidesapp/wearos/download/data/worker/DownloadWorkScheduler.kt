package dev.tidesapp.wearos.download.data.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and manages [DownloadWorker] jobs via WorkManager.
 *
 * Each collection download is enqueued as unique work keyed by the
 * collection ID, so re-requesting a download that is already queued or
 * running is a no-op ([ExistingWorkPolicy.KEEP]).
 *
 * Network constraints are derived from the user's "Wi-Fi only" setting
 * at enqueue time.
 */
@Singleton
class DownloadWorkScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository,
) {

    /**
     * Enqueues a one-time download job for [collectionId].
     *
     * The job will only run when the required network type is available
     * (unmetered if the user has Wi-Fi-only downloads enabled, otherwise
     * any connectivity).
     */
    suspend fun enqueueCollectionDownload(collectionId: String, type: CollectionType) {
        val wifiOnly = settingsRepository.isWifiOnly().first()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED,
            )
            .build()

        val inputData = workDataOf(
            DownloadWorker.KEY_COLLECTION_ID to collectionId,
            DownloadWorker.KEY_COLLECTION_TYPE to type.name,
        )

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(TAG_DOWNLOAD)
            .build()

        workManager.enqueueUniqueWork(
            workName(collectionId),
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    /** Cancels the download job for [collectionId] if it is queued or running. */
    fun cancelCollectionDownload(collectionId: String) {
        workManager.cancelUniqueWork(workName(collectionId))
    }

    /** Cancels every download job managed by this scheduler. */
    fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG_DOWNLOAD)
    }

    /** Observes the work status for a specific collection download. */
    fun getWorkStatus(collectionId: String) =
        workManager.getWorkInfosForUniqueWorkFlow(workName(collectionId))

    /**
     * Enqueues a periodic worker that revalidates offline licenses every 24 hours.
     * Uses [ExistingPeriodicWorkPolicy.KEEP] so re-calling this method is safe
     * and won't reset the existing schedule.
     */
    fun schedulePeriodicRevalidation() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<RevalidationWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            REVALIDATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun workName(collectionId: String) = "download_$collectionId"

    companion object {
        const val TAG_DOWNLOAD = "tides_download"
        const val REVALIDATION_WORK_NAME = "tides_revalidation"
    }
}
