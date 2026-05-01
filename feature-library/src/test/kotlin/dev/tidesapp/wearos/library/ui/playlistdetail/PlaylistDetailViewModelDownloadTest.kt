package dev.tidesapp.wearos.library.ui.playlistdetail

import androidx.lifecycle.SavedStateHandle
import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.core.domain.playback.PlaybackControl
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.library.domain.repository.PlaylistRepository
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
class PlaylistDetailViewModelDownloadTest {

    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var playbackControl: PlaybackControl
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var downloadWorkScheduler: DownloadWorkScheduler
    private lateinit var viewModel: PlaylistDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testPlaylist = PlaylistItem(
        id = "playlist-1",
        title = "Test Playlist",
        description = "A test playlist",
        imageUrl = "https://img.tidal.com/playlist-1",
        numberOfTracks = 3,
        creator = "Test Creator",
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
        playlistRepository = mockk()
        playbackControl = mockk(relaxed = true)
        downloadWorkScheduler = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `DownloadPlaylist queues collection and enqueues work`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(emptyList())
            coEvery { queueCollectionDownload(any(), any(), any(), any(), any()) } returns Unit
        }
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)
        coEvery { downloadWorkScheduler.enqueueCollectionDownload(any(), any()) } returns Unit

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        viewModel.onEvent(PlaylistDetailUiEvent.DownloadPlaylist)
        advanceUntilIdle()

        coVerify {
            downloadRepository.queueCollectionDownload(
                "playlist-1", CollectionType.PLAYLIST, "Test Playlist", "https://img.tidal.com/playlist-1", testTracks,
            )
        }
        coVerify {
            downloadWorkScheduler.enqueueCollectionDownload("playlist-1", CollectionType.PLAYLIST)
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
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        viewModel.onEvent(PlaylistDetailUiEvent.RemoveDownload)
        advanceUntilIdle()

        coVerify { downloadRepository.deleteCollection("playlist-1", CollectionType.PLAYLIST) }
        verify { downloadWorkScheduler.cancelCollectionDownload("playlist-1") }
    }

    @Test
    fun `completed download state sets isDownloaded true`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(
                listOf(createDownloadedCollection(DownloadState.COMPLETED)),
            )
        }
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as PlaylistDetailUiState.Success
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
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as PlaylistDetailUiState.Success
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
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as PlaylistDetailUiState.Success
        assertFalse(state.isDownloaded)
        assertTrue(state.isDownloading)
    }

    @Test
    fun `no matching collection keeps defaults`() = runTest {
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns flowOf(emptyList())
        }
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as PlaylistDetailUiState.Success
        assertFalse(state.isDownloaded)
        assertFalse(state.isDownloading)
    }

    @Test
    fun `download state updates reactively when flow emits`() = runTest {
        val collectionsFlow = MutableStateFlow<List<DownloadedCollection>>(emptyList())
        downloadRepository = mockk {
            every { getDownloadedCollections() } returns collectionsFlow
        }
        coEvery { playlistRepository.getPlaylist("playlist-1") } returns Result.success(testPlaylist)
        coEvery { playlistRepository.getPlaylistTracks("playlist-1") } returns Result.success(testTracks)

        viewModel = PlaylistDetailViewModel(
            playlistRepository, playbackControl, downloadRepository, downloadWorkScheduler, SavedStateHandle(),
        )
        viewModel.onEvent(PlaylistDetailUiEvent.LoadPlaylistDetail("playlist-1"))
        advanceUntilIdle()

        var state = viewModel.uiState.value as PlaylistDetailUiState.Success
        assertFalse(state.isDownloaded)

        collectionsFlow.value = listOf(createDownloadedCollection(DownloadState.COMPLETED))
        advanceUntilIdle()

        state = viewModel.uiState.value as PlaylistDetailUiState.Success
        assertTrue(state.isDownloaded)
    }

    private fun createDownloadedCollection(state: DownloadState) = DownloadedCollection(
        id = "playlist-1",
        type = CollectionType.PLAYLIST,
        title = "Test Playlist",
        imageUrl = "https://img.tidal.com/playlist-1",
        trackCount = 3,
        downloadedTrackCount = if (state == DownloadState.COMPLETED) 3 else 0,
        totalSizeBytes = 0L,
        downloadedAt = System.currentTimeMillis(),
        state = state,
    )
}
