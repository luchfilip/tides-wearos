package dev.tidesapp.wearos.library.data.repository

import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.core.domain.model.TrackItem
import dev.tidesapp.wearos.library.data.api.TidesLibraryApi
import dev.tidesapp.wearos.library.domain.mapper.toDomain
import dev.tidesapp.wearos.library.domain.repository.MixRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MixRepositoryImpl @Inject constructor(
    private val api: TidesLibraryApi,
    private val credentialsProvider: CredentialsProvider,
) : MixRepository {

    private suspend fun getBearerToken(): Result<String> {
        val result = credentialsProvider.getCredentials(null)
        val token = result.successData?.token
            ?: return Result.failure(RuntimeException("Failed to obtain credentials"))
        return Result.success("Bearer $token")
    }

    override suspend fun getMixItems(mixId: String): Result<List<TrackItem>> {
        return try {
            val token = getBearerToken().getOrElse { return Result.failure(it) }
            val response = api.getMixItems(token = token, mixId = mixId)
            // /v1/mixes/{id}/items wraps each track in { item, type: "track" } envelopes.
            // Drop envelopes without a track payload defensively.
            val tracks = response.items.mapNotNull { it.item?.toDomain() }
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
