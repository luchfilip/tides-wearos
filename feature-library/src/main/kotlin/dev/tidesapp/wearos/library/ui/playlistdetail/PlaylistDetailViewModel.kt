package dev.tidesapp.wearos.library.ui.playlistdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.core.domain.playback.PlaybackControl
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.library.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        val isDownloaded: Boolean = false,
        val isDownloading: Boolean = false,
    ) : PlaylistDetailUiState

    data class Error(val message: String) : PlaylistDetailUiState
}

@Immutable
sealed interface PlaylistDetailUiEvent {
    data class LoadPlaylistDetail(val playlistId: String) : PlaylistDetailUiEvent
    data object PlayAll : PlaylistDetailUiEvent
    data object ShufflePlay : PlaylistDetailUiEvent
    data class PlayTrack(val track: TrackItem) : PlaylistDetailUiEvent
    data object DownloadPlaylist : PlaylistDetailUiEvent
    data object RemoveDownload : PlaylistDetailUiEvent
}

@Immutable
sealed interface PlaylistDetailUiEffect {
    data object NavigateToNowPlaying : PlaylistDetailUiEffect
    data class ShowError(val message: String) : PlaylistDetailUiEffect
}

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playbackControl: PlaybackControl,
    private val downloadRepository: DownloadRepository,
    private val downloadWorkScheduler: DownloadWorkScheduler,
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
            PlaylistDetailUiEvent.PlayAll -> playFromIndex(0)
            PlaylistDetailUiEvent.ShufflePlay -> shufflePlay()
            is PlaylistDetailUiEvent.PlayTrack -> playTrack(event.track)
            PlaylistDetailUiEvent.DownloadPlaylist -> downloadPlaylist()
            PlaylistDetailUiEvent.RemoveDownload -> removeDownload()
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
                observeDownloadState(playlistId)
            } else if (loadFromOfflineDatabase(playlistId)) {
                observeDownloadState(playlistId)
            } else {
                _uiState.value = PlaylistDetailUiState.Error(
                    playlistResult.exceptionOrNull()?.message
                        ?: tracksResult.exceptionOrNull()?.message
                        ?: "Failed to load playlist"
                )
            }
        }
    }

    private suspend fun loadFromOfflineDatabase(playlistId: String): Boolean {
        val collections = downloadRepository.getDownloadedCollections().first()
        val collection = collections.find {
            it.id == playlistId && it.type == CollectionType.PLAYLIST
        } ?: return false

        val downloadedTracks = downloadRepository
            .getDownloadedTracksForCollection(playlistId).first()
        if (downloadedTracks.isEmpty()) return false

        val playlist = PlaylistItem(
            id = collection.id,
            title = collection.title,
            description = null,
            imageUrl = collection.imageUrl.ifBlank { null },
            numberOfTracks = collection.trackCount,
            creator = "",
        )
        val tracks = downloadedTracks.map { dt ->
            TrackItem(
                id = dt.trackId.toString(),
                title = dt.title,
                artistName = dt.artistName,
                albumTitle = dt.albumTitle,
                duration = dt.duration,
                trackNumber = dt.trackNumber,
                imageUrl = dt.imageUrl.ifBlank { null },
            )
        }
        _uiState.value = PlaylistDetailUiState.Success(
            playlist = playlist,
            tracks = tracks.toImmutableList(),
            isDownloaded = true,
        )
        return true
    }

    private fun observeDownloadState(playlistId: String) {
        viewModelScope.launch {
            downloadRepository.getDownloadedCollections().collect { collections ->
                val current = _uiState.value as? PlaylistDetailUiState.Success ?: return@collect
                val collection = collections.find {
                    it.id == playlistId && it.type == CollectionType.PLAYLIST
                }
                _uiState.value = current.copy(
                    isDownloaded = collection?.state == DownloadState.COMPLETED,
                    isDownloading = collection?.state == DownloadState.PENDING ||
                        collection?.state == DownloadState.DOWNLOADING,
                )
            }
        }
    }

    private fun downloadPlaylist() {
        val state = _uiState.value as? PlaylistDetailUiState.Success ?: return
        viewModelScope.launch {
            downloadRepository.queueCollectionDownload(
                collectionId = state.playlist.id,
                type = CollectionType.PLAYLIST,
                title = state.playlist.title,
                imageUrl = state.playlist.imageUrl.orEmpty(),
                tracks = state.tracks,
            )
            downloadWorkScheduler.enqueueCollectionDownload(
                state.playlist.id,
                CollectionType.PLAYLIST,
            )
        }
    }

    private fun removeDownload() {
        val state = _uiState.value as? PlaylistDetailUiState.Success ?: return
        viewModelScope.launch {
            downloadRepository.deleteCollection(state.playlist.id, CollectionType.PLAYLIST)
            downloadWorkScheduler.cancelCollectionDownload(state.playlist.id)
        }
    }

    private fun playTrack(track: TrackItem) {
        val state = _uiState.value as? PlaylistDetailUiState.Success ?: return
        val startIndex = state.tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playFromIndex(startIndex)
    }

    private fun shufflePlay() {
        val state = _uiState.value as? PlaylistDetailUiState.Success ?: return
        if (state.tracks.isEmpty()) return
        val randomIndex = state.tracks.indices.random()
        playFromIndex(randomIndex)
    }

    private fun playFromIndex(startIndex: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state !is PlaylistDetailUiState.Success || state.tracks.isEmpty()) return@launch

            playbackControl.playTracks(state.tracks, startIndex)
                .onSuccess { _uiEffect.send(PlaylistDetailUiEffect.NavigateToNowPlaying) }
                .onFailure { error ->
                    _uiEffect.send(
                        PlaylistDetailUiEffect.ShowError(
                            error.message ?: "Failed to start playback",
                        ),
                    )
                }
        }
    }
}
