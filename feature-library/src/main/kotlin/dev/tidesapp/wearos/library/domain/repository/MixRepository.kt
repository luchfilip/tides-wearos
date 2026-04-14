package dev.tidesapp.wearos.library.domain.repository

import dev.tidesapp.wearos.core.domain.model.TrackItem

interface MixRepository {
    suspend fun getMixItems(mixId: String): Result<List<TrackItem>>
}
