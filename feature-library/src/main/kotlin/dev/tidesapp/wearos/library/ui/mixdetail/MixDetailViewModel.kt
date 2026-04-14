package dev.tidesapp.wearos.library.ui.mixdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.library.domain.repository.MixRepository
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
data class MixHeader(
    val id: String,
    val title: String,
    val subTitle: String?,
    val imageUrl: String?,
)

@Immutable
sealed interface MixDetailUiState {
    data object Initial : MixDetailUiState
    data object Loading : MixDetailUiState
    data class Success(
        val header: MixHeader,
        val tracks: ImmutableList<TrackItem>,
    ) : MixDetailUiState

    data class Error(val message: String) : MixDetailUiState
}

@Immutable
sealed interface MixDetailUiEvent {
    data object LoadMixDetail : MixDetailUiEvent
    data object PlayAll : MixDetailUiEvent
    data object ShufflePlay : MixDetailUiEvent
    data class PlayTrack(val track: TrackItem) : MixDetailUiEvent
}

@Immutable
sealed interface MixDetailUiEffect {
    data class NavigateToNowPlaying(val trackId: String) : MixDetailUiEffect
}

@HiltViewModel
class MixDetailViewModel @Inject constructor(
    private val mixRepository: MixRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mixId: String = savedStateHandle.get<String>(ARG_MIX_ID).orEmpty()
    private val mixTitle: String = savedStateHandle.get<String>(ARG_TITLE).orEmpty()
    private val mixSubTitle: String? = savedStateHandle.get<String>(ARG_SUBTITLE)
    private val mixImageUrl: String? = savedStateHandle.get<String>(ARG_IMAGE_URL)

    private val header = MixHeader(
        id = mixId,
        title = mixTitle.ifBlank { "Mix" },
        subTitle = mixSubTitle?.takeIf { it.isNotBlank() },
        imageUrl = mixImageUrl?.takeIf { it.isNotBlank() },
    )

    private val _uiState = MutableStateFlow<MixDetailUiState>(MixDetailUiState.Initial)
    val uiState: StateFlow<MixDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MixDetailUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: MixDetailUiEvent) {
        when (event) {
            MixDetailUiEvent.LoadMixDetail -> loadMixDetail()
            MixDetailUiEvent.PlayAll -> playAll()
            MixDetailUiEvent.ShufflePlay -> shufflePlay()
            is MixDetailUiEvent.PlayTrack -> playTrack(event.track)
        }
    }

    private fun loadMixDetail() {
        viewModelScope.launch {
            if (_uiState.value is MixDetailUiState.Loading) return@launch
            _uiState.value = MixDetailUiState.Loading

            mixRepository.getMixItems(mixId)
                .onSuccess { tracks ->
                    _uiState.value = MixDetailUiState.Success(
                        header = header,
                        tracks = tracks.toImmutableList(),
                    )
                }
                .onFailure { error ->
                    _uiState.value = MixDetailUiState.Error(
                        message = error.message ?: "Failed to load mix",
                    )
                }
        }
    }

    private fun playAll() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is MixDetailUiState.Success && state.tracks.isNotEmpty()) {
                _uiEffect.send(MixDetailUiEffect.NavigateToNowPlaying(state.tracks.first().id))
            }
        }
    }

    private fun shufflePlay() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is MixDetailUiState.Success && state.tracks.isNotEmpty()) {
                _uiEffect.send(
                    MixDetailUiEffect.NavigateToNowPlaying(state.tracks.random().id),
                )
            }
        }
    }

    private fun playTrack(track: TrackItem) {
        viewModelScope.launch {
            _uiEffect.send(MixDetailUiEffect.NavigateToNowPlaying(track.id))
        }
    }

    companion object {
        const val ARG_MIX_ID = "mixId"
        const val ARG_TITLE = "title"
        const val ARG_SUBTITLE = "subTitle"
        const val ARG_IMAGE_URL = "imageUrl"
    }
}
