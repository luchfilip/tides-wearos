package dev.tidesapp.wearos.download.data.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Downloads individual DASH segments from CDN URLs to local disk.
 *
 * Streams bytes directly to [FileOutputStream] with an 8 KB buffer
 * so even large segments never reside fully in memory.
 */
class SegmentDownloader @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {

    /**
     * Downloads a segment from [url] and writes it to [outputFile].
     *
     * @param url        CDN URL of the segment (init or media).
     * @param outputFile Target file on disk.
     * @param append     If `true`, appends to the file; if `false`, overwrites it.
     * @throws IOException on HTTP errors or empty response bodies.
     */
    suspend fun downloadSegmentToFile(url: String, outputFile: File, append: Boolean) {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .header("Accept-Encoding", "identity")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Segment download failed: HTTP ${response.code}")
            }

            response.body.byteStream().use { input ->
                FileOutputStream(outputFile, append).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            }
        }
    }
}
