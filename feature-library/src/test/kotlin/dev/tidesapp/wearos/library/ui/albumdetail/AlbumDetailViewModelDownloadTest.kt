package dev.tidesapp.wearos.library.ui.albumdetail

import androidx.lifecycle.SavedStateHandle
import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.core.domain.playback.PlaybackControl
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.library.domain.repository.AlbumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelDownloadTest {

    private lateinit var albumRepository: AlbumRepository
    private lateinit var playbackControl: PlaybackControl
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var downloadWorkScheduler: DownloadWorkScheduler
    private lateinit var viewModel: AlbumDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testAlbum = AlbumItem(
        id = "album-1",
        title = "Test Album",
        artistName = "Test Artist",
        imageUrl = "https://img.tidal.com/album-1",
        releaseDate = "2024-01-01",
        numberOfTracks = 3,
    )

    private val testTracks = List(3) { index ->
        TrackItem(
            id = "track-$index",
            title = "Track $index",
            artistName = "Test Artist",
            albumTitle = "Test Album",
            duration = 200 + index * 30,
            trackNumber = index + 1,
            imageUrl = "https://img.tidal.com/track-$index",
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        albumRepository = mockk()
        playbackControl = mockk(relaxed = true)
        downloadWorkScheduler = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `DownloadAlbum queues collection and enqueues work`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(emptyList())
            coEvery { queueCollectionDownload(any(), any(), any(), any(), any()) } returns Unit
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)
        coEvery { downloadWorkScheduler.enqueueCollectionDownload(any(), any()) } returns Unit

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        viewModel.onEvent(AlbumDetailUiEvent.DownloadAlbum)
        advanceUntilIdle()

        coVerify {
            downloadRepository.queueCollectionDownload(
                "album-1", CollectionType.ALBUM, "Test Album", "https://img.tidal.com/album-1", testTracks,
            )
        }
        coVerify {
            downloadWorkScheduler.enqueueCollectionDownload("album-1", CollectionType.ALBUM)
        }
    }

    @Test
    fun `RemoveDownload deletes collection and cancels work`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(
                listOf(createDownloadedCollection(DownloadState.COMPLETED)),
            )
            coEvery { deleteCollection(any(), any()) } returns Unit
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        viewModel.onEvent(AlbumDetailUiEvent.RemoveDownload)
        advanceUntilIdle()

        coVerify { downloadRepository.deleteCollection("album-1", CollectionType.ALBUM) }
        verify { downloadWorkScheduler.cancelCollectionDownload("album-1") }
    }

    @Test
    fun `completed download state sets isDownloaded true`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(
                listOf(createDownloadedCollection(DownloadState.COMPLETED)),
            )
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertTrue(state.isDownloaded)
        assertFalse(state.isDownloading)
    }

    @Test
    fun `pending download state sets isDownloading true`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(
                listOf(createDownloadedCollection(DownloadState.PENDING)),
            )
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertFalse(state.isDownloaded)
        assertTrue(state.isDownloading)
    }

    @Test
    fun `downloading state sets isDownloading true`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(
                listOf(createDownloadedCollection(DownloadState.DOWNLOADING)),
            )
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertFalse(state.isDownloaded)
        assertTrue(state.isDownloading)
    }

    @Test
    fun `no matching collection keeps defaults`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(emptyList())
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertFalse(state.isDownloaded)
        assertFalse(state.isDownloading)
    }

    @Test
    fun `download state updates reactively when flow emits`() = runTest {
        val collectionsFlow = MutableStateFlow<List<DownloadedCollection>>(emptyList())
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns collectionsFlow
        }
        coEvery { albumRepository.getAlbumDetail("album-1") } returns Result.success(testAlbum)
        coEvery { albumRepository.getAlbumTracks("album-1") } returns Result.success(testTracks)

        viewModel = AlbumDetailViewModel(
            albumRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(AlbumDetailUiEvent.LoadAlbumDetail("album-1"))
        advanceUntilIdle()

        var state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertFalse(state.isDownloaded)

        collectionsFlow.value = listOf(createDownloadedCollection(DownloadState.COMPLETED))
        advanceUntilIdle()

        state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertTrue(state.isDownloaded)
    }

    private fun createDownloadedCollection(state: DownloadState) = DownloadedCollection(
        id = "album-1",
        type = CollectionType.ALBUM,
        title = "Test Album",
        imageUrl = "https://img.tidal.com/album-1",
        trackCount = 3,
        downloadedTrackCount = if (state == DownloadState.COMPLETED) 3 else 0,
        totalSizeBytes = 0L,
        downloadedAt = System.currentTimeMillis(),
        state = state,
    )
}
