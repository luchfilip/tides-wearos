package dev.tidesapp.wearos.library.ui.playlists

import app.cash.turbine.test
import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.library.domain.repository.PlaylistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsViewModelTest {

    private lateinit var repository: PlaylistRepository
    private lateinit var viewModel: PlaylistsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Initial`() {
        viewModel = PlaylistsViewModel(repository)
        assertEquals(PlaylistsUiState.Initial, viewModel.uiState.value)
    }

    @Test
    fun `LoadPlaylists successfully loads playlists`() = runTest {
        val playlists = createTestPlaylists(3)
        coEvery { repository.getUserPlaylists(false) } returns Result.success(playlists)

        viewModel = PlaylistsViewModel(repository)
        viewModel.onEvent(PlaylistsUiEvent.LoadPlaylists)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PlaylistsUiState.Success)
        assertEquals(3, (state as PlaylistsUiState.Success).playlists.size)
        coVerify(exactly = 1) { repository.getUserPlaylists(false) }
    }

    @Test
    fun `LoadPlaylists failure shows Error state`() = runTest {
        coEvery { repository.getUserPlaylists(false) } returns Result.failure(
            RuntimeException("Network error")
        )

        viewModel = PlaylistsViewModel(repository)
        viewModel.onEvent(PlaylistsUiEvent.LoadPlaylists)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PlaylistsUiState.Error)
        assertEquals("Network error", (state as PlaylistsUiState.Error).message)
    }

    @Test
    fun `RefreshPlaylists failure preserves existing data`() = runTest {
        val playlists = createTestPlaylists(2)
        coEvery { repository.getUserPlaylists(false) } returns Result.success(playlists)
        coEvery { repository.getUserPlaylists(true) } returns Result.failure(
            RuntimeException("Refresh failed")
        )

        viewModel = PlaylistsViewModel(repository)
        viewModel.onEvent(PlaylistsUiEvent.LoadPlaylists)
        advanceUntilIdle()

        viewModel.onEvent(PlaylistsUiEvent.RefreshPlaylists)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PlaylistsUiState.Success)
        assertEquals(2, (state as PlaylistsUiState.Success).playlists.size)
    }

    @Test
    fun `PlaylistClicked emits NavigateToPlaylistDetail effect`() = runTest {
        val playlists = createTestPlaylists(1)
        coEvery { repository.getUserPlaylists(false) } returns Result.success(playlists)

        viewModel = PlaylistsViewModel(repository)
        viewModel.onEvent(PlaylistsUiEvent.LoadPlaylists)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(PlaylistsUiEvent.PlaylistClicked(playlists.first()))
            val effect = awaitItem()
            assertTrue(effect is PlaylistsUiEffect.NavigateToPlaylistDetail)
            assertEquals(
                "playlist-0",
                (effect as PlaylistsUiEffect.NavigateToPlaylistDetail).playlistId
            )
        }
    }

    @Test
    fun `RetryLoad from Error state succeeds`() = runTest {
        coEvery { repository.getUserPlaylists(any()) } returns Result.failure(
            RuntimeException("Error")
        ) andThen Result.success(createTestPlaylists(1))

        viewModel = PlaylistsViewModel(repository)
        viewModel.onEvent(PlaylistsUiEvent.LoadPlaylists)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PlaylistsUiState.Error)

        viewModel.onEvent(PlaylistsUiEvent.RetryLoad)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PlaylistsUiState.Success)
    }

    private fun createTestPlaylists(count: Int): List<PlaylistItem> {
        return List(count) { index ->
            PlaylistItem(
                id = "playlist-$index",
                title = "Playlist $index",
                description = "Description $index",
                imageUrl = "https://img.tidal.com/playlist-$index",
                numberOfTracks = 10 + index,
                creator = "Creator $index",
            )
        }
    }
}
