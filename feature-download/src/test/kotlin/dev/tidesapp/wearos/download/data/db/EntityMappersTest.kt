package dev.tidesapp.wearos.download.data.db

import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.DownloadedTrack
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappersTest {

    // region Track entity -> domain

    @Test
    fun `DownloadedTrackEntity maps to domain correctly`() {
        val entity = DownloadedTrackEntity(
            trackId = 123L,
            title = "Ocean Eyes",
            artistName = "Billie Eilish",
            albumTitle = "Don't Smile at Me",
            imageUrl = "https://img.example.com/cover.jpg",
            duration = 210,
            trackNumber = 3,
            filePath = "/data/tracks/123.flac",
            fileSize = 25_000_000L,
            audioQuality = "LOSSLESS",
            manifestHash = "abc123hash",
            offlineRevalidateAt = 1_700_000_000L,
            offlineValidUntil = 1_700_100_000L,
            downloadedAt = 1_699_900_000L,
            state = "COMPLETED",
            collectionId = "album-42",
            collectionType = "ALBUM",
        )

        val domain = entity.toDomain()

        assertEquals(123L, domain.trackId)
        assertEquals("Ocean Eyes", domain.title)
        assertEquals("Billie Eilish", domain.artistName)
        assertEquals("Don't Smile at Me", domain.albumTitle)
        assertEquals("https://img.example.com/cover.jpg", domain.imageUrl)
        assertEquals(210, domain.duration)
        assertEquals(3, domain.trackNumber)
        assertEquals("/data/tracks/123.flac", domain.filePath)
        assertEquals(25_000_000L, domain.fileSize)
        assertEquals("LOSSLESS", domain.audioQuality)
        assertEquals("abc123hash", domain.manifestHash)
        assertEquals(1_700_000_000L, domain.offlineRevalidateAt)
        assertEquals(1_700_100_000L, domain.offlineValidUntil)
        assertEquals(1_699_900_000L, domain.downloadedAt)
        assertEquals(DownloadState.COMPLETED, domain.state)
        assertEquals("album-42", domain.collectionId)
        assertEquals(CollectionType.ALBUM, domain.collectionType)
    }

    // endregion

    // region Track domain -> entity

    @Test
    fun `DownloadedTrack maps to entity correctly`() {
        val domain = DownloadedTrack(
            trackId = 456L,
            title = "Blinding Lights",
            artistName = "The Weeknd",
            albumTitle = "After Hours",
            imageUrl = "https://img.example.com/ah.jpg",
            duration = 200,
            trackNumber = 1,
            filePath = "/data/tracks/456.flac",
            fileSize = 30_000_000L,
            audioQuality = "HI_RES",
            manifestHash = "def456hash",
            offlineRevalidateAt = 1_700_200_000L,
            offlineValidUntil = 1_700_300_000L,
            downloadedAt = 1_700_100_000L,
            state = DownloadState.DOWNLOADING,
            collectionId = "playlist-99",
            collectionType = CollectionType.PLAYLIST,
        )

        val entity = domain.toEntity()

        assertEquals(456L, entity.trackId)
        assertEquals("Blinding Lights", entity.title)
        assertEquals("The Weeknd", entity.artistName)
        assertEquals("After Hours", entity.albumTitle)
        assertEquals("https://img.example.com/ah.jpg", entity.imageUrl)
        assertEquals(200, entity.duration)
        assertEquals(1, entity.trackNumber)
        assertEquals("/data/tracks/456.flac", entity.filePath)
        assertEquals(30_000_000L, entity.fileSize)
        assertEquals("HI_RES", entity.audioQuality)
        assertEquals("def456hash", entity.manifestHash)
        assertEquals(1_700_200_000L, entity.offlineRevalidateAt)
        assertEquals(1_700_300_000L, entity.offlineValidUntil)
        assertEquals(1_700_100_000L, entity.downloadedAt)
        assertEquals("DOWNLOADING", entity.state)
        assertEquals("playlist-99", entity.collectionId)
        assertEquals("PLAYLIST", entity.collectionType)
    }

    // endregion

    // region Track round-trip

    @Test
    fun `track entity round-trip preserves all fields`() {
        val original = DownloadedTrackEntity(
            trackId = 789L,
            title = "Starboy",
            artistName = "The Weeknd",
            albumTitle = "Starboy",
            imageUrl = "https://img.example.com/sb.jpg",
            duration = 230,
            trackNumber = 5,
            filePath = "/data/tracks/789.flac",
            fileSize = 28_000_000L,
            audioQuality = "HIGH",
            manifestHash = "ghi789hash",
            offlineRevalidateAt = 1_700_400_000L,
            offlineValidUntil = 1_700_500_000L,
            downloadedAt = 1_700_300_000L,
            state = "PENDING",
            collectionId = "album-77",
            collectionType = "PLAYLIST",
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

    // endregion

    // region Collection entity -> domain

    @Test
    fun `DownloadedCollectionEntity maps to domain correctly`() {
        val entity = DownloadedCollectionEntity(
            id = "collection-1",
            type = "ALBUM",
            title = "After Hours",
            imageUrl = "https://img.example.com/ah.jpg",
            trackCount = 14,
            downloadedTrackCount = 10,
            totalSizeBytes = 400_000_000L,
            downloadedAt = 1_700_000_000L,
            state = "DOWNLOADING",
        )

        val domain = entity.toDomain()

        assertEquals("collection-1", domain.id)
        assertEquals(CollectionType.ALBUM, domain.type)
        assertEquals("After Hours", domain.title)
        assertEquals("https://img.example.com/ah.jpg", domain.imageUrl)
        assertEquals(14, domain.trackCount)
        assertEquals(10, domain.downloadedTrackCount)
        assertEquals(400_000_000L, domain.totalSizeBytes)
        assertEquals(1_700_000_000L, domain.downloadedAt)
        assertEquals(DownloadState.DOWNLOADING, domain.state)
    }

    // endregion

    // region Collection domain -> entity

    @Test
    fun `DownloadedCollection maps to entity correctly`() {
        val domain = DownloadedCollection(
            id = "collection-2",
            type = CollectionType.PLAYLIST,
            title = "Chill Vibes",
            imageUrl = "https://img.example.com/cv.jpg",
            trackCount = 20,
            downloadedTrackCount = 20,
            totalSizeBytes = 600_000_000L,
            downloadedAt = 1_700_100_000L,
            state = DownloadState.COMPLETED,
        )

        val entity = domain.toEntity()

        assertEquals("collection-2", entity.id)
        assertEquals("PLAYLIST", entity.type)
        assertEquals("Chill Vibes", entity.title)
        assertEquals("https://img.example.com/cv.jpg", entity.imageUrl)
        assertEquals(20, entity.trackCount)
        assertEquals(20, entity.downloadedTrackCount)
        assertEquals(600_000_000L, entity.totalSizeBytes)
        assertEquals(1_700_100_000L, entity.downloadedAt)
        assertEquals("COMPLETED", entity.state)
    }

    // endregion

    // region Collection round-trip

    @Test
    fun `collection entity round-trip preserves all fields`() {
        val original = DownloadedCollectionEntity(
            id = "collection-3",
            type = "ALBUM",
            title = "Dawn FM",
            imageUrl = "https://img.example.com/dfm.jpg",
            trackCount = 16,
            downloadedTrackCount = 8,
            totalSizeBytes = 450_000_000L,
            downloadedAt = 1_700_200_000L,
            state = "FAILED",
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

    // endregion
}
