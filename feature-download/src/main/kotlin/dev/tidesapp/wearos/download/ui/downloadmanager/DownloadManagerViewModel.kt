package dev.tidesapp.wearos.download.ui.downloadmanager

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.download.domain.repository.DownloadStorageRepository
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
sealed interface DownloadManagerUiState {
    data object Loading : DownloadManagerUiState
    data class Loaded(
        val collections: ImmutableList<DownloadedCollection>,
        val storageInfo: StorageInfo,
    ) : DownloadManagerUiState
}

@Immutable
sealed interface DownloadManagerUiEvent {
    data class DeleteCollection(val collection: DownloadedCollection) : DownloadManagerUiEvent
    data object ClearAll : DownloadManagerUiEvent
}

@Immutable
sealed interface DownloadManagerUiEffect {
    data class ShowConfirmDelete(val collectionTitle: String) : DownloadManagerUiEffect
}

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadStorageRepository: DownloadStorageRepository,
    private val downloadWorkScheduler: DownloadWorkScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DownloadManagerUiState>(DownloadManagerUiState.Loading)
    val uiState: StateFlow<DownloadManagerUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DownloadManagerUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeDownloads()
    }

    fun onEvent(event: DownloadManagerUiEvent) {
        when (event) {
            is DownloadManagerUiEvent.DeleteCollection -> deleteCollection(event.collection)
            DownloadManagerUiEvent.ClearAll -> clearAll()
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            combine(
                downloadRepository.getDownloadedCollections(),
                downloadStorageRepository.getStorageInfo(),
            ) { collections, storageInfo ->
                DownloadManagerUiState.Loaded(
                    collections = collections.toImmutableList(),
                    storageInfo = storageInfo,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun deleteCollection(collection: DownloadedCollection) {
        viewModelScope.launch {
            downloadWorkScheduler.cancelCollectionDownload(collection.id)
            downloadRepository.deleteCollection(collection.id, collection.type)
        }
    }

    private fun clearAll() {
        viewModelScope.launch {
            downloadWorkScheduler.cancelAll()
            downloadStorageRepository.clearAllDownloads()
        }
    }
}
