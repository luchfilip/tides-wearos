package dev.tidesapp.wearos.library.ui.albums

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.library.domain.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
sealed interface AlbumsUiState {
    data object Initial : AlbumsUiState
    data object Loading : AlbumsUiState
    data class Success(val albums: ImmutableList<AlbumItem>) : AlbumsUiState
    data class Refreshing(val albums: ImmutableList<AlbumItem>) : AlbumsUiState
    data class Error(val message: String) : AlbumsUiState
}

@Immutable
sealed interface AlbumsUiEvent {
    data object LoadAlbums : AlbumsUiEvent
    data object RefreshAlbums : AlbumsUiEvent
    data object RetryLoad : AlbumsUiEvent
    data class AlbumClicked(val album: AlbumItem) : AlbumsUiEvent
}

@Immutable
sealed interface AlbumsUiEffect {
    data class NavigateToAlbumDetail(val albumId: String) : AlbumsUiEffect
    data class ShowError(val message: String) : AlbumsUiEffect
}

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Initial)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AlbumsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: AlbumsUiEvent) {
        when (event) {
            AlbumsUiEvent.LoadAlbums -> loadAlbums(false)
            AlbumsUiEvent.RefreshAlbums -> loadAlbums(true)
            AlbumsUiEvent.RetryLoad -> loadAlbums(true)
            is AlbumsUiEvent.AlbumClicked -> onAlbumClicked(event.album)
        }
    }

    private fun loadAlbums(forceRefresh: Boolean) {
        viewModelScope.launch {
            when (val currentState = _uiState.value) {
                is AlbumsUiState.Loading,
                is AlbumsUiState.Refreshing -> return@launch

                is AlbumsUiState.Success -> {
                    if (forceRefresh) _uiState.value =
                        AlbumsUiState.Refreshing(currentState.albums)
                }

                is AlbumsUiState.Error,
                is AlbumsUiState.Initial -> _uiState.value = AlbumsUiState.Loading
            }

            albumRepository.getUserAlbums(forceRefresh)
                .onSuccess { albums ->
                    _uiState.value = AlbumsUiState.Success(albums.toImmutableList())
                }
                .onFailure { error ->
                    when (val currentState = _uiState.value) {
                        is AlbumsUiState.Refreshing -> {
                            _uiState.value = AlbumsUiState.Success(currentState.albums)
                            _uiEffect.send(
                                AlbumsUiEffect.ShowError(
                                    error.message ?: "Failed to refresh"
                                )
                            )
                        }

                        else -> {
                            _uiState.value = AlbumsUiState.Error(
                                error.message ?: "Failed to load albums"
                            )
                        }
                    }
                }
        }
    }

    private fun onAlbumClicked(album: AlbumItem) {
        viewModelScope.launch {
            _uiEffect.send(AlbumsUiEffect.NavigateToAlbumDetail(album.id))
        }
    }
}
