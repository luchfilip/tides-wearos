package dev.tidesapp.wearos.download.data.worker

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.data.api.dto.OfflinePlaybackInfoResponse
import dev.tidesapp.wearos.download.data.download.TrackDownloadResult
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the revalidation logic that [RevalidationWorker] performs.
 *
 * Since [CoroutineWorker] is tightly coupled to Android's WorkManager runtime,
 * we test the core logic via [RevalidationLogic] — a plain-Kotlin extraction
 * that the worker delegates to. This mirrors the TrackResolver pattern:
 * extract testable logic, test on JVM, keep the worker itself thin.
 */
class RevalidationWorkerTest {

    private lateinit var downloadRepository: DownloadRepository
    private lateinit var offlineApi: TidesOfflineApi
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var revalidationLogic: RevalidationLogic

    private val currentTime = 1700000000L

    @Before
    fun setup() {
        downloadRepository = mockk(relaxUnitFun = true)
        offlineApi = mockk()
        credentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns mockk {
                    every { token } returns "fake-token"
                }
            }
        }
        revalidationLogic = RevalidationLogic(
            downloadRepository = downloadRepository,
            offlineApi = offlineApi,
            credentialsProvider = credentialsProvider,
        )

        // Defaults
        coEvery { downloadRepository.getExpiredTracks(any()) } returns emptyList()
        coEvery { downloadRepository.getTracksNeedingRevalidation(any()) } returns emptyList()
    }

    @Test
    fun `expired tracks are marked failed`() = runTest {
        val track1 = makeTrack(trackId = 1, offlineValidUntil = currentTime - 100)
        val track2 = makeTrack(trackId = 2, offlineValidUntil = currentTime - 200)
        coEvery { downloadRepository.getExpiredTracks(currentTime) } returns listOf(track1, track2)

        val result = revalidationLogic.execute(currentTime)

        assertEquals(RevalidationResult.SUCCESS, result)
        coVerify(exactly = 1) {
            downloadRepository.markTrackFailed(1L, "Offline license expired")
        }
        coVerify(exactly = 1) {
            downloadRepository.markTrackFailed(2L, "Offline license expired")
        }
    }

    @Test
    fun `tracks needing revalidation with matching hash get updated timestamps`() = runTest {
        val track = makeTrack(
            trackId = 42,
            manifestHash = "abc123",
            offlineRevalidateAt = currentTime - 10,
        )
        coEvery { downloadRepository.getTracksNeedingRevalidation(currentTime) } returns listOf(track)
        coEvery {
            offlineApi.getOfflinePlaybackInfo(
                token = any(),
                trackId = "42",
                audioQuality = "HIGH",
                streamingSessionId = any(),
            )
        } returns OfflinePlaybackInfoResponse(
            trackId = 42,
            manifestHash = "abc123", // same hash
            offlineRevalidateAt = currentTime + 86400,
            offlineValidUntil = currentTime + 172800,
            bitDepth = 16,
            sampleRate = 44100,
        )

        val result = revalidationLogic.execute(currentTime)

        assertEquals(RevalidationResult.SUCCESS, result)
        coVerify(exactly = 1) {
            downloadRepository.markTrackCompleted(
                42L,
                match<TrackDownloadResult> {
                    it.offlineRevalidateAt == currentTime + 86400 &&
                        it.offlineValidUntil == currentTime + 172800 &&
                        it.manifestHash == "abc123" &&
                        it.filePath == "/data/downloads/42.mp4"
                },
            )
        }
    }

    @Test
    fun `tracks needing revalidation with different hash get marked for re-download`() = runTest {
        val track = makeTrack(
            trackId = 42,
            manifestHash = "abc123",
            offlineRevalidateAt = currentTime - 10,
        )
        coEvery { downloadRepository.getTracksNeedingRevalidation(currentTime) } returns listOf(track)
        coEvery {
            offlineApi.getOfflinePlaybackInfo(
                token = any(),
                trackId = "42",
                audioQuality = "HIGH",
                streamingSessionId = any(),
            )
        } returns OfflinePlaybackInfoResponse(
            trackId = 42,
            manifestHash = "different-hash", // hash changed
            offlineRevalidateAt = currentTime + 86400,
            offlineValidUntil = currentTime + 172800,
        )

        val result = revalidationLogic.execute(currentTime)

        assertEquals(RevalidationResult.SUCCESS, result)
        coVerify(exactly = 1) {
            downloadRepository.markTrackFailed(42L, "Content changed, re-download needed")
        }
        coVerify(exactly = 0) { downloadRepository.markTrackCompleted(any(), any()) }
    }

    @Test
    fun `network errors during revalidation are silently skipped`() = runTest {
        val track = makeTrack(trackId = 42, offlineRevalidateAt = currentTime - 10)
        coEvery { downloadRepository.getTracksNeedingRevalidation(currentTime) } returns listOf(track)
        coEvery {
            offlineApi.getOfflinePlaybackInfo(
                token = any(),
                trackId = any(),
                audioQuality = any(),
                streamingSessionId = any(),
            )
        } throws java.io.IOException("Network unavailable")

        val result = revalidationLogic.execute(currentTime)

        assertEquals(RevalidationResult.SUCCESS, result)
        coVerify(exactly = 0) { downloadRepository.markTrackFailed(42L, any()) }
        coVerify(exactly = 0) { downloadRepository.markTrackCompleted(any(), any()) }
    }

    @Test
    fun `returns RETRY when credentials fail`() = runTest {
        val failingCredentials: CredentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns null
            }
        }
        val logic = RevalidationLogic(
            downloadRepository = downloadRepository,
            offlineApi = offlineApi,
            credentialsProvider = failingCredentials,
        )
        val track = makeTrack(trackId = 42, offlineRevalidateAt = currentTime - 10)
        coEvery { downloadRepository.getTracksNeedingRevalidation(currentTime) } returns listOf(track)

        val result = logic.execute(currentTime)

        assertEquals(RevalidationResult.RETRY, result)
    }

    private fun makeTrack(
        trackId: Long,
        manifestHash: String = "hash",
        offlineValidUntil: Long = 0,
        offlineRevalidateAt: Long = 0,
    ) = DownloadedTrack(
        trackId = trackId,
        title = "Track $trackId",
        artistName = "Artist",
        albumTitle = "Album",
        imageUrl = "",
        duration = 200,
        trackNumber = 1,
        filePath = "/data/downloads/$trackId.mp4",
        fileSize = 1024,
        audioQuality = "HIGH",
        manifestHash = manifestHash,
        offlineRevalidateAt = offlineRevalidateAt,
        offlineValidUntil = offlineValidUntil,
        downloadedAt = currentTime - 1000,
        state = DownloadState.COMPLETED,
        collectionId = "collection-1",
        collectionType = CollectionType.ALBUM,
    )
}
