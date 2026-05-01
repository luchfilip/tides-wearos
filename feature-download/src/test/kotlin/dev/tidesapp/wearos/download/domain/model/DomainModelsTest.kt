package dev.tidesapp.wearos.download.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelsTest {

    // ── DownloadState enum ──────────────────────────────────────────────

    @Test
    fun `DownloadState has all expected values`() {
        val values = DownloadState.entries
        assertEquals(5, values.size)
        assertEquals(DownloadState.PENDING, DownloadState.valueOf("PENDING"))
        assertEquals(DownloadState.DOWNLOADING, DownloadState.valueOf("DOWNLOADING"))
        assertEquals(DownloadState.COMPLETED, DownloadState.valueOf("COMPLETED"))
        assertEquals(DownloadState.FAILED, DownloadState.valueOf("FAILED"))
        assertEquals(DownloadState.EXPIRED, DownloadState.valueOf("EXPIRED"))
    }

    // ── CollectionType enum ─────────────────────────────────────────────

    @Test
    fun `CollectionType has all expected values`() {
        val values = CollectionType.entries
        assertEquals(2, values.size)
        assertEquals(CollectionType.ALBUM, CollectionType.valueOf("ALBUM"))
        assertEquals(CollectionType.PLAYLIST, CollectionType.valueOf("PLAYLIST"))
    }

    // ── DownloadedTrack data class ──────────────────────────────────────

    @Test
    fun `DownloadedTrack construction holds correct values`() {
        val track = DownloadedTrack(
            trackId = 97429322L,
            title = "Test Track",
            artistName = "Test Artist",
            albumTitle = "Test Album",
            imageUrl = "https://example.com/image.jpg",
            duration = 240,
            trackNumber = 5,
            filePath = "/data/tracks/97429322.flac",
            fileSize = 25_000_000L,
            audioQuality = "LOSSLESS",
            manifestHash = "mBEWfAyp2ufVftg7gBFEauWdASUXuTi4Tb78STJpQdQ=",
            offlineRevalidateAt = 1779755647L,
            offlineValidUntil = 1780360447L,
            downloadedAt = 1779000000L,
            state = DownloadState.COMPLETED,
            collectionId = "album-456",
            collectionType = CollectionType.ALBUM,
        )

        assertEquals(97429322L, track.trackId)
        assertEquals("Test Track", track.title)
        assertEquals("Test Artist", track.artistName)
        assertEquals("Test Album", track.albumTitle)
        assertEquals("https://example.com/image.jpg", track.imageUrl)
        assertEquals("/data/tracks/97429322.flac", track.filePath)
        assertEquals(25_000_000L, track.fileSize)
        assertEquals("LOSSLESS", track.audioQuality)
        assertEquals("mBEWfAyp2ufVftg7gBFEauWdASUXuTi4Tb78STJpQdQ=", track.manifestHash)
        assertEquals(1779755647L, track.offlineRevalidateAt)
        assertEquals(1780360447L, track.offlineValidUntil)
        assertEquals(1779000000L, track.downloadedAt)
        assertEquals(DownloadState.COMPLETED, track.state)
        assertEquals("album-456", track.collectionId)
        assertEquals(CollectionType.ALBUM, track.collectionType)
    }

    @Test
    fun `DownloadedTrack equality works correctly`() {
        val track1 = createTrack()
        val track2 = createTrack()
        assertEquals(track1, track2)
        assertEquals(track1.hashCode(), track2.hashCode())
    }

    @Test
    fun `DownloadedTrack copy changes only specified fields`() {
        val original = createTrack()
        val copied = original.copy(state = DownloadState.FAILED, fileSize = 0L)

        assertEquals(DownloadState.FAILED, copied.state)
        assertEquals(0L, copied.fileSize)
        assertEquals(original.trackId, copied.trackId)
        assertEquals(original.title, copied.title)
    }

    // ── DownloadedCollection data class ─────────────────────────────────

    @Test
    fun `DownloadedCollection construction holds correct values`() {
        val collection = DownloadedCollection(
            id = "playlist-789",
            type = CollectionType.PLAYLIST,
            title = "My Playlist",
            imageUrl = "https://example.com/playlist.jpg",
            trackCount = 12,
            downloadedTrackCount = 8,
            totalSizeBytes = 300_000_000L,
            downloadedAt = 1779000000L,
            state = DownloadState.DOWNLOADING,
        )

        assertEquals("playlist-789", collection.id)
        assertEquals(CollectionType.PLAYLIST, collection.type)
        assertEquals("My Playlist", collection.title)
        assertEquals("https://example.com/playlist.jpg", collection.imageUrl)
        assertEquals(12, collection.trackCount)
        assertEquals(8, collection.downloadedTrackCount)
        assertEquals(300_000_000L, collection.totalSizeBytes)
        assertEquals(1779000000L, collection.downloadedAt)
        assertEquals(DownloadState.DOWNLOADING, collection.state)
    }

    @Test
    fun `DownloadedCollection equality works correctly`() {
        val c1 = createCollection()
        val c2 = createCollection()
        assertEquals(c1, c2)
        assertEquals(c1.hashCode(), c2.hashCode())
    }

    @Test
    fun `DownloadedCollection copy changes only specified fields`() {
        val original = createCollection()
        val copied = original.copy(downloadedTrackCount = 12, state = DownloadState.COMPLETED)

        assertEquals(12, copied.downloadedTrackCount)
        assertEquals(DownloadState.COMPLETED, copied.state)
        assertEquals(original.id, copied.id)
    }

    // ── OfflineRegistration data class ──────────────────────────────────

    @Test
    fun `OfflineRegistration construction holds correct values`() {
        val reg = OfflineRegistration(
            clientId = 593402320L,
            authorizedForOffline = true,
            authorizedAt = 1779000000L,
        )

        assertEquals(593402320L, reg.clientId)
        assertTrue(reg.authorizedForOffline)
        assertEquals(1779000000L, reg.authorizedAt)
    }

    @Test
    fun `OfflineRegistration authorizedAt can be null`() {
        val reg = OfflineRegistration(
            clientId = 100L,
            authorizedForOffline = false,
            authorizedAt = null,
        )

        assertFalse(reg.authorizedForOffline)
        assertNull(reg.authorizedAt)
    }

    @Test
    fun `OfflineRegistration equality works correctly`() {
        val r1 = OfflineRegistration(1L, true, 100L)
        val r2 = OfflineRegistration(1L, true, 100L)
        assertEquals(r1, r2)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    // ── StorageInfo data class ──────────────────────────────────────────

    @Test
    fun `StorageInfo construction holds correct values`() {
        val info = StorageInfo(
            usedBytes = 500_000_000L,
            limitBytes = 2_000_000_000L,
            trackCount = 42,
        )

        assertEquals(500_000_000L, info.usedBytes)
        assertEquals(2_000_000_000L, info.limitBytes)
        assertEquals(42, info.trackCount)
    }

    @Test
    fun `StorageInfo equality works correctly`() {
        val s1 = StorageInfo(100L, 200L, 3)
        val s2 = StorageInfo(100L, 200L, 3)
        assertEquals(s1, s2)
        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun `StorageInfo copy changes only specified fields`() {
        val original = StorageInfo(100L, 200L, 3)
        val copied = original.copy(trackCount = 10)

        assertEquals(10, copied.trackCount)
        assertEquals(original.usedBytes, copied.usedBytes)
        assertEquals(original.limitBytes, copied.limitBytes)
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun createTrack() = DownloadedTrack(
        trackId = 97429322L,
        title = "Test Track",
        artistName = "Test Artist",
        albumTitle = "Test Album",
        imageUrl = "https://example.com/image.jpg",
        duration = 240,
        trackNumber = 5,
        filePath = "/data/tracks/97429322.flac",
        fileSize = 25_000_000L,
        audioQuality = "LOSSLESS",
        manifestHash = "abc123",
        offlineRevalidateAt = 1779755647L,
        offlineValidUntil = 1780360447L,
        downloadedAt = 1779000000L,
        state = DownloadState.COMPLETED,
        collectionId = "album-456",
        collectionType = CollectionType.ALBUM,
    )

    private fun createCollection() = DownloadedCollection(
        id = "playlist-789",
        type = CollectionType.PLAYLIST,
        title = "My Playlist",
        imageUrl = "https://example.com/playlist.jpg",
        trackCount = 12,
        downloadedTrackCount = 8,
        totalSizeBytes = 300_000_000L,
        downloadedAt = 1779000000L,
        state = DownloadState.DOWNLOADING,
    )
}
