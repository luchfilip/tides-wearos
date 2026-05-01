package dev.tidesapp.wearos.player.service

import android.net.Uri
import android.util.Base64
import androidx.media3.common.MediaItem
import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.player.data.api.TidesPlaybackApi
import dev.tidesapp.wearos.player.data.dto.PlaybackInfoResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Unit tests for [TrackResolver] — the plain-Kotlin extraction of the
 * stub -> playable-MediaItem conversion that used to live inline in
 * [TrackResolvingCallback]. Tests run on JVM with `isReturnDefaultValues`
 * enabled so Media3's `MediaItem.Builder` works without an Android runtime.
 */
class TrackResolverTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var api: TidesPlaybackApi
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var resolver: TrackResolver

    @Before
    fun setup() {
        api = mockk()
        downloadRepository = mockk()
        credentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns mockk {
                    every { token } returns "fake-jwt-token"
                }
            }
        }
        resolver = TrackResolver(
            playbackApi = api,
            credentialsProvider = credentialsProvider,
            downloadRepository = downloadRepository,
        )

        // android.util.Base64 is not in the JVM runtime; stub it so BTS path
        // decodes deterministically. DASH tests don't hit this.
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any<Int>()) } answers {
            // Echo input bytes — the BTS test will pass a pre-constructed
            // JSON string so the round-trip lands on real JSON.
            firstArg<String>().toByteArray(Charsets.UTF_8)
        }

        // android.net.Uri.parse returns null under isReturnDefaultValues,
        // which makes MediaItem.Builder drop the localConfiguration. Mock it
        // to return a Uri whose toString() echoes the input — that's all
        // MediaItem.LocalConfiguration cares about for these assertions.
        mockkStatic(Uri::class)
        val uriSlot = slot<String>()
        every { Uri.parse(capture(uriSlot)) } answers {
            val captured = uriSlot.captured
            mockk<Uri>(relaxed = true).also { every { it.toString() } returns captured }
        }

        // Uri.fromFile for local file resolution tests.
        every { Uri.fromFile(any()) } answers {
            val file = firstArg<java.io.File>()
            val fileUri = "file://${file.absolutePath}"
            mockk<Uri>(relaxed = true).also { every { it.toString() } returns fileUri }
        }

        // Default: no tracks downloaded
        coEvery { downloadRepository.getDownloadedTrack(any()) } returns null
    }

    @After
    fun tearDown() {
        unmockkStatic(Base64::class)
        unmockkStatic(Uri::class)
    }

    @Test
    fun `resolve DASH track sets data URI and dash mime on builder`() = runTest {
        val stub = MediaItem.Builder().setMediaId("track-42").build()
        coEvery { api.getTrackPlaybackInfo(any(), eq("track-42")) } returns
            PlaybackInfoResponse(
                trackId = 42L,
                manifestMimeType = "application/dash+xml",
                manifest = "BASE64MPD==",
            )

        val resolved = resolver.resolve(stub)

        assertEquals("track-42", resolved.mediaId)
        assertNotNull(resolved.localConfiguration)
        assertEquals(
            "data:application/dash+xml;base64,BASE64MPD==",
            resolved.localConfiguration!!.uri.toString(),
        )
        assertEquals("application/dash+xml", resolved.localConfiguration!!.mimeType)
    }

    @Test
    fun `resolve BTS track picks first URL from decoded JSON manifest`() = runTest {
        val stub = MediaItem.Builder().setMediaId("track-7").build()
        // The stubbed Base64.decode echoes bytes back, so manifest already
        // holds the plain JSON string.
        val btsJson = """{"mimeType":"audio/mp4","codecs":"mp4a","urls":["https://cdn.tidal/audio.m4a"]}"""
        coEvery { api.getTrackPlaybackInfo(any(), eq("track-7")) } returns
            PlaybackInfoResponse(
                trackId = 7L,
                manifestMimeType = "application/vnd.tidal.bts",
                manifest = btsJson,
            )

        val resolved = resolver.resolve(stub)

        assertNotNull(resolved.localConfiguration)
        assertEquals(
            "https://cdn.tidal/audio.m4a",
            resolved.localConfiguration!!.uri.toString(),
        )
    }

    @Test
    fun `resolve returns untouched stub when mediaId is blank`() = runTest {
        val stub = MediaItem.Builder().setMediaId("").build()

        val resolved = resolver.resolve(stub)

        assertEquals(stub, resolved)
        assertNull(resolved.localConfiguration)
    }

    @Test(expected = RuntimeException::class)
    fun `resolve propagates API exceptions for caller to handle`() = runTest {
        val stub = MediaItem.Builder().setMediaId("track-boom").build()
        coEvery { api.getTrackPlaybackInfo(any(), eq("track-boom")) } throws
            RuntimeException("401 Unauthorized")

        resolver.resolve(stub)
    }

    @Test(expected = RuntimeException::class)
    fun `resolve throws when credentials provider returns no success data`() = runTest {
        val failingCredentials: CredentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns null
            }
        }
        val failingResolver = TrackResolver(
            playbackApi = api,
            credentialsProvider = failingCredentials,
        )
        val stub = MediaItem.Builder().setMediaId("track-99").build()

        failingResolver.resolve(stub)
    }

    // --- D12: Offline playback integration tests ---

    @Test
    fun `resolve returns local file URI when track is downloaded`() = runTest {
        val audioFile = tempFolder.newFile("42.mp4")
        val downloaded = makeDownloadedTrack(
            trackId = 42,
            filePath = audioFile.absolutePath,
            state = DownloadState.COMPLETED,
            offlineValidUntil = (System.currentTimeMillis() / 1000) + 3600, // 1h from now
        )
        coEvery { downloadRepository.getDownloadedTrack(42L) } returns downloaded

        val stub = MediaItem.Builder().setMediaId("42").build()
        val resolved = resolver.resolve(stub)

        assertNotNull(resolved.localConfiguration)
        assertTrue(
            "Expected file:// URI but got: ${resolved.localConfiguration!!.uri}",
            resolved.localConfiguration!!.uri.toString().startsWith("file://"),
        )
        // API should NOT be called when local file is used
        coVerify(exactly = 0) { api.getTrackPlaybackInfo(any(), any()) }
    }

    @Test
    fun `resolve falls back to streaming when track not downloaded`() = runTest {
        coEvery { downloadRepository.getDownloadedTrack(7L) } returns null

        val btsJson = """{"mimeType":"audio/mp4","codecs":"mp4a","urls":["https://cdn.tidal/audio.m4a"]}"""
        coEvery { api.getTrackPlaybackInfo(any(), eq("7")) } returns
            PlaybackInfoResponse(
                trackId = 7L,
                manifestMimeType = "application/vnd.tidal.bts",
                manifest = btsJson,
            )

        val stub = MediaItem.Builder().setMediaId("7").build()
        val resolved = resolver.resolve(stub)

        assertEquals(
            "https://cdn.tidal/audio.m4a",
            resolved.localConfiguration!!.uri.toString(),
        )
        coVerify(exactly = 1) { api.getTrackPlaybackInfo(any(), eq("7")) }
    }

    @Test
    fun `resolve falls back to streaming when downloaded file missing`() = runTest {
        val downloaded = makeDownloadedTrack(
            trackId = 42,
            filePath = "/nonexistent/path/42.mp4",
            state = DownloadState.COMPLETED,
            offlineValidUntil = (System.currentTimeMillis() / 1000) + 3600,
        )
        coEvery { downloadRepository.getDownloadedTrack(42L) } returns downloaded

        val btsJson = """{"mimeType":"audio/mp4","codecs":"mp4a","urls":["https://cdn.tidal/audio.m4a"]}"""
        coEvery { api.getTrackPlaybackInfo(any(), eq("42")) } returns
            PlaybackInfoResponse(
                trackId = 42L,
                manifestMimeType = "application/vnd.tidal.bts",
                manifest = btsJson,
            )

        val stub = MediaItem.Builder().setMediaId("42").build()
        val resolved = resolver.resolve(stub)

        assertEquals(
            "https://cdn.tidal/audio.m4a",
            resolved.localConfiguration!!.uri.toString(),
        )
        coVerify(exactly = 1) { api.getTrackPlaybackInfo(any(), eq("42")) }
    }

    @Test
    fun `resolve falls back to streaming when track expired`() = runTest {
        val audioFile = tempFolder.newFile("42.mp4")
        val downloaded = makeDownloadedTrack(
            trackId = 42,
            filePath = audioFile.absolutePath,
            state = DownloadState.COMPLETED,
            offlineValidUntil = (System.currentTimeMillis() / 1000) - 3600, // expired 1h ago
        )
        coEvery { downloadRepository.getDownloadedTrack(42L) } returns downloaded

        val btsJson = """{"mimeType":"audio/mp4","codecs":"mp4a","urls":["https://cdn.tidal/audio.m4a"]}"""
        coEvery { api.getTrackPlaybackInfo(any(), eq("42")) } returns
            PlaybackInfoResponse(
                trackId = 42L,
                manifestMimeType = "application/vnd.tidal.bts",
                manifest = btsJson,
            )

        val stub = MediaItem.Builder().setMediaId("42").build()
        val resolved = resolver.resolve(stub)

        assertEquals(
            "https://cdn.tidal/audio.m4a",
            resolved.localConfiguration!!.uri.toString(),
        )
        coVerify(exactly = 1) { api.getTrackPlaybackInfo(any(), eq("42")) }
    }

    @Test
    fun `resolve falls back to streaming when downloadRepository is null`() = runTest {
        val resolverWithoutDownloads = TrackResolver(
            playbackApi = api,
            credentialsProvider = credentialsProvider,
        )

        val btsJson = """{"mimeType":"audio/mp4","codecs":"mp4a","urls":["https://cdn.tidal/audio.m4a"]}"""
        coEvery { api.getTrackPlaybackInfo(any(), eq("42")) } returns
            PlaybackInfoResponse(
                trackId = 42L,
                manifestMimeType = "application/vnd.tidal.bts",
                manifest = btsJson,
            )

        val stub = MediaItem.Builder().setMediaId("42").build()
        val resolved = resolverWithoutDownloads.resolve(stub)

        assertEquals(
            "https://cdn.tidal/audio.m4a",
            resolved.localConfiguration!!.uri.toString(),
        )
        coVerify(exactly = 1) { api.getTrackPlaybackInfo(any(), eq("42")) }
    }

    @Test
    fun `resolve works with mixed queue local and remote`() = runTest {
        // Track 10: downloaded locally
        val audioFile = tempFolder.newFile("10.mp4")
        coEvery { downloadRepository.getDownloadedTrack(10L) } returns makeDownloadedTrack(
            trackId = 10,
            filePath = audioFile.absolutePath,
            state = DownloadState.COMPLETED,
            offlineValidUntil = (System.currentTimeMillis() / 1000) + 3600,
        )

        // Track 20: not downloaded, needs streaming
        coEvery { downloadRepository.getDownloadedTrack(20L) } returns null
        val btsJson = """{"mimeType":"audio/mp4","codecs":"mp4a","urls":["https://cdn.tidal/20.m4a"]}"""
        coEvery { api.getTrackPlaybackInfo(any(), eq("20")) } returns
            PlaybackInfoResponse(
                trackId = 20L,
                manifestMimeType = "application/vnd.tidal.bts",
                manifest = btsJson,
            )

        val stub10 = MediaItem.Builder().setMediaId("10").build()
        val stub20 = MediaItem.Builder().setMediaId("20").build()

        val resolved10 = resolver.resolve(stub10)
        val resolved20 = resolver.resolve(stub20)

        assertTrue(resolved10.localConfiguration!!.uri.toString().startsWith("file://"))
        assertEquals("https://cdn.tidal/20.m4a", resolved20.localConfiguration!!.uri.toString())

        // Only track 20 should hit the API
        coVerify(exactly = 0) { api.getTrackPlaybackInfo(any(), eq("10")) }
        coVerify(exactly = 1) { api.getTrackPlaybackInfo(any(), eq("20")) }
    }

    private fun makeDownloadedTrack(
        trackId: Long,
        filePath: String = "",
        state: DownloadState = DownloadState.COMPLETED,
        offlineValidUntil: Long = 0,
    ) = DownloadedTrack(
        trackId = trackId,
        title = "Test Track",
        artistName = "Test Artist",
        albumTitle = "Test Album",
        imageUrl = "",
        duration = 200,
        trackNumber = 1,
        filePath = filePath,
        fileSize = 1024,
        audioQuality = "HIGH",
        manifestHash = "hash123",
        offlineRevalidateAt = 0,
        offlineValidUntil = offlineValidUntil,
        downloadedAt = System.currentTimeMillis() / 1000,
        state = state,
        collectionId = "collection-1",
        collectionType = dev.tidesapp.wearos.download.domain.model.CollectionType.ALBUM,
    )
}
