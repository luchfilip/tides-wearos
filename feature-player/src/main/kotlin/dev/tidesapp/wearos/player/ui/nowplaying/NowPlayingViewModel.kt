package dev.tidesapp.wearos.player.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.player.domain.repository.PlayerRepository
import dev.tidesapp.wearos.player.domain.repository.PlayerState
import dev.tidesapp.wearos.player.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NowPlayingUiState {
    data object Initial : NowPlayingUiState
    data object Loading : NowPlayingUiState
    data class Playing(
        val trackTitle: String,
        val artistName: String,
        val albumArtUrl: String?,
        val isPlaying: Boolean,
        val progressMs: Long,
        val durationMs: Long,
    ) : NowPlayingUiState

    data class Error(val message: String) : NowPlayingUiState
}

sealed interface NowPlayingUiEvent {
    data class PlayTrack(val trackId: String) : NowPlayingUiEvent
    data object ObservePlayerState : NowPlayingUiEvent
    data object PlayPause : NowPlayingUiEvent
    data object SkipNext : NowPlayingUiEvent
    data object SkipPrevious : NowPlayingUiEvent
    data class SeekTo(val positionMs: Long) : NowPlayingUiEvent
}

sealed interface NowPlayingUiEffect {
    data object NavigateBack : NowPlayingUiEffect
    data class ShowError(val message: String) : NowPlayingUiEffect
}

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val _uiEffect = Channel<NowPlayingUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _isResolving = MutableStateFlow(false)
    private val _resolveError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NowPlayingUiState> = combine(
        playerRepository.playerState,
        _isResolving,
        _resolveError,
    ) { playerState, resolving, error ->
        when {
            error != null -> NowPlayingUiState.Error(error)
            resolving -> NowPlayingUiState.Loading
            else -> playerState.toUiState()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NowPlayingUiState.Initial)

    fun onEvent(event: NowPlayingUiEvent) {
        when (event) {
            is NowPlayingUiEvent.PlayTrack -> playTrack(event.trackId)
            NowPlayingUiEvent.ObservePlayerState -> observePlayerState()
            is NowPlayingUiEvent.PlayPause -> playerRepository.playPause()
            is NowPlayingUiEvent.SkipNext -> playerRepository.skipNext()
            is NowPlayingUiEvent.SkipPrevious -> playerRepository.skipPrevious()
            is NowPlayingUiEvent.SeekTo -> playerRepository.seekTo(event.positionMs)
        }
    }

    private fun playTrack(trackId: String) {
        viewModelScope.launch {
            _isResolving.value = true
            _resolveError.value = null
            try {
                playbackController.playTrack(trackId)
            } catch (e: Exception) {
                val message = e.message ?: "Failed to play track"
                _resolveError.value = message
                _uiEffect.send(NowPlayingUiEffect.ShowError(message))
            } finally {
                _isResolving.value = false
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerRepository.playerState.collect { state ->
                if (state.error != null) {
                    _uiEffect.send(NowPlayingUiEffect.ShowError(state.error))
                }
            }
        }
    }

    private fun PlayerState.toUiState(): NowPlayingUiState {
        if (error != null) return NowPlayingUiState.Error(error)
        val track = this.track ?: return NowPlayingUiState.Initial
        return NowPlayingUiState.Playing(
            trackTitle = track.title,
            artistName = track.artistName,
            albumArtUrl = track.imageUrl,
            isPlaying = isPlaying,
            progressMs = currentPositionMs,
            durationMs = durationMs,
        )
    }
}
