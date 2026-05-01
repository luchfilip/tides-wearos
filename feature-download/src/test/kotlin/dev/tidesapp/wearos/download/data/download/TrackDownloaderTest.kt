package dev.tidesapp.wearos.download.data.download

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.data.api.dto.OfflinePlaybackInfoResponse
import dev.tidesapp.wearos.download.data.manifest.DashManifestParser
import dev.tidesapp.wearos.download.data.manifest.SegmentInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TrackDownloaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var offlineApi: TidesOfflineApi
    private lateinit var manifestParser: DashManifestParser
    private lateinit var segmentDownloader: SegmentDownloader
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var trackDownloader: TrackDownloader

    private val testPlaybackInfo = OfflinePlaybackInfoResponse(
        trackId = 12345,
        assetPresentation = "FULL",
        audioMode = "STEREO",
        audioQuality = "HIGH",
        streamingSessionId = "session-id",
        manifestMimeType = "application/dash+xml",
        manifestHash = "abc123hash",
        manifest = "BASE64_MANIFEST",
        albumReplayGain = -7.5,
        albumPeakAmplitude = 0.99,
        trackReplayGain = -6.2,
        trackPeakAmplitude = 0.97,
        offlineRevalidateAt = 1700000000L,
        offlineValidUntil = 1700100000L,
        bitDepth = 16,
        sampleRate = 44100,
    )

    private val testSegmentInfo = SegmentInfo(
        initUrl = "https://cdn.example.com/init.mp4",
        mediaUrls = listOf(
            "https://cdn.example.com/1.mp4",
            "https://cdn.example.com/2.mp4",
            "https://cdn.example.com/3.mp4",
        ),
        codec = "flac",
        bandwidth = 1001821,
        sampleRate = 44100,
        durationSeconds = 240.0,
    )

    @Before
    fun setup() {
        offlineApi = mockk()
        manifestParser = mockk()
        segmentDownloader = mockk()
        credentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns mockk {
                    every { token } returns "fake-token"
                }
            }
        }

        trackDownloader = TrackDownloader(
            offlineApi = offlineApi,
            manifestParser = manifestParser,
            segmentDownloader = segmentDownloader,
            credentialsProvider = credentialsProvider,
        )

        // Default stubs
        coEvery {
            offlineApi.getOfflinePlaybackInfo(
                token = any(),
                trackId = any(),
                audioQuality = any(),
                streamingSessionId = any(),
            )
        } returns testPlaybackInfo
        every { manifestParser.parse(any()) } returns testSegmentInfo
        coEvery { segmentDownloader.downloadSegmentToFile(any(), any(), any()) } returns Unit
    }

    @Test
    fun `downloadTrack produces correct file path`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")

        val result = trackDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "HIGH",
            outputDir = outputDir,
        )

        assertEquals(
            File(outputDir, "12345.mp4").absolutePath,
            result.filePath,
        )
    }

    @Test
    fun `downloadTrack calls API with correct params`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")

        trackDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "LOSSLESS",
            outputDir = outputDir,
        )

        coVerify {
            offlineApi.getOfflinePlaybackInfo(
                token = "Bearer fake-token",
                trackId = "12345",
                audioQuality = "LOSSLESS",
                streamingSessionId = any(),
            )
        }
    }

    @Test
    fun `downloadTrack parses manifest from API response`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")

        trackDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "HIGH",
            outputDir = outputDir,
        )

        io.mockk.verify { manifestParser.parse("BASE64_MANIFEST") }
    }

    @Test
    fun `downloadTrack downloads init segment first then media segments in order`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")
        val expectedFile = File(outputDir, "12345.mp4")

        trackDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "HIGH",
            outputDir = outputDir,
        )

        coVerifyOrder {
            segmentDownloader.downloadSegmentToFile(
                url = "https://cdn.example.com/init.mp4",
                outputFile = expectedFile,
                append = false,
            )
            segmentDownloader.downloadSegmentToFile(
                url = "https://cdn.example.com/1.mp4",
                outputFile = expectedFile,
                append = true,
            )
            segmentDownloader.downloadSegmentToFile(
                url = "https://cdn.example.com/2.mp4",
                outputFile = expectedFile,
                append = true,
            )
            segmentDownloader.downloadSegmentToFile(
                url = "https://cdn.example.com/3.mp4",
                outputFile = expectedFile,
                append = true,
            )
        }
    }

    @Test
    fun `downloadTrack reports progress correctly`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")
        val progressCalls = mutableListOf<Pair<Int, Int>>()

        trackDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "HIGH",
            outputDir = outputDir,
            onProgress = { downloaded, total -> progressCalls.add(downloaded to total) },
        )

        // init + 3 media = 4 total segments
        val expectedTotal = 4
        assertEquals(
            listOf(1 to expectedTotal, 2 to expectedTotal, 3 to expectedTotal, 4 to expectedTotal),
            progressCalls,
        )
    }

    @Test
    fun `downloadTrack returns correct result metadata`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")

        val result = trackDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "HIGH",
            outputDir = outputDir,
        )

        assertEquals("abc123hash", result.manifestHash)
        assertEquals(1700000000L, result.offlineRevalidateAt)
        assertEquals(1700100000L, result.offlineValidUntil)
        assertEquals("HIGH", result.audioQuality)
        assertEquals(16, result.bitDepth)
        assertEquals(44100, result.sampleRate)
    }

    @Test
    fun `downloadTrack deletes partial file on failure`() = runTest {
        val outputDir = tempFolder.newFolder("downloads")
        val expectedFile = File(outputDir, "12345.mp4")

        // Let init segment succeed (creates the file) but fail on second media segment
        var callCount = 0
        coEvery { segmentDownloader.downloadSegmentToFile(any(), any(), any()) } answers {
            callCount++
            if (callCount == 3) throw java.io.IOException("Network error on segment 3")
            // Create the file so we can verify deletion
            if (callCount == 1) expectedFile.createNewFile()
        }

        val exception = try {
            trackDownloader.downloadTrack(
                trackId = 12345,
                audioQuality = "HIGH",
                outputDir = outputDir,
            )
            null
        } catch (e: Exception) {
            e
        }

        assertTrue("Expected exception to be thrown", exception is java.io.IOException)
        assertFalse("Partial file should be deleted", expectedFile.exists())
    }

    @Test(expected = RuntimeException::class)
    fun `downloadTrack throws on credential failure`() = runTest {
        val failingCredentials: CredentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns null
            }
        }
        val failingDownloader = TrackDownloader(
            offlineApi = offlineApi,
            manifestParser = manifestParser,
            segmentDownloader = segmentDownloader,
            credentialsProvider = failingCredentials,
        )

        val outputDir = tempFolder.newFolder("downloads")
        failingDownloader.downloadTrack(
            trackId = 12345,
            audioQuality = "HIGH",
            outputDir = outputDir,
        )
    }
}
