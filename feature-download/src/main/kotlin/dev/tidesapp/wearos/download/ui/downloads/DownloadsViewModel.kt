package dev.tidesapp.wearos.download.ui.downloads

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadedCollection
import dev.tidesapp.wearos.download.domain.model.StorageInfo
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
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
sealed interface DownloadsUiState {
    data object Loading : DownloadsUiState
    data class Success(
        val collections: ImmutableList<DownloadedCollection>,
        val storageInfo: StorageInfo,
    ) : DownloadsUiState
    data class Empty(val storageInfo: StorageInfo) : DownloadsUiState
}

@Immutable
sealed interface DownloadsUiEvent {
    data class CollectionClicked(val collection: DownloadedCollection) : DownloadsUiEvent
    data object ManageStorageClicked : DownloadsUiEvent
}

@Immutable
sealed interface DownloadsUiEffect {
    data class NavigateToCollection(
        val collectionId: String,
        val type: CollectionType,
    ) : DownloadsUiEffect
    data object NavigateToDownloadManager : DownloadsUiEffect
}

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DownloadsUiState>(DownloadsUiState.Loading)
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DownloadsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeDownloads()
    }

    fun onEvent(event: DownloadsUiEvent) {
        when (event) {
            is DownloadsUiEvent.CollectionClicked -> onCollectionClicked(event.collection)
            DownloadsUiEvent.ManageStorageClicked -> onManageStorageClicked()
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            combine(
                downloadRepository.getDownloadedCollections(),
                downloadRepository.getStorageInfo(),
            ) { collections, storageInfo ->
                if (collections.isEmpty()) {
                    DownloadsUiState.Empty(storageInfo)
                } else {
                    DownloadsUiState.Success(
                        collections = collections.toImmutableList(),
                        storageInfo = storageInfo,
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun onCollectionClicked(collection: DownloadedCollection) {
        viewModelScope.launch {
            _uiEffect.send(
                DownloadsUiEffect.NavigateToCollection(
                    collectionId = collection.id,
                    type = collection.type,
                ),
            )
        }
    }

    private fun onManageStorageClicked() {
        viewModelScope.launch {
            _uiEffect.send(DownloadsUiEffect.NavigateToDownloadManager)
        }
    }
}
