package dev.tidesapp.wearos.library.domain.repository

import dev.tidesapp.wearos.core.domain.model.SearchResult

interface SearchRepository {
    suspend fun search(query: String): Result<SearchResult>
}
