package dev.tidesapp.wearos.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SharedModelsTest {

    @Test
    fun `NetworkState has all expected values`() {
        val values = NetworkState.entries
        assertEquals(3, values.size)
        assertEquals(NetworkState.WIFI, NetworkState.valueOf("WIFI"))
        assertEquals(NetworkState.CELLULAR, NetworkState.valueOf("CELLULAR"))
        assertEquals(NetworkState.OFFLINE, NetworkState.valueOf("OFFLINE"))
    }

    @Test
    fun `StreamingMode has all expected values`() {
        val values = StreamingMode.entries
        assertEquals(2, values.size)
        assertEquals(StreamingMode.STREAMING, StreamingMode.valueOf("STREAMING"))
        assertEquals(StreamingMode.OFFLINE, StreamingMode.valueOf("OFFLINE"))
    }

    @Test
    fun `PlaybackSourceType has all expected values`() {
        val values = PlaybackSourceType.entries
        assertEquals(4, values.size)
        assertEquals(PlaybackSourceType.ALBUM, PlaybackSourceType.valueOf("ALBUM"))
        assertEquals(PlaybackSourceType.PLAYLIST, PlaybackSourceType.valueOf("PLAYLIST"))
        assertEquals(PlaybackSourceType.SEARCH, PlaybackSourceType.valueOf("SEARCH"))
        assertEquals(PlaybackSourceType.FAVORITES, PlaybackSourceType.valueOf("FAVORITES"))
    }

    @Test
    fun `PlaybackContext holds correct values`() {
        val context = PlaybackContext(
            trackId = "track-123",
            sourceType = PlaybackSourceType.ALBUM,
            sourceId = "album-456",
        )
        assertEquals("track-123", context.trackId)
        assertEquals(PlaybackSourceType.ALBUM, context.sourceType)
        assertEquals("album-456", context.sourceId)
    }
}
