package dev.tidesapp.wearos.library.domain.repository

import dev.tidesapp.wearos.core.domain.model.AlbumItem
import dev.tidesapp.wearos.core.domain.model.TrackItem

interface AlbumRepository {
    suspend fun getUserAlbums(forceRefresh: Boolean = false): Result<List<AlbumItem>>
    suspend fun getAlbumDetail(id: String): Result<AlbumItem>
    suspend fun getAlbumTracks(id: String): Result<List<TrackItem>>
}
