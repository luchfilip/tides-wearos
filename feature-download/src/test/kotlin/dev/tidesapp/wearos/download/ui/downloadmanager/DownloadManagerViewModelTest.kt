package dev.tidesapp.wearos.download.ui.downloadmanager

import app.cash.turbine.test
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class DownloadManagerViewModelTest {

    private lateinit var downloadRepository: DownloadRepository
    private lateinit var downloadStorageRepository: DownloadStorageRepository
    private lateinit var downloadWorkScheduler: DownloadWorkScheduler
    private lateinit var viewModel: DownloadManagerViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val collectionsFlow = MutableStateFlow<List<DownloadedCollection>>(emptyList())
    private val storageInfoFlow = MutableStateFlow(StorageInfo(0L, 500L * 1024 * 1024, 0))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        downloadRepository = mockk(relaxUnitFun = true)
        downloadStorageRepository = mockk(relaxUnitFun = true)
        downloadWorkScheduler = mockk(relaxUnitFun = true)

        every { downloadRepository.getDownloadedCollections() } returns collectionsFlow
        every { downloadStorageRepository.getStorageInfo() } returns storageInfoFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        viewModel = createViewModel()
        assertEquals(DownloadManagerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `observes collections and storage info into Loaded state`() = runTest {
        val collections = createTestCollections(2)
        collectionsFlow.value = collections
        storageInfoFlow.value = StorageInfo(100L * 1024 * 1024, 500L * 1024 * 1024, 20)

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DownloadManagerUiState.Loaded)
        val loaded = state as DownloadManagerUiState.Loaded
        assertEquals(2, loaded.collections.size)
        assertEquals(100L * 1024 * 1024, loaded.storageInfo.usedBytes)
    }

    @Test
    fun `DeleteCollection cancels work and deletes from repository`() = runTest {
        val collections = createTestCollections(1)
        collectionsFlow.value = collections

        viewModel = createViewModel()
        advanceUntilIdle()

        val collection = collections.first()
        viewModel.onEvent(DownloadManagerUiEvent.DeleteCollection(collection))
        advanceUntilIdle()

        verify { downloadWorkScheduler.cancelCollectionDownload("collection-0") }
        coVerify { downloadRepository.deleteCollection("collection-0", CollectionType.ALBUM) }
    }

    @Test
    fun `ClearAll cancels all work and clears storage`() = runTest {
        collectionsFlow.value = createTestCollections(3)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DownloadManagerUiEvent.ClearAll)
        advanceUntilIdle()

        verify { downloadWorkScheduler.cancelAll() }
        coVerify { downloadStorageRepository.clearAllDownloads() }
    }

    @Test
    fun `state updates reactively when collections change`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val loaded = viewModel.uiState.value as DownloadManagerUiState.Loaded
        assertTrue(loaded.collections.isEmpty())

        collectionsFlow.value = createTestCollections(2)
        advanceUntilIdle()

        val updated = viewModel.uiState.value as DownloadManagerUiState.Loaded
        assertEquals(2, updated.collections.size)
    }

    @Test
    fun `empty collections still emits Loaded state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DownloadManagerUiState.Loaded)
        assertTrue((state as DownloadManagerUiState.Loaded).collections.isEmpty())
    }

    private fun createViewModel() = DownloadManagerViewModel(
        downloadRepository = downloadRepository,
        downloadStorageRepository = downloadStorageRepository,
        downloadWorkScheduler = downloadWorkScheduler,
    )

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
