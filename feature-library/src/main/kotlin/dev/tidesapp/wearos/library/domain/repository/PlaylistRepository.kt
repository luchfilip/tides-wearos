package dev.tidesapp.wearos.library.domain.repository

import dev.tidesapp.wearos.core.domain.model.PlaylistItem
import dev.tidesapp.wearos.core.domain.model.TrackItem

interface PlaylistRepository {
    suspend fun getUserPlaylists(forceRefresh: Boolean = false): Result<List<PlaylistItem>>
    suspend fun getPlaylist(id: String): Result<PlaylistItem>
    suspend fun getPlaylistTracks(id: String): Result<List<TrackItem>>
}
