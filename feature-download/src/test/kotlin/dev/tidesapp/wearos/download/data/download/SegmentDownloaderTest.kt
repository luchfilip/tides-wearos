package dev.tidesapp.wearos.download.data.download

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class SegmentDownloaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockClient: OkHttpClient
    private lateinit var downloader: SegmentDownloader

    @Before
    fun setup() {
        mockClient = mockk()
        downloader = SegmentDownloader(mockClient)
    }

    @Test
    fun `downloadSegmentToFile writes bytes to new file`() = runTest {
        val testData = "test audio segment data".toByteArray()
        stubOkHttpResponse(testData, 200)

        val outputFile = tempFolder.newFile("segment.mp4")
        downloader.downloadSegmentToFile(
            url = "https://cdn.example.com/0.mp4",
            outputFile = outputFile,
            append = false,
        )

        assertArrayEquals(testData, outputFile.readBytes())
    }

    @Test
    fun `downloadSegmentToFile appends to existing file`() = runTest {
        val existingData = "INIT_SEGMENT".toByteArray()
        val newData = "MEDIA_SEGMENT".toByteArray()

        val outputFile = tempFolder.newFile("track.mp4")
        outputFile.writeBytes(existingData)

        stubOkHttpResponse(newData, 200)

        downloader.downloadSegmentToFile(
            url = "https://cdn.example.com/1.mp4",
            outputFile = outputFile,
            append = true,
        )

        val expected = existingData + newData
        assertArrayEquals(expected, outputFile.readBytes())
    }

    @Test
    fun `downloadSegmentToFile overwrites when append is false`() = runTest {
        val existingData = "OLD_DATA".toByteArray()
        val newData = "NEW_DATA".toByteArray()

        val outputFile = tempFolder.newFile("track.mp4")
        outputFile.writeBytes(existingData)

        stubOkHttpResponse(newData, 200)

        downloader.downloadSegmentToFile(
            url = "https://cdn.example.com/0.mp4",
            outputFile = outputFile,
            append = false,
        )

        assertArrayEquals(newData, outputFile.readBytes())
    }

    @Test(expected = IOException::class)
    fun `downloadSegmentToFile throws on HTTP error`() = runTest {
        stubOkHttpResponse(byteArrayOf(), 403)

        val outputFile = tempFolder.newFile("segment.mp4")
        downloader.downloadSegmentToFile(
            url = "https://cdn.example.com/forbidden.mp4",
            outputFile = outputFile,
            append = false,
        )
    }

    @Test
    fun `downloadSegmentToFile streams large data without OOM`() = runTest {
        // 1 MB of data to verify streaming behavior
        val largeData = ByteArray(1_000_000) { (it % 256).toByte() }
        stubOkHttpResponse(largeData, 200)

        val outputFile = tempFolder.newFile("large.mp4")
        downloader.downloadSegmentToFile(
            url = "https://cdn.example.com/large.mp4",
            outputFile = outputFile,
            append = false,
        )

        assertEquals(largeData.size.toLong(), outputFile.length())
        assertArrayEquals(largeData, outputFile.readBytes())
    }

    private fun stubOkHttpResponse(data: ByteArray, code: Int) {
        val mockCall = mockk<okhttp3.Call>()
        every { mockClient.newCall(any()) } returns mockCall

        val responseBody = data.toResponseBody("audio/mp4".toMediaType())
        val response = Response.Builder()
            .request(Request.Builder().url("https://cdn.example.com/0.mp4").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "Forbidden")
            .body(responseBody)
            .build()
        every { mockCall.execute() } returns response
    }
}
