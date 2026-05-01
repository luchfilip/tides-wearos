package dev.tidesapp.wearos.download.data.repository

import android.content.Context
import app.cash.turbine.test
import dev.tidesapp.wearos.download.data.db.DownloadedCollectionDao
import dev.tidesapp.wearos.download.data.db.DownloadedTrackDao
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DownloadStorageRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var trackDao: DownloadedTrackDao
    private lateinit var collectionDao: DownloadedCollectionDao
    private lateinit var context: Context
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var storageLimitFlow: MutableStateFlow<Long>
    private lateinit var repository: DownloadStorageRepositoryImpl

    @Before
    fun setup() {
        trackDao = mockk(relaxed = true)
        collectionDao = mockk(relaxed = true)
        context = mockk {
            every { filesDir } returns tempFolder.root
        }
        storageLimitFlow = MutableStateFlow(1_073_741_824L) // 1GB default
        settingsRepository = mockk {
            every { getStorageLimitBytes() } returns storageLimitFlow
        }
        repository = DownloadStorageRepositoryImpl(context, trackDao, collectionDao, settingsRepository)
    }

    // --- getStorageInfo ---

    @Test
    fun `getStorageInfo combines dao and settings limit`() = runTest {
        every { trackDao.getTotalSizeBytes() } returns flowOf(500_000_000L)
        every { trackDao.getTrackCount() } returns flowOf(10)

        repository.getStorageInfo().test {
            val info = awaitItem()
            assertEquals(500_000_000L, info.usedBytes)
            assertEquals(1_073_741_824L, info.limitBytes)
            assertEquals(10, info.trackCount)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getStorageInfo updates when limit changes`() = runTest {
        every { trackDao.getTotalSizeBytes() } returns flowOf(500_000_000L)
        every { trackDao.getTrackCount() } returns flowOf(10)

        repository.getStorageInfo().test {
            val first = awaitItem()
            assertEquals(1_073_741_824L, first.limitBytes)

            storageLimitFlow.value = 2_147_483_648L // 2GB
            val second = awaitItem()
            assertEquals(2_147_483_648L, second.limitBytes)

            cancelAndConsumeRemainingEvents()
        }
    }

    // --- getDownloadDirectory ---

    @Test
    fun `getDownloadDirectory creates dir if not exists`() = runTest {
        val dir = repository.getDownloadDirectory()

        assertTrue(dir.exists())
        assertTrue(dir.isDirectory)
        assertEquals("offline_tracks", dir.name)
    }

    @Test
    fun `getDownloadDirectory returns existing dir`() = runTest {
        val expected = File(tempFolder.root, "offline_tracks")
        expected.mkdirs()
        File(expected, "test.flac").createNewFile()

        val dir = repository.getDownloadDirectory()

        assertTrue(dir.exists())
        assertEquals(1, dir.listFiles()?.size)
    }

    // --- hasSpaceForTrack ---

    @Test
    fun `hasSpaceForTrack returns true when under limit`() = runTest {
        every { trackDao.getTotalSizeBytes() } returns flowOf(500_000_000L) // 500MB used

        val result = repository.hasSpaceForTrack(100_000_000L) // 100MB track

        assertTrue(result) // 500M + 100M = 600M < 1GB
    }

    @Test
    fun `hasSpaceForTrack returns false when over limit`() = runTest {
        every { trackDao.getTotalSizeBytes() } returns flowOf(900_000_000L) // 900MB used

        val result = repository.hasSpaceForTrack(200_000_000L) // 200MB track

        assertFalse(result) // 900M + 200M = 1.1GB > 1GB
    }

    @Test
    fun `hasSpaceForTrack returns true when exactly at limit`() = runTest {
        every { trackDao.getTotalSizeBytes() } returns flowOf(900_000_000L)

        val result = repository.hasSpaceForTrack(173_741_824L) // exactly reaches 1GB

        assertTrue(result)
    }

    // --- clearAllDownloads ---

    @Test
    fun `clearAllDownloads deletes database entities and directory contents`() = runTest {
        repository.clearAllDownloads()

        coVerify { trackDao.deleteAll() }
        coVerify { collectionDao.deleteAll() }
    }

    @Test
    fun `clearAllDownloads deletes directory contents`() = runTest {
        val downloadDir = File(tempFolder.root, "offline_tracks")
        downloadDir.mkdirs()
        File(downloadDir, "track1.flac").writeText("audio data")
        File(downloadDir, "track2.flac").writeText("more audio data")
        val subDir = File(downloadDir, "subdir")
        subDir.mkdirs()
        File(subDir, "nested.flac").writeText("nested data")

        assertEquals(3, downloadDir.listFiles()?.size) // 2 files + 1 dir

        repository.clearAllDownloads()

        assertTrue(downloadDir.exists())
        assertEquals(0, downloadDir.listFiles()?.size ?: -1)
    }

    @Test
    fun `clearAllDownloads handles non-existent directory`() = runTest {
        // Directory doesn't exist yet — should not throw
        repository.clearAllDownloads()

        val downloadDir = File(tempFolder.root, "offline_tracks")
        assertTrue(downloadDir.exists())
    }
}
