package dev.tidesapp.wearos.library.ui.playlists

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.library.domain.repository.PlaylistRepository
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
sealed interface PlaylistsUiState {
    data object Initial : PlaylistsUiState
    data object Loading : PlaylistsUiState
    data class Success(val playlists: ImmutableList<PlaylistItem>) : PlaylistsUiState
    data class Refreshing(val playlists: ImmutableList<PlaylistItem>) : PlaylistsUiState
    data class Error(val message: String) : PlaylistsUiState
}

@Immutable
sealed interface PlaylistsUiEvent {
    data object LoadPlaylists : PlaylistsUiEvent
    data object RefreshPlaylists : PlaylistsUiEvent
    data object RetryLoad : PlaylistsUiEvent
    data class PlaylistClicked(val playlist: PlaylistItem) : PlaylistsUiEvent
}

@Immutable
sealed interface PlaylistsUiEffect {
    data class NavigateToPlaylistDetail(val playlistId: String) : PlaylistsUiEffect
    data class ShowError(val message: String) : PlaylistsUiEffect
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaylistsUiState>(PlaylistsUiState.Initial)
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PlaylistsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: PlaylistsUiEvent) {
        when (event) {
            PlaylistsUiEvent.LoadPlaylists -> loadPlaylists(false)
            PlaylistsUiEvent.RefreshPlaylists -> loadPlaylists(true)
            PlaylistsUiEvent.RetryLoad -> loadPlaylists(true)
            is PlaylistsUiEvent.PlaylistClicked -> onPlaylistClicked(event.playlist)
        }
    }

    private fun loadPlaylists(forceRefresh: Boolean) {
        viewModelScope.launch {
            when (val currentState = _uiState.value) {
                is PlaylistsUiState.Loading,
                is PlaylistsUiState.Refreshing -> return@launch

                is PlaylistsUiState.Success -> {
                    if (forceRefresh) _uiState.value =
                        PlaylistsUiState.Refreshing(currentState.playlists)
                }

                is PlaylistsUiState.Error,
                is PlaylistsUiState.Initial -> _uiState.value = PlaylistsUiState.Loading
            }

            playlistRepository.getUserPlaylists(forceRefresh)
                .onSuccess { playlists ->
                    _uiState.value = PlaylistsUiState.Success(playlists.toImmutableList())
                }
                .onFailure { error ->
                    when (val currentState = _uiState.value) {
                        is PlaylistsUiState.Refreshing -> {
                            _uiState.value = PlaylistsUiState.Success(currentState.playlists)
                            _uiEffect.send(
                                PlaylistsUiEffect.ShowError(
                                    error.message ?: "Failed to refresh"
                                )
                            )
                        }

                        else -> {
                            _uiState.value = PlaylistsUiState.Error(
                                error.message ?: "Failed to load playlists"
                            )
                        }
                    }
                }
        }
    }

    private fun onPlaylistClicked(playlist: PlaylistItem) {
        viewModelScope.launch {
            _uiEffect.send(PlaylistsUiEffect.NavigateToPlaylistDetail(playlist.id))
        }
    }
}
