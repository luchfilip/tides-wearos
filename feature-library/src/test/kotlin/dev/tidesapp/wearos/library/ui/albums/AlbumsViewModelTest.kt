package dev.tidesapp.wearos.library.ui.albums

import app.cash.turbine.test
import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.library.domain.repository.AlbumRepository
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
class AlbumsViewModelTest {

    private lateinit var repository: AlbumRepository
    private lateinit var viewModel: AlbumsViewModel
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
        viewModel = AlbumsViewModel(repository)
        assertEquals(AlbumsUiState.Initial, viewModel.uiState.value)
    }

    @Test
    fun `LoadAlbums successfully loads albums`() = runTest {
        val albums = createTestAlbums(3)
        coEvery { repository.getUserAlbums(false) } returns Result.success(albums)

        viewModel = AlbumsViewModel(repository)
        viewModel.onEvent(AlbumsUiEvent.LoadAlbums)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AlbumsUiState.Success)
        assertEquals(3, (state as AlbumsUiState.Success).albums.size)
        coVerify(exactly = 1) { repository.getUserAlbums(false) }
    }

    @Test
    fun `LoadAlbums failure shows Error state`() = runTest {
        coEvery { repository.getUserAlbums(false) } returns Result.failure(
            RuntimeException("Network error")
        )

        viewModel = AlbumsViewModel(repository)
        viewModel.onEvent(AlbumsUiEvent.LoadAlbums)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AlbumsUiState.Error)
        assertEquals("Network error", (state as AlbumsUiState.Error).message)
    }

    @Test
    fun `RefreshAlbums failure preserves existing data`() = runTest {
        val albums = createTestAlbums(2)
        coEvery { repository.getUserAlbums(false) } returns Result.success(albums)
        coEvery { repository.getUserAlbums(true) } returns Result.failure(
            RuntimeException("Refresh failed")
        )

        viewModel = AlbumsViewModel(repository)
        viewModel.onEvent(AlbumsUiEvent.LoadAlbums)
        advanceUntilIdle()

        viewModel.onEvent(AlbumsUiEvent.RefreshAlbums)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AlbumsUiState.Success)
        assertEquals(2, (state as AlbumsUiState.Success).albums.size)
    }

    @Test
    fun `AlbumClicked emits NavigateToAlbumDetail effect`() = runTest {
        val albums = createTestAlbums(1)
        coEvery { repository.getUserAlbums(false) } returns Result.success(albums)

        viewModel = AlbumsViewModel(repository)
        viewModel.onEvent(AlbumsUiEvent.LoadAlbums)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(AlbumsUiEvent.AlbumClicked(albums.first()))
            val effect = awaitItem()
            assertTrue(effect is AlbumsUiEffect.NavigateToAlbumDetail)
            assertEquals("album-0", (effect as AlbumsUiEffect.NavigateToAlbumDetail).albumId)
        }
    }

    @Test
    fun `RetryLoad from Error state succeeds`() = runTest {
        coEvery { repository.getUserAlbums(any()) } returns Result.failure(
            RuntimeException("Error")
        ) andThen Result.success(createTestAlbums(1))

        viewModel = AlbumsViewModel(repository)
        viewModel.onEvent(AlbumsUiEvent.LoadAlbums)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AlbumsUiState.Error)

        viewModel.onEvent(AlbumsUiEvent.RetryLoad)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AlbumsUiState.Success)
    }

    private fun createTestAlbums(count: Int): List<AlbumItem> {
        return List(count) { index ->
            AlbumItem(
                id = "album-$index",
                title = "Album $index",
                artistName = "Artist $index",
                imageUrl = "https://img.tidal.com/album-$index",
                releaseDate = "2024-01-01",
                numberOfTracks = 10,
            )
        }
    }
}
