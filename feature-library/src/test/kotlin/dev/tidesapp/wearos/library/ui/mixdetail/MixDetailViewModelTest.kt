package dev.tidesapp.wearos.library.ui.mixdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.library.domain.repository.MixRepository
import io.mockk.coEvery
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
class MixDetailViewModelTest {

    private lateinit var repository: MixRepository
    private lateinit var viewModel: MixDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val savedStateHandle = SavedStateHandle(
        mapOf(
            MixDetailViewModel.ARG_MIX_ID to "mix-abc",
            MixDetailViewModel.ARG_TITLE to "Track Radio",
            MixDetailViewModel.ARG_SUBTITLE to "What Else Is There?",
            MixDetailViewModel.ARG_IMAGE_URL to "https://images.tidal.com/mix.jpg",
        ),
    )

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
    fun `LoadMixDetail successfully loads header and tracks`() = runTest {
        val tracks = createTestTracks(3)
        coEvery { repository.getMixItems("mix-abc") } returns Result.success(tracks)

        viewModel = MixDetailViewModel(repository, savedStateHandle)
        viewModel.onEvent(MixDetailUiEvent.LoadMixDetail)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MixDetailUiState.Success)
        val success = state as MixDetailUiState.Success
        assertEquals("mix-abc", success.header.id)
        assertEquals("Track Radio", success.header.title)
        assertEquals("What Else Is There?", success.header.subTitle)
        assertEquals("https://images.tidal.com/mix.jpg", success.header.imageUrl)
        assertEquals(3, success.tracks.size)
    }

    @Test
    fun `LoadMixDetail failure shows Error state`() = runTest {
        coEvery { repository.getMixItems("mix-abc") } returns
            Result.failure(RuntimeException("boom"))

        viewModel = MixDetailViewModel(repository, savedStateHandle)
        viewModel.onEvent(MixDetailUiEvent.LoadMixDetail)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MixDetailUiState.Error)
        assertEquals("boom", (state as MixDetailUiState.Error).message)
    }

    @Test
    fun `PlayTrack emits NavigateToNowPlaying with that track id`() = runTest {
        val tracks = createTestTracks(3)
        coEvery { repository.getMixItems("mix-abc") } returns Result.success(tracks)

        viewModel = MixDetailViewModel(repository, savedStateHandle)
        viewModel.onEvent(MixDetailUiEvent.LoadMixDetail)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(MixDetailUiEvent.PlayTrack(tracks[2]))
            val effect = awaitItem()
            assertTrue(effect is MixDetailUiEffect.NavigateToNowPlaying)
            assertEquals(
                "track-2",
                (effect as MixDetailUiEffect.NavigateToNowPlaying).trackId,
            )
        }
    }

    @Test
    fun `PlayAll emits NavigateToNowPlaying with first track`() = runTest {
        val tracks = createTestTracks(3)
        coEvery { repository.getMixItems("mix-abc") } returns Result.success(tracks)

        viewModel = MixDetailViewModel(repository, savedStateHandle)
        viewModel.onEvent(MixDetailUiEvent.LoadMixDetail)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(MixDetailUiEvent.PlayAll)
            val effect = awaitItem()
            assertTrue(effect is MixDetailUiEffect.NavigateToNowPlaying)
            assertEquals(
                "track-0",
                (effect as MixDetailUiEffect.NavigateToNowPlaying).trackId,
            )
        }
    }

    private fun createTestTracks(count: Int): List<TrackItem> = List(count) { index ->
        TrackItem(
            id = "track-$index",
            title = "Track $index",
            artistName = "Test Artist",
            albumTitle = "Test Album",
            duration = 200 + index * 30,
            trackNumber = index + 1,
            imageUrl = null,
        )
    }
}
