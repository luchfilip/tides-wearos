package dev.tidesapp.wearos.download.ui.downloads

import app.cash.turbine.test
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class DownloadsViewModelTest {

    private lateinit var repository: DownloadRepository
    private lateinit var viewModel: DownloadsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val collectionsFlow = MutableStateFlow<List<DownloadedCollection>>(emptyList())
    private val storageInfoFlow = MutableStateFlow(StorageInfo(0L, 500L * 1024 * 1024, 0))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.getDownloadedCollections() } returns collectionsFlow
        every { repository.getStorageInfo() } returns storageInfoFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        viewModel = DownloadsViewModel(repository)
        assertEquals(DownloadsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty collections emits Empty state`() = runTest {
        viewModel = DownloadsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DownloadsUiState.Empty)
        assertEquals(0L, (state as DownloadsUiState.Empty).storageInfo.usedBytes)
    }

    @Test
    fun `non-empty collections emits Success state`() = runTest {
        collectionsFlow.value = createTestCollections(3)

        viewModel = DownloadsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DownloadsUiState.Success)
        assertEquals(3, (state as DownloadsUiState.Success).collections.size)
    }

    @Test
    fun `state updates when collections change`() = runTest {
        viewModel = DownloadsViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DownloadsUiState.Empty)

        collectionsFlow.value = createTestCollections(2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DownloadsUiState.Success)
        assertEquals(2, (state as DownloadsUiState.Success).collections.size)
    }

    @Test
    fun `CollectionClicked emits NavigateToCollection effect`() = runTest {
        collectionsFlow.value = createTestCollections(1)

        viewModel = DownloadsViewModel(repository)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(
                DownloadsUiEvent.CollectionClicked(collectionsFlow.value.first()),
            )
            val effect = awaitItem()
            assertTrue(effect is DownloadsUiEffect.NavigateToCollection)
            val nav = effect as DownloadsUiEffect.NavigateToCollection
            assertEquals("collection-0", nav.collectionId)
            assertEquals(CollectionType.ALBUM, nav.type)
        }
    }

    @Test
    fun `ManageStorageClicked emits NavigateToDownloadManager effect`() = runTest {
        viewModel = DownloadsViewModel(repository)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DownloadsUiEvent.ManageStorageClicked)
            val effect = awaitItem()
            assertTrue(effect is DownloadsUiEffect.NavigateToDownloadManager)
        }
    }

    @Test
    fun `Success state includes storage info`() = runTest {
        val storageInfo = StorageInfo(100L * 1024 * 1024, 500L * 1024 * 1024, 10)
        storageInfoFlow.value = storageInfo
        collectionsFlow.value = createTestCollections(1)

        viewModel = DownloadsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value as DownloadsUiState.Success
        assertEquals(storageInfo, state.storageInfo)
    }

    private fun createTestCollections(count: Int): List<DownloadedCollection> {
        return List(count) { index ->
            DownloadedCollection(
                id = "collection-$index",
                type = CollectionType.ALBUM,
                title = "Collection $index",
                imageUrl = "https://img.tidal.com/collection-$index",
                trackCount = 10,
                downloadedTrackCount = 10,
                totalSizeBytes = 50L * 1024 * 1024,
                downloadedAt = System.currentTimeMillis(),
                state = DownloadState.COMPLETED,
            )
        }
    }
}
