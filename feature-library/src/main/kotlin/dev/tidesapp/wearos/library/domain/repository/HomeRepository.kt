package dev.tidesapp.wearos.library.domain.repository

import dev.tidesapp.wearos.core.domain.model.HomeFeedSection

interface HomeRepository {
    suspend fun getHomeFeed(forceRefresh: Boolean = false): Result<List<HomeFeedSection>>
}
