package dev.tidesapp.wearos.library.ui.playlistdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
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
sealed interface PlaylistDetailUiState {
    data object Initial : PlaylistDetailUiState
    data object Loading : PlaylistDetailUiState
    data class Success(
        val playlist: PlaylistItem,
        val tracks: ImmutableList<TrackItem>,
    ) : PlaylistDetailUiState

    data class Error(val message: String) : PlaylistDetailUiState
}

@Immutable
sealed interface PlaylistDetailUiEvent {
    data class LoadPlaylistDetail(val playlistId: String) : PlaylistDetailUiEvent
    data object PlayAll : PlaylistDetailUiEvent
    data object ShufflePlay : PlaylistDetailUiEvent
    data class PlayTrack(val track: TrackItem) : PlaylistDetailUiEvent
}

@Immutable
sealed interface PlaylistDetailUiEffect {
    data class NavigateToNowPlaying(val trackId: String) : PlaylistDetailUiEffect
    data class ShowError(val message: String) : PlaylistDetailUiEffect
}

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<PlaylistDetailUiState>(PlaylistDetailUiState.Initial)
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PlaylistDetailUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: PlaylistDetailUiEvent) {
        when (event) {
            is PlaylistDetailUiEvent.LoadPlaylistDetail -> loadPlaylistDetail(event.playlistId)
            PlaylistDetailUiEvent.PlayAll -> playAll()
            PlaylistDetailUiEvent.ShufflePlay -> shufflePlay()
            is PlaylistDetailUiEvent.PlayTrack -> playTrack(event.track)
        }
    }

    private fun loadPlaylistDetail(playlistId: String) {
        viewModelScope.launch {
            if (_uiState.value is PlaylistDetailUiState.Loading) return@launch

            _uiState.value = PlaylistDetailUiState.Loading

            val playlistResult = playlistRepository.getPlaylist(playlistId)
            val tracksResult = playlistRepository.getPlaylistTracks(playlistId)

            val playlist = playlistResult.getOrNull()
            val tracks = tracksResult.getOrNull()

            if (playlist != null && tracks != null) {
                _uiState.value = PlaylistDetailUiState.Success(
                    playlist = playlist,
                    tracks = tracks.toImmutableList(),
                )
            } else {
                _uiState.value = PlaylistDetailUiState.Error(
                    playlistResult.exceptionOrNull()?.message
                        ?: tracksResult.exceptionOrNull()?.message
                        ?: "Failed to load playlist"
                )
            }
        }
    }

    private fun playAll() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is PlaylistDetailUiState.Success && state.tracks.isNotEmpty()) {
                _uiEffect.send(
                    PlaylistDetailUiEffect.NavigateToNowPlaying(state.tracks.first().id)
                )
            }
        }
    }

    private fun shufflePlay() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is PlaylistDetailUiState.Success && state.tracks.isNotEmpty()) {
                val randomTrack = state.tracks.random()
                _uiEffect.send(
                    PlaylistDetailUiEffect.NavigateToNowPlaying(randomTrack.id)
                )
            }
        }
    }

    private fun playTrack(track: TrackItem) {
        viewModelScope.launch {
            _uiEffect.send(PlaylistDetailUiEffect.NavigateToNowPlaying(track.id))
        }
    }
}
