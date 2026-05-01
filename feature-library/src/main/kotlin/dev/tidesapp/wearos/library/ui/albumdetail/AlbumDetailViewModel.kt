package dev.tidesapp.wearos.library.ui.albumdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.core.domain.playback.PlaybackControl
import dev.tidesapp.wearos.download.data.worker.DownloadWorkScheduler
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.DownloadState
import dev.tidesapp.wearos.download.domain.repository.DownloadRepository
import dev.tidesapp.wearos.library.domain.repository.AlbumRepository
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
sealed interface AlbumDetailUiState {
    data object Initial : AlbumDetailUiState
    data object Loading : AlbumDetailUiState
    data class Success(
        val album: AlbumItem,
        val tracks: ImmutableList<TrackItem>,
        val isDownloaded: Boolean = false,
        val isDownloading: Boolean = false,
    ) : AlbumDetailUiState

    data class Error(val message: String) : AlbumDetailUiState
}

@Immutable
sealed interface AlbumDetailUiEvent {
    data class LoadAlbumDetail(val albumId: String) : AlbumDetailUiEvent
    data object PlayAll : AlbumDetailUiEvent
    data class PlayTrack(val track: TrackItem) : AlbumDetailUiEvent
    data object DownloadAlbum : AlbumDetailUiEvent
    data object RemoveDownload : AlbumDetailUiEvent
}

@Immutable
sealed interface AlbumDetailUiEffect {
    data object NavigateToNowPlaying : AlbumDetailUiEffect
    data class ShowError(val message: String) : AlbumDetailUiEffect
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val playbackControl: PlaybackControl,
    private val downloadRepository: DownloadRepository,
    private val downloadWorkScheduler: DownloadWorkScheduler,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Initial)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AlbumDetailUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: AlbumDetailUiEvent) {
        when (event) {
            is AlbumDetailUiEvent.LoadAlbumDetail -> loadAlbumDetail(event.albumId)
            AlbumDetailUiEvent.PlayAll -> playFromIndex(0)
            is AlbumDetailUiEvent.PlayTrack -> playTrack(event.track)
            AlbumDetailUiEvent.DownloadAlbum -> downloadAlbum()
            AlbumDetailUiEvent.RemoveDownload -> removeDownload()
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
                observeDownloadState(albumId)
            } else if (loadFromOfflineDatabase(albumId)) {
                observeDownloadState(albumId)
            } else {
                _uiState.value = AlbumDetailUiState.Error(
                    albumResult.exceptionOrNull()?.message
                        ?: tracksResult.exceptionOrNull()?.message
                        ?: "Failed to load album"
                )
            }
        }
    }

    private suspend fun loadFromOfflineDatabase(albumId: String): Boolean {
        val collections = downloadRepository.getDownloadedCollections().first()
        val collection = collections.find {
            it.id == albumId && it.type == CollectionType.ALBUM
        } ?: return false

        val downloadedTracks = downloadRepository
            .getDownloadedTracksForCollection(albumId).first()
        if (downloadedTracks.isEmpty()) return false

        val album = AlbumItem(
            id = collection.id,
            title = collection.title,
            artistName = downloadedTracks.first().artistName,
            imageUrl = collection.imageUrl.ifBlank { null },
            releaseDate = null,
            numberOfTracks = collection.trackCount,
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
        _uiState.value = AlbumDetailUiState.Success(
            album = album,
            tracks = tracks.toImmutableList(),
            isDownloaded = true,
        )
        return true
    }

    private fun observeDownloadState(albumId: String) {
        viewModelScope.launch {
            downloadRepository.getDownloadedCollections().collect { collections ->
                val current = _uiState.value as? AlbumDetailUiState.Success ?: return@collect
                val collection = collections.find {
                    it.id == albumId && it.type == CollectionType.ALBUM
                }
                _uiState.value = current.copy(
                    isDownloaded = collection?.state == DownloadState.COMPLETED,
                    isDownloading = collection?.state == DownloadState.PENDING ||
                        collection?.state == DownloadState.DOWNLOADING,
                )
            }
        }
    }

    private fun downloadAlbum() {
        val state = _uiState.value as? AlbumDetailUiState.Success ?: return
        viewModelScope.launch {
            downloadRepository.queueCollectionDownload(
                collectionId = state.album.id,
                type = CollectionType.ALBUM,
                title = state.album.title,
                imageUrl = state.album.imageUrl.orEmpty(),
                tracks = state.tracks,
            )
            downloadWorkScheduler.enqueueCollectionDownload(
                state.album.id,
                CollectionType.ALBUM,
            )
        }
    }

    private fun removeDownload() {
        val state = _uiState.value as? AlbumDetailUiState.Success ?: return
        viewModelScope.launch {
            downloadRepository.deleteCollection(state.album.id, CollectionType.ALBUM)
            downloadWorkScheduler.cancelCollectionDownload(state.album.id)
        }
    }

    private fun playTrack(track: TrackItem) {
        val state = _uiState.value as? AlbumDetailUiState.Success ?: return
        val startIndex = state.tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playFromIndex(startIndex)
    }

    private fun playFromIndex(startIndex: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state !is AlbumDetailUiState.Success || state.tracks.isEmpty()) return@launch

            playbackControl.playTracks(state.tracks, startIndex)
                .onSuccess { _uiEffect.send(AlbumDetailUiEffect.NavigateToNowPlaying) }
                .onFailure { error ->
                    _uiEffect.send(
                        AlbumDetailUiEffect.ShowError(error.message ?: "Failed to start playback"),
                    )
                }
        }
    }
}
