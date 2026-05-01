package dev.tidesapp.wearos.download.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dev.tidesapp.wearos.core.domain.model.AudioQualityPreference
import dev.tidesapp.wearos.download.data.download.TrackDownloadResult
import dev.tidesapp.wearos.download.data.download.TrackDownloader
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack
import dev.tidesapp.wearos.download.domain.model.OfflineRegistration
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository
import dev.tidesapp.wearos.download.domain.repository.OfflineRegistrationRepository
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class DownloadWorkerTest {

    private lateinit var context: Context
    private lateinit var params: WorkerParameters
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var offlineRegistration: OfflineRegistrationRepository
    private lateinit var trackDownloader: TrackDownloader
    private lateinit var storageRepository: DownloadStorageRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        params = mockk(relaxed = true)
        downloadRepository = mockk(relaxed = true)
        offlineRegistration = mockk(relaxed = true)
        trackDownloader = mockk(relaxed = true)
        storageRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        // Default stubs
        coEvery { offlineRegistration.ensureOfflineAuthorized() } returns
            Result.success(OfflineRegistration(1L, true, 100L))
        coEvery { offlineRegistration.registerCollectionOffline(any(), any()) } returns
            Result.success(Unit)
        every { settingsRepository.getDownloadQuality() } returns
            flowOf(AudioQualityPreference.HIGH)
        coEvery { storageRepository.getDownloadDirectory() } returns File("/tmp/downloads")
        coEvery { storageRepository.hasSpaceForTrack(any()) } returns true
    }

    private fun createWorker(
        collectionId: String? = "album-123",
        collectionType: String? = "ALBUM",
    ): DownloadWorker {
        val inputData = androidx.work.Data.Builder()
        if (collectionId != null) {
            inputData.putString(DownloadWorker.KEY_COLLECTION_ID, collectionId)
        }
        if (collectionType != null) {
            inputData.putString(DownloadWorker.KEY_COLLECTION_TYPE, collectionType)
        }
        every { params.inputData } returns inputData.build()

        return DownloadWorker(
            context = context,
            params = params,
            downloadRepository = downloadRepository,
            offlineRegistration = offlineRegistration,
            trackDownloader = trackDownloader,
            storageRepository = storageRepository,
            settingsRepository = settingsRepository,
        )
    }

    private fun pendingTrack(trackId: Long) = DownloadedTrack(
        trackId = trackId,
        title = "Track $trackId",
        artistName = "Artist",
        albumTitle = "Album",
        imageUrl = "https://example.com/img.jpg",
        duration = 200,
        trackNumber = 1,
        filePath = "",
        fileSize = 0L,
        audioQuality = "",
        manifestHash = "",
        offlineRevalidateAt = 0L,
        offlineValidUntil = 0L,
        downloadedAt = 0L,
        state = DownloadState.PENDING,
        collectionId = "album-123",
        collectionType = CollectionType.ALBUM,
    )

    private fun downloadResult(trackId: Long) = TrackDownloadResult(
        filePath = "/tmp/downloads/$trackId.mp4",
        fileSize = 10_000_000L,
        manifestHash = "hash-$trackId",
        offlineRevalidateAt = 1_000_000L,
        offlineValidUntil = 2_000_000L,
        audioQuality = "HIGH",
        bitDepth = 16,
        sampleRate = 44100,
    )

    // ── Success cases ──────────────────────────────────────────────────

    @Test
    fun `doWork downloads all pending tracks and marks completed`() = runTest {
        val tracks = listOf(pendingTrack(1L), pendingTrack(2L), pendingTrack(3L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks

        tracks.forEach { track ->
            coEvery {
                trackDownloader.downloadTrack(
                    trackId = track.trackId,
                    audioQuality = "HIGH",
                    outputDir = File("/tmp/downloads"),
                    onProgress = any(),
                )
            } returns downloadResult(track.trackId)
        }

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { offlineRegistration.ensureOfflineAuthorized() }
        coVerify(exactly = 1) {
            offlineRegistration.registerCollectionOffline("album-123", CollectionType.ALBUM)
        }

        tracks.forEach { track ->
            coVerify(exactly = 1) {
                trackDownloader.downloadTrack(
                    trackId = track.trackId,
                    audioQuality = "HIGH",
                    outputDir = File("/tmp/downloads"),
                    onProgress = any(),
                )
            }
            coVerify(exactly = 1) {
                downloadRepository.markTrackCompleted(track.trackId, downloadResult(track.trackId))
            }
        }
    }

    @Test
    fun `doWork uses download quality from settings`() = runTest {
        every { settingsRepository.getDownloadQuality() } returns
            flowOf(AudioQualityPreference.LOSSLESS)

        val tracks = listOf(pendingTrack(1L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks
        coEvery {
            trackDownloader.downloadTrack(
                trackId = 1L,
                audioQuality = "LOSSLESS",
                outputDir = any(),
                onProgress = any(),
            )
        } returns downloadResult(1L)

        val worker = createWorker()
        worker.doWork()

        coVerify {
            trackDownloader.downloadTrack(
                trackId = 1L,
                audioQuality = "LOSSLESS",
                outputDir = any(),
                onProgress = any(),
            )
        }
    }

    // ── Stream mode fallback cases ───────────────────────────────────

    @Test
    fun `doWork falls back to stream mode when offline authorization fails`() = runTest {
        coEvery { offlineRegistration.ensureOfflineAuthorized() } returns
            Result.failure(RuntimeException("Unauthorized"))

        val tracks = listOf(pendingTrack(1L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks
        coEvery {
            trackDownloader.downloadTrack(
                trackId = 1L,
                audioQuality = "HIGH",
                outputDir = any(),
                useStreamMode = true,
                onProgress = any(),
            )
        } returns downloadResult(1L)

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify {
            trackDownloader.downloadTrack(
                trackId = 1L,
                audioQuality = "HIGH",
                outputDir = any(),
                useStreamMode = true,
                onProgress = any(),
            )
        }
    }

    @Test
    fun `doWork falls back to stream mode when collection registration fails`() = runTest {
        coEvery { offlineRegistration.registerCollectionOffline(any(), any()) } returns
            Result.failure(RuntimeException("Registration failed"))

        val tracks = listOf(pendingTrack(1L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks
        coEvery {
            trackDownloader.downloadTrack(
                trackId = 1L,
                audioQuality = "HIGH",
                outputDir = any(),
                useStreamMode = true,
                onProgress = any(),
            )
        } returns downloadResult(1L)

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify {
            trackDownloader.downloadTrack(
                trackId = 1L,
                audioQuality = "HIGH",
                outputDir = any(),
                useStreamMode = true,
                onProgress = any(),
            )
        }
    }

    // ── Failure cases ──────────────────────────────────────────────────

    @Test
    fun `doWork returns failure for missing collection ID`() = runTest {
        val worker = createWorker(collectionId = null)
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork returns failure for missing collection type`() = runTest {
        val worker = createWorker(collectionType = null)
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    // ── Partial failure cases ──────────────────────────────────────────

    @Test
    fun `doWork marks track failed when download throws and continues`() = runTest {
        val tracks = listOf(pendingTrack(1L), pendingTrack(2L), pendingTrack(3L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks

        coEvery {
            trackDownloader.downloadTrack(trackId = 1L, any(), any(), any())
        } returns downloadResult(1L)

        coEvery {
            trackDownloader.downloadTrack(trackId = 2L, any(), any(), any())
        } throws RuntimeException("Network error")

        coEvery {
            trackDownloader.downloadTrack(trackId = 3L, any(), any(), any())
        } returns downloadResult(3L)

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        coVerify { downloadRepository.markTrackCompleted(1L, downloadResult(1L)) }
        coVerify { downloadRepository.markTrackFailed(2L, "Network error") }
        coVerify { downloadRepository.markTrackCompleted(3L, downloadResult(3L)) }
    }

    @Test
    fun `doWork marks track failed with fallback message on null error`() = runTest {
        val tracks = listOf(pendingTrack(1L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks

        coEvery {
            trackDownloader.downloadTrack(trackId = 1L, any(), any(), any())
        } throws RuntimeException()

        val worker = createWorker()
        worker.doWork()

        coVerify { downloadRepository.markTrackFailed(1L, "Download failed") }
    }

    // ── Storage cases ──────────────────────────────────────────────────

    @Test
    fun `doWork skips track when storage insufficient`() = runTest {
        val tracks = listOf(pendingTrack(1L), pendingTrack(2L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks

        // First track: has space; second track: no space
        coEvery { storageRepository.hasSpaceForTrack(any()) } returnsMany listOf(true, false)

        coEvery {
            trackDownloader.downloadTrack(trackId = 1L, any(), any(), any())
        } returns downloadResult(1L)

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        coVerify { downloadRepository.markTrackCompleted(1L, downloadResult(1L)) }
        coVerify { downloadRepository.markTrackFailed(2L, "Insufficient storage") }
        coVerify(exactly = 0) {
            trackDownloader.downloadTrack(trackId = 2L, any(), any(), any())
        }
    }

    // ── Edge cases ─────────────────────────────────────────────────────

    @Test
    fun `doWork succeeds with empty pending tracks list`() = runTest {
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns emptyList()

        val worker = createWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 0) { trackDownloader.downloadTrack(any(), any(), any(), any()) }
    }

    @Test
    fun `doWork processes tracks in order`() = runTest {
        val tracks = listOf(pendingTrack(10L), pendingTrack(20L), pendingTrack(30L))
        coEvery { downloadRepository.getPendingTracksForCollection("album-123") } returns tracks

        tracks.forEach { track ->
            coEvery {
                trackDownloader.downloadTrack(trackId = track.trackId, any(), any(), any())
            } returns downloadResult(track.trackId)
        }

        val worker = createWorker()
        worker.doWork()

        coVerifyOrder {
            trackDownloader.downloadTrack(trackId = 10L, any(), any(), any())
            trackDownloader.downloadTrack(trackId = 20L, any(), any(), any())
            trackDownloader.downloadTrack(trackId = 30L, any(), any(), any())
        }
    }
}
