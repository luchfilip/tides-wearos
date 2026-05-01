package dev.tidesapp.wearos.download.data.download

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.data.manifest.DashManifestParser
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Orchestrates a complete track download:
 *
 * 1. Obtains an OAuth bearer token via [CredentialsProvider].
 * 2. Fetches offline playback info (manifest + license metadata) from the API.
 * 3. Parses the DASH MPD manifest to extract segment URLs.
 * 4. Downloads the init segment, then every media segment, appending each
 *    to a single `.mp4` file on disk.
 * 5. Reports progress after each segment via [onProgress].
 *
 * If anything fails mid-download the partial file is deleted so the
 * caller never sees an incomplete artifact.
 */
class TrackDownloader @Inject constructor(
    private val offlineApi: TidesOfflineApi,
    private val manifestParser: DashManifestParser,
    private val segmentDownloader: SegmentDownloader,
    private val credentialsProvider: CredentialsProvider,
) {

    /**
     * Downloads a complete track to [outputDir]/`{trackId}.mp4`.
     *
     * @param trackId      Tidal track identifier.
     * @param audioQuality Desired quality (`LOW`, `HIGH`, `LOSSLESS`, `HI_RES`).
     * @param outputDir    Directory where the output file will be created.
     * @param onProgress   Callback invoked after each segment with
     *                     `(completedSegments, totalSegments)`.
     * @return Metadata about the downloaded file and its offline license.
     * @throws RuntimeException if credentials cannot be obtained.
     * @throws java.io.IOException on network or disk errors.
     */
    suspend fun downloadTrack(
        trackId: Long,
        audioQuality: String,
        outputDir: File,
        useStreamMode: Boolean = false,
        onProgress: (downloaded: Int, total: Int) -> Unit = { _, _ -> },
    ): TrackDownloadResult {
        val token = getBearerToken()
        val sessionId = UUID.randomUUID().toString()

        val playbackInfo = offlineApi.getOfflinePlaybackInfo(
            token = token,
            trackId = trackId.toString(),
            audioQuality = audioQuality,
            playbackMode = if (useStreamMode) "STREAM" else "OFFLINE",
            streamingSessionId = sessionId,
        )

        val segmentInfo = manifestParser.parse(playbackInfo.manifest)
        val outputFile = File(outputDir, "${trackId}.mp4")
        val totalSegments = segmentInfo.mediaUrls.size + 1 // +1 for init segment

        try {
            // Download init segment (overwrites any existing file)
            segmentDownloader.downloadSegmentToFile(
                url = segmentInfo.initUrl,
                outputFile = outputFile,
                append = false,
            )
            onProgress(1, totalSegments)

            // Download media segments (append to file)
            segmentInfo.mediaUrls.forEachIndexed { index, url ->
                segmentDownloader.downloadSegmentToFile(
                    url = url,
                    outputFile = outputFile,
                    append = true,
                )
                onProgress(index + 2, totalSegments) // +2: 1 for init + 1 for 0-based index
            }

            return TrackDownloadResult(
                filePath = outputFile.absolutePath,
                fileSize = outputFile.length(),
                manifestHash = playbackInfo.manifestHash,
                offlineRevalidateAt = playbackInfo.offlineRevalidateAt,
                offlineValidUntil = playbackInfo.offlineValidUntil,
                audioQuality = playbackInfo.audioQuality,
                bitDepth = playbackInfo.bitDepth,
                sampleRate = playbackInfo.sampleRate,
            )
        } catch (e: Exception) {
            // Clean up partial file on failure
            outputFile.delete()
            throw e
        }
    }

    private suspend fun getBearerToken(): String {
        val result = credentialsProvider.getCredentials(null)
        val token = result.successData?.token
            ?: throw RuntimeException("Failed to obtain credentials")
        return "Bearer $token"
    }
}
