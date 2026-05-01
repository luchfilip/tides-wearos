package dev.tidesapp.wearos.download.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DownloadWorkSchedulerTest {

    private lateinit var workManager: WorkManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var scheduler: DownloadWorkScheduler

    @Before
    fun setUp() {
        workManager = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        scheduler = DownloadWorkScheduler(workManager, settingsRepository)
    }

    @Test
    fun `enqueueCollectionDownload enqueues unique work with correct name`() = runTest {
        every { settingsRepository.isWifiOnly() } returns flowOf(false)

        scheduler.enqueueCollectionDownload("album-123", CollectionType.ALBUM)

        val nameSlot = slot<String>()
        verify {
            workManager.enqueueUniqueWork(
                capture(nameSlot),
                eq(ExistingWorkPolicy.KEEP),
                any<OneTimeWorkRequest>(),
            )
        }
        assertEquals("download_album-123", nameSlot.captured)
    }

    @Test
    fun `enqueueCollectionDownload uses UNMETERED constraint when wifiOnly`() = runTest {
        every { settingsRepository.isWifiOnly() } returns flowOf(true)

        scheduler.enqueueCollectionDownload("album-123", CollectionType.ALBUM)

        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(any(), any(), capture(requestSlot))
        }

        val constraints = requestSlot.captured.workSpec.constraints
        assertEquals(NetworkType.UNMETERED, constraints.requiredNetworkType)
    }

    @Test
    fun `enqueueCollectionDownload uses CONNECTED constraint when not wifiOnly`() = runTest {
        every { settingsRepository.isWifiOnly() } returns flowOf(false)

        scheduler.enqueueCollectionDownload("playlist-456", CollectionType.PLAYLIST)

        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(any(), any(), capture(requestSlot))
        }

        val constraints = requestSlot.captured.workSpec.constraints
        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
    }

    @Test
    fun `enqueueCollectionDownload sets correct input data`() = runTest {
        every { settingsRepository.isWifiOnly() } returns flowOf(false)

        scheduler.enqueueCollectionDownload("playlist-456", CollectionType.PLAYLIST)

        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(any(), any(), capture(requestSlot))
        }

        val inputData = requestSlot.captured.workSpec.input
        assertEquals("playlist-456", inputData.getString(DownloadWorker.KEY_COLLECTION_ID))
        assertEquals("PLAYLIST", inputData.getString(DownloadWorker.KEY_COLLECTION_TYPE))
    }

    @Test
    fun `enqueueCollectionDownload tags work with download tag`() = runTest {
        every { settingsRepository.isWifiOnly() } returns flowOf(false)

        scheduler.enqueueCollectionDownload("album-123", CollectionType.ALBUM)

        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(any(), any(), capture(requestSlot))
        }

        assertTrue(requestSlot.captured.tags.contains(DownloadWorkScheduler.TAG_DOWNLOAD))
    }

    @Test
    fun `cancelCollectionDownload cancels unique work with correct name`() {
        scheduler.cancelCollectionDownload("album-123")

        verify { workManager.cancelUniqueWork("download_album-123") }
    }

    @Test
    fun `cancelAll cancels all work by download tag`() {
        scheduler.cancelAll()

        verify { workManager.cancelAllWorkByTag(DownloadWorkScheduler.TAG_DOWNLOAD) }
    }
}
