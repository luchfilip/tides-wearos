package dev.tidesapp.wearos.download.domain.repository

import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.OfflineRegistration

interface OfflineRegistrationRepository {
    suspend fun getSessionInfo(): Result<OfflineRegistration>
    suspend fun registerDeviceForOffline(): Result<OfflineRegistration>
    suspend fun ensureOfflineAuthorized(): Result<OfflineRegistration>
    suspend fun registerCollectionOffline(collectionId: String, type: CollectionType): Result<Unit>
}
