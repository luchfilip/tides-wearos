package dev.tidesapp.wearos.download.data.repository

import app.cash.turbine.test
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionEntity
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao
import dev.tidesapp.wearos.download.data.db.DownloadedTrackEntity
import dev.tidesapp.wearos.download.data.download.TrackDownloadResult
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DownloadRepositoryImplTest {

    private lateinit var trackDao: DownloadedTrackDao
    private lateinit var collectionDao: DownloadedCollectionDao
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: DownloadRepositoryImpl

    private val sampleTrackEntity = DownloadedTrackEntity(
        trackId = 1L,
        title = "Track One",
        artistName = "Artist",
        albumTitle = "Album",
        imageUrl = "https://img.example.com/1.jpg",
        duration = 180,
        trackNumber = 1,
        filePath = "/data/offline_tracks/1.flac",
        fileSize = 10_000_000L,
        audioQuality = "LOSSLESS",
        manifestHash = "abc123",
        offlineRevalidateAt = 1000L,
        offlineValidUntil = 2000L,
        downloadedAt = 500L,
        state = "COMPLETED",
        collectionId = "collection-1",
        collectionType = "ALBUM",
    )

    private val sampleCollectionEntity = DownloadedCollectionEntity(
        id = "collection-1",
        type = "ALBUM",
        title = "Test Album",
        imageUrl = "https://img.example.com/album.jpg",
        trackCount = 10,
        downloadedTrackCount = 5,
        totalSizeBytes = 50_000_000L,
        downloadedAt = 500L,
        state = "DOWNLOADING",
    )

    @Before
    fun setup() {
        trackDao = mockk(relaxed = true)
        collectionDao = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        every { settingsRepository.getStorageLimitBytes() } returns flowOf(1_073_741_824L)
        repository = DownloadRepositoryImpl(trackDao, collectionDao, settingsRepository)
    }

    // --- getDownloadedCollections ---

    @Test
    fun `getDownloadedCollections maps entities to domain`() = runTest {
        every { collectionDao.getAll() } returns flowOf(listOf(sampleCollectionEntity))

        repository.getDownloadedCollections().test {
            val collections = awaitItem()
            assertEquals(1, collections.size)
            val c = collections[0]
            assertEquals("collection-1", c.id)
            assertEquals(CollectionType.ALBUM, c.type)
            assertEquals("Test Album", c.title)
            assertEquals(10, c.trackCount)
            assertEquals(5, c.downloadedTrackCount)
            assertEquals(DownloadState.DOWNLOADING, c.state)
            cancelAndConsumeRemainingEvents()
        }
    }

    // --- getDownloadedTracksForCollection ---

    @Test
    fun `getDownloadedTracksForCollection maps entities to domain`() = runTest {
        every { trackDao.getByCollectionId("collection-1") } returns flowOf(
            listOf(sampleTrackEntity),
        )

        repository.getDownloadedTracksForCollection("collection-1").test {
            val tracks = awaitItem()
            assertEquals(1, tracks.size)
            assertEquals(1L, tracks[0].trackId)
            assertEquals("Track One", tracks[0].title)
            assertEquals(DownloadState.COMPLETED, tracks[0].state)
            cancelAndConsumeRemainingEvents()
        }
    }

    // --- getStorageInfo ---

    @Test
    fun `getStorageInfo combines dao flows with settings limit`() = runTest {
        every { trackDao.getTotalSizeBytes() } returns flowOf(100_000_000L)
        every { trackDao.getTrackCount() } returns flowOf(5)

        repository.getStorageInfo().test {
            val info = awaitItem()
            assertEquals(100_000_000L, info.usedBytes)
            assertEquals(1_073_741_824L, info.limitBytes)
            assertEquals(5, info.trackCount)
            cancelAndConsumeRemainingEvents()
        }
    }

    // --- isTrackDownloaded ---

    @Test
    fun `isTrackDownloaded delegates to dao`() = runTest {
        coEvery { trackDao.isTrackDownloaded(1L) } returns true

        assertTrue(repository.isTrackDownloaded(1L))
    }

    @Test
    fun `isTrackDownloaded returns false for missing track`() = runTest {
        coEvery { trackDao.isTrackDownloaded(99L) } returns false

        assertFalse(repository.isTrackDownloaded(99L))
    }

    // --- getDownloadedTrack ---

    @Test
    fun `getDownloadedTrack returns domain model`() = runTest {
        coEvery { trackDao.getByTrackId(1L) } returns sampleTrackEntity

        val track = repository.getDownloadedTrack(1L)

        assertEquals(1L, track?.trackId)
        assertEquals("Track One", track?.title)
        assertEquals(DownloadState.COMPLETED, track?.state)
    }

    @Test
    fun `getDownloadedTrack returns null for missing track`() = runTest {
        coEvery { trackDao.getByTrackId(99L) } returns null

        assertNull(repository.getDownloadedTrack(99L))
    }

    // --- queueCollectionDownload ---

    @Test
    fun `queueCollectionDownload creates collection and track entities`() = runTest {
        val collectionSlot = slot<DownloadedCollectionEntity>()
        val tracksSlot = slot<List<DownloadedTrackEntity>>()
        coEvery { collectionDao.insert(capture(collectionSlot)) } just Runs
        coEvery { trackDao.insertAll(capture(tracksSlot)) } just Runs

        val tracks = listOf(
            TrackItem("100", "Song A", "Artist A", "Album X", 180, 1, "https://img/a.jpg"),
            TrackItem("200", "Song B", "Artist B", "Album X", 240, 2, null),
        )

        repository.queueCollectionDownload(
            collectionId = "col-1",
            type = CollectionType.PLAYLIST,
            title = "My Playlist",
            imageUrl = "https://img/playlist.jpg",
            tracks = tracks,
        )

        // Verify collection entity
        val col = collectionSlot.captured
        assertEquals("col-1", col.id)
        assertEquals("PLAYLIST", col.type)
        assertEquals("My Playlist", col.title)
        assertEquals(2, col.trackCount)
        assertEquals(0, col.downloadedTrackCount)
        assertEquals(0L, col.totalSizeBytes)
        assertEquals("PENDING", col.state)

        // Verify track entities
        val insertedTracks = tracksSlot.captured
        assertEquals(2, insertedTracks.size)

        val t1 = insertedTracks[0]
        assertEquals(100L, t1.trackId)
        assertEquals("Song A", t1.title)
        assertEquals("Artist A", t1.artistName)
        assertEquals("Album X", t1.albumTitle)
        assertEquals("PENDING", t1.state)
        assertEquals("col-1", t1.collectionId)
        assertEquals("PLAYLIST", t1.collectionType)
        assertEquals("", t1.filePath)
        assertEquals(0L, t1.fileSize)
        assertEquals("", t1.manifestHash)
        assertEquals(0L, t1.offlineRevalidateAt)
        assertEquals(0L, t1.offlineValidUntil)

        val t2 = insertedTracks[1]
        assertEquals(200L, t2.trackId)
        assertEquals("", t2.imageUrl) // null imageUrl becomes empty string
    }

    // --- markTrackCompleted ---

    @Test
    fun `markTrackCompleted updates track and recalculates collection progress`() = runTest {
        val result = TrackDownloadResult(
            filePath = "/data/offline_tracks/100.flac",
            fileSize = 15_000_000L,
            manifestHash = "hash123",
            offlineRevalidateAt = 3000L,
            offlineValidUntil = 6000L,
            audioQuality = "LOSSLESS",
            bitDepth = 24,
            sampleRate = 96000,
        )

        // Track belongs to collection-1
        coEvery { trackDao.getByTrackId(100L) } returns sampleTrackEntity.copy(
            trackId = 100L,
            collectionId = "collection-1",
            collectionType = "ALBUM",
        )

        // After completing, the collection has these completed tracks
        val completedTrack1 = sampleTrackEntity.copy(trackId = 100L, fileSize = 15_000_000L, state = "COMPLETED")
        val completedTrack2 = sampleTrackEntity.copy(trackId = 101L, fileSize = 10_000_000L, state = "COMPLETED")
        every { trackDao.getByCollectionId("collection-1") } returns flowOf(
            listOf(
                completedTrack1,
                completedTrack2,
                sampleTrackEntity.copy(trackId = 102L, state = "PENDING"),
            ),
        )

        coEvery { collectionDao.getById("collection-1", "ALBUM") } returns sampleCollectionEntity
        coEvery { trackDao.updateCompleted(any(), any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { collectionDao.update(any()) } just Runs

        repository.markTrackCompleted(100L, result)

        coVerify {
            trackDao.updateCompleted(
                trackId = 100L,
                state = "COMPLETED",
                filePath = "/data/offline_tracks/100.flac",
                fileSize = 15_000_000L,
                manifestHash = "hash123",
                revalidateAt = 3000L,
                validUntil = 6000L,
            )
        }

        // Collection should be updated with recalculated progress
        coVerify {
            collectionDao.update(
                withArg { entity ->
                    assertEquals(2, entity.downloadedTrackCount)
                    assertEquals(25_000_000L, entity.totalSizeBytes) // 15M + 10M
                    assertEquals("DOWNLOADING", entity.state) // not all done
                },
            )
        }
    }

    @Test
    fun `markTrackCompleted sets collection state to COMPLETED when all tracks done`() = runTest {
        val result = TrackDownloadResult(
            filePath = "/data/offline_tracks/100.flac",
            fileSize = 10_000_000L,
            manifestHash = "hash",
            offlineRevalidateAt = 1000L,
            offlineValidUntil = 2000L,
            audioQuality = "HIGH",
            bitDepth = 16,
            sampleRate = 44100,
        )

        coEvery { trackDao.getByTrackId(100L) } returns sampleTrackEntity.copy(
            trackId = 100L,
            collectionId = "collection-1",
            collectionType = "ALBUM",
        )

        // All tracks completed after this update
        every { trackDao.getByCollectionId("collection-1") } returns flowOf(
            listOf(
                sampleTrackEntity.copy(trackId = 100L, fileSize = 10_000_000L, state = "COMPLETED"),
                sampleTrackEntity.copy(trackId = 101L, fileSize = 10_000_000L, state = "COMPLETED"),
            ),
        )

        coEvery { collectionDao.getById("collection-1", "ALBUM") } returns sampleCollectionEntity.copy(
            trackCount = 2,
        )
        coEvery { trackDao.updateCompleted(any(), any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { collectionDao.update(any()) } just Runs

        repository.markTrackCompleted(100L, result)

        coVerify {
            collectionDao.update(
                withArg { entity ->
                    assertEquals(2, entity.downloadedTrackCount)
                    assertEquals("COMPLETED", entity.state)
                },
            )
        }
    }

    // --- markTrackFailed ---

    @Test
    fun `markTrackFailed updates track state and recalculates collection`() = runTest {
        coEvery { trackDao.updateState(any(), any()) } just Runs
        coEvery { trackDao.getByTrackId(100L) } returns sampleTrackEntity.copy(
            trackId = 100L,
            collectionId = "collection-1",
            collectionType = "ALBUM",
        )
        coEvery { collectionDao.getById("collection-1", "ALBUM") } returns sampleCollectionEntity
        every { trackDao.getByCollectionId("collection-1") } returns flowOf(
            listOf(sampleTrackEntity.copy(trackId = 100L, state = "FAILED")),
        )
        coEvery { collectionDao.update(any()) } just Runs

        repository.markTrackFailed(100L, "Network error")

        coVerify { trackDao.updateState(100L, "FAILED") }
        coVerify { collectionDao.update(withArg { assertEquals("FAILED", it.state) }) }
    }

    // --- deleteCollection ---

    @Test
    fun `deleteCollection removes track entities and collection entity`() = runTest {
        every { trackDao.getByCollectionId("collection-1") } returns flowOf(
            listOf(
                sampleTrackEntity.copy(filePath = ""),
                sampleTrackEntity.copy(trackId = 2L, filePath = ""),
            ),
        )
        coEvery { trackDao.deleteByCollectionId("collection-1") } just Runs
        coEvery { collectionDao.deleteById("collection-1", "ALBUM") } just Runs

        repository.deleteCollection("collection-1", CollectionType.ALBUM)

        coVerify { trackDao.deleteByCollectionId("collection-1") }
        coVerify { collectionDao.deleteById("collection-1", "ALBUM") }
    }

    // --- deleteTrack ---

    @Test
    fun `deleteTrack removes entity`() = runTest {
        coEvery { trackDao.getByTrackId(1L) } returns sampleTrackEntity.copy(filePath = "")
        coEvery { trackDao.deleteByTrackId(1L) } just Runs

        repository.deleteTrack(1L)

        coVerify { trackDao.deleteByTrackId(1L) }
    }

    // --- getExpiredTracks ---

    @Test
    fun `getExpiredTracks delegates to dao and maps to domain`() = runTest {
        coEvery { trackDao.getExpiredTracks(5000L) } returns listOf(sampleTrackEntity)

        val result = repository.getExpiredTracks(5000L)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].trackId)
    }

    // --- getTracksNeedingRevalidation ---

    @Test
    fun `getTracksNeedingRevalidation delegates to dao and maps to domain`() = runTest {
        coEvery { trackDao.getTracksNeedingRevalidation(3000L) } returns listOf(sampleTrackEntity)

        val result = repository.getTracksNeedingRevalidation(3000L)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].trackId)
    }

    // --- getPendingTracksForCollection ---

    @Test
    fun `getPendingTracksForCollection delegates to dao and maps to domain`() = runTest {
        val pendingEntity = sampleTrackEntity.copy(state = "PENDING")
        coEvery { trackDao.getPendingByCollectionId("collection-1") } returns listOf(pendingEntity)

        val result = repository.getPendingTracksForCollection("collection-1")

        assertEquals(1, result.size)
        assertEquals(DownloadState.PENDING, result[0].state)
    }
}
