package dev.tidesapp.wearos.download.data.api

import dev.tidesapp.wearos.download.data.api.dto.OfflineClientsResponse
import dev.tidesapp.wearos.download.data.api.dto.OfflinePlaybackInfoResponse
import dev.tidesapp.wearos.download.data.api.dto.SessionClientInfo
import dev.tidesapp.wearos.download.data.api.dto.SessionResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineDtosTest {

    private lateinit var json: Json

    @Before
    fun setup() {
        json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    // ── SessionResponse ─────────────────────────────────────────────────

    @Test
    fun `SessionResponse deserializes from Charles capture JSON`() {
        val input = """
            {
              "sessionId": "48c883e3-79c0-44fc-b090-055055cd38ab",
              "userId": 168139498,
              "countryCode": "US",
              "client": {
                "id": 593402320,
                "name": "Google Pixel 6a",
                "authorizedForOffline": true,
                "authorizedForOfflineDate": "2026-04-30T18:34:05.843+0000"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<SessionResponse>(input)

        assertEquals("48c883e3-79c0-44fc-b090-055055cd38ab", response.sessionId)
        assertEquals(168139498L, response.userId)
        assertEquals("US", response.countryCode)
        assertEquals(593402320L, response.client.id)
        assertEquals("Google Pixel 6a", response.client.name)
        assertTrue(response.client.authorizedForOffline)
        assertEquals("2026-04-30T18:34:05.843+0000", response.client.authorizedForOfflineDate)
    }

    @Test
    fun `SessionResponse defaults when fields are missing`() {
        val minimal = "{}"
        val response = json.decodeFromString<SessionResponse>(minimal)

        assertEquals("", response.sessionId)
        assertEquals(0L, response.userId)
        assertEquals("", response.countryCode)
        assertEquals(0L, response.client.id)
        assertEquals("", response.client.name)
        assertEquals(false, response.client.authorizedForOffline)
        assertNull(response.client.authorizedForOfflineDate)
    }

    @Test
    fun `SessionResponse ignores unknown fields`() {
        val withUnknowns = """
            {
              "sessionId": "abc",
              "userId": 1,
              "countryCode": "SE",
              "client": { "id": 1, "name": "x", "authorizedForOffline": false },
              "unknownField": "should be ignored",
              "anotherUnknown": 42
            }
        """.trimIndent()

        val response = json.decodeFromString<SessionResponse>(withUnknowns)
        assertEquals("abc", response.sessionId)
        assertEquals("SE", response.countryCode)
    }

    // ── SessionClientInfo ───────────────────────────────────────────────

    @Test
    fun `SessionClientInfo authorizedForOfflineDate is nullable`() {
        val input = """
            {
              "id": 100,
              "name": "Test Device",
              "authorizedForOffline": false
            }
        """.trimIndent()

        val client = json.decodeFromString<SessionClientInfo>(input)
        assertEquals(100L, client.id)
        assertEquals("Test Device", client.name)
        assertEquals(false, client.authorizedForOffline)
        assertNull(client.authorizedForOfflineDate)
    }

    // ── OfflineClientsResponse ──────────────────────────────────────────

    @Test
    fun `OfflineClientsResponse deserializes with items`() {
        val input = """
            {
              "limit": 9999,
              "offset": 0,
              "totalNumberOfItems": 2,
              "items": [
                {
                  "id": 100,
                  "name": "Device A",
                  "authorizedForOffline": true,
                  "authorizedForOfflineDate": "2026-01-01T00:00:00.000+0000"
                },
                {
                  "id": 200,
                  "name": "Device B",
                  "authorizedForOffline": false
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<OfflineClientsResponse>(input)
        assertEquals(9999, response.limit)
        assertEquals(0, response.offset)
        assertEquals(2, response.totalNumberOfItems)
        assertEquals(2, response.items.size)
        assertEquals("Device A", response.items[0].name)
        assertTrue(response.items[0].authorizedForOffline)
        assertEquals("Device B", response.items[1].name)
        assertNull(response.items[1].authorizedForOfflineDate)
    }

    @Test
    fun `OfflineClientsResponse defaults when fields are missing`() {
        val minimal = "{}"
        val response = json.decodeFromString<OfflineClientsResponse>(minimal)

        assertEquals(0, response.limit)
        assertEquals(0, response.offset)
        assertEquals(0, response.totalNumberOfItems)
        assertTrue(response.items.isEmpty())
    }

    // ── OfflinePlaybackInfoResponse ─────────────────────────────────────

    @Test
    fun `OfflinePlaybackInfoResponse deserializes from Charles capture JSON`() {
        val input = """
            {
              "trackId": 97429322,
              "assetPresentation": "FULL",
              "audioMode": "STEREO",
              "audioQuality": "LOSSLESS",
              "streamingSessionId": "27d5491f-7b04-447b-be41-7b7496132afc",
              "manifestMimeType": "application/dash+xml",
              "manifestHash": "mBEWfAyp2ufVftg7gBFEauWdASUXuTi4Tb78STJpQdQ=",
              "manifest": "PD94bWw=",
              "albumReplayGain": -6.87,
              "albumPeakAmplitude": 0.988586,
              "trackReplayGain": -6.87,
              "trackPeakAmplitude": 0.988525,
              "offlineRevalidateAt": 1779755647,
              "offlineValidUntil": 1780360447,
              "bitDepth": 16,
              "sampleRate": 44100
            }
        """.trimIndent()

        val response = json.decodeFromString<OfflinePlaybackInfoResponse>(input)

        assertEquals(97429322L, response.trackId)
        assertEquals("FULL", response.assetPresentation)
        assertEquals("STEREO", response.audioMode)
        assertEquals("LOSSLESS", response.audioQuality)
        assertEquals("27d5491f-7b04-447b-be41-7b7496132afc", response.streamingSessionId)
        assertEquals("application/dash+xml", response.manifestMimeType)
        assertEquals("mBEWfAyp2ufVftg7gBFEauWdASUXuTi4Tb78STJpQdQ=", response.manifestHash)
        assertEquals("PD94bWw=", response.manifest)
        assertEquals(-6.87, response.albumReplayGain, 0.001)
        assertEquals(0.988586, response.albumPeakAmplitude, 0.000001)
        assertEquals(-6.87, response.trackReplayGain, 0.001)
        assertEquals(0.988525, response.trackPeakAmplitude, 0.000001)
        assertEquals(1779755647L, response.offlineRevalidateAt)
        assertEquals(1780360447L, response.offlineValidUntil)
        assertEquals(16, response.bitDepth)
        assertEquals(44100, response.sampleRate)
    }

    @Test
    fun `OfflinePlaybackInfoResponse defaults when fields are missing`() {
        val minimal = "{}"
        val response = json.decodeFromString<OfflinePlaybackInfoResponse>(minimal)

        assertEquals(0L, response.trackId)
        assertEquals("", response.assetPresentation)
        assertEquals("", response.audioMode)
        assertEquals("", response.audioQuality)
        assertEquals("", response.streamingSessionId)
        assertEquals("", response.manifestMimeType)
        assertEquals("", response.manifestHash)
        assertEquals("", response.manifest)
        assertEquals(0.0, response.albumReplayGain, 0.0)
        assertEquals(0.0, response.albumPeakAmplitude, 0.0)
        assertEquals(0.0, response.trackReplayGain, 0.0)
        assertEquals(0.0, response.trackPeakAmplitude, 0.0)
        assertEquals(0L, response.offlineRevalidateAt)
        assertEquals(0L, response.offlineValidUntil)
        assertEquals(0, response.bitDepth)
        assertEquals(0, response.sampleRate)
    }

    @Test
    fun `OfflinePlaybackInfoResponse ignores unknown fields`() {
        val withUnknowns = """
            {
              "trackId": 123,
              "assetPresentation": "FULL",
              "audioMode": "STEREO",
              "audioQuality": "HIGH",
              "streamingSessionId": "sid",
              "manifestMimeType": "audio/flac",
              "manifestHash": "hash",
              "manifest": "data",
              "albumReplayGain": 0.0,
              "albumPeakAmplitude": 0.0,
              "trackReplayGain": 0.0,
              "trackPeakAmplitude": 0.0,
              "offlineRevalidateAt": 0,
              "offlineValidUntil": 0,
              "bitDepth": 16,
              "sampleRate": 44100,
              "futureField": "should be ignored",
              "anotherFuture": [1, 2, 3]
            }
        """.trimIndent()

        val response = json.decodeFromString<OfflinePlaybackInfoResponse>(withUnknowns)
        assertEquals(123L, response.trackId)
        assertEquals(44100, response.sampleRate)
    }
}
