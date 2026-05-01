package dev.tidesapp.wearos.download.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tidal.sdk.auth.CredentialsProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository

/**
 * Periodic worker that revalidates offline licenses for downloaded tracks.
 *
 * Delegates all logic to [RevalidationLogic] so the business rules can be
 * unit-tested on JVM without the WorkManager runtime.
 */
@HiltWorker
class RevalidationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downloadRepository: DownloadRepository,
    private val offlineApi: TidesOfflineApi,
    private val credentialsProvider: CredentialsProvider,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val logic = RevalidationLogic(
            downloadRepository = downloadRepository,
            offlineApi = offlineApi,
            credentialsProvider = credentialsProvider,
        )
        val currentTime = System.currentTimeMillis() / 1000
        return when (logic.execute(currentTime)) {
            RevalidationResult.SUCCESS -> Result.success()
            RevalidationResult.RETRY -> Result.retry()
        }
    }
}
