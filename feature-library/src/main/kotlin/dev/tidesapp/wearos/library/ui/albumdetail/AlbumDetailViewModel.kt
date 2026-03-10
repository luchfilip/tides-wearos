package dev.tidesapp.wearos.library.ui.albumdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
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
sealed interface AlbumDetailUiState {
    data object Initial : AlbumDetailUiState
    data object Loading : AlbumDetailUiState
    data class Success(
        val album: AlbumItem,
        val tracks: ImmutableList<TrackItem>,
    ) : AlbumDetailUiState

    data class Error(val message: String) : AlbumDetailUiState
}

@Immutable
sealed interface AlbumDetailUiEvent {
    data class LoadAlbumDetail(val albumId: String) : AlbumDetailUiEvent
    data object PlayAll : AlbumDetailUiEvent
    data class PlayTrack(val track: TrackItem) : AlbumDetailUiEvent
}

@Immutable
sealed interface AlbumDetailUiEffect {
    data class NavigateToNowPlaying(val trackId: String) : AlbumDetailUiEffect
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Initial)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AlbumDetailUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: AlbumDetailUiEvent) {
        when (event) {
            is AlbumDetailUiEvent.LoadAlbumDetail -> loadAlbumDetail(event.albumId)
            AlbumDetailUiEvent.PlayAll -> playAll()
            is AlbumDetailUiEvent.PlayTrack -> playTrack(event.track)
        }
    }

    private fun loadAlbumDetail(albumId: String) {
        viewModelScope.launch {
            if (_uiState.value is AlbumDetailUiState.Loading) return@launch

            _uiState.value = AlbumDetailUiState.Loading

            val albumResult = albumRepository.getAlbumDetail(albumId)
            val tracksResult = albumRepository.getAlbumTracks(albumId)

            val album = albumResult.getOrNull()
            val tracks = tracksResult.getOrNull()

            if (album != null && tracks != null) {
                _uiState.value = AlbumDetailUiState.Success(
                    album = album,
                    tracks = tracks.toImmutableList(),
                )
            } else {
                _uiState.value = AlbumDetailUiState.Error(
                    albumResult.exceptionOrNull()?.message
                        ?: tracksResult.exceptionOrNull()?.message
                        ?: "Failed to load album"
                )
            }
        }
    }

    private fun playAll() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is AlbumDetailUiState.Success && state.tracks.isNotEmpty()) {
                _uiEffect.send(
                    AlbumDetailUiEffect.NavigateToNowPlaying(state.tracks.first().id)
                )
            }
        }
    }

    private fun playTrack(track: TrackItem) {
        viewModelScope.launch {
            _uiEffect.send(AlbumDetailUiEffect.NavigateToNowPlaying(track.id))
        }
    }
}
