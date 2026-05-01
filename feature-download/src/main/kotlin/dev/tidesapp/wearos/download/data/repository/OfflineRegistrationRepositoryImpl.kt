package dev.tidesapp.wearos.download.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.domain.model.CollectionType
import dev.tidesapp.wearos.download.domain.model.OfflineRegistration
import dev.tidesapp.wearos.download.domain.repository.OfflineRegistrationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRegistrationRepositoryImpl @Inject constructor(
    private val offlineApi: TidesOfflineApi,
    private val credentialsProvider: CredentialsProvider,
    private val dataStore: DataStore<Preferences>,
) : OfflineRegistrationRepository {

    private object Keys {
        val CLIENT_ID = longPreferencesKey("offline_client_id")
        val AUTHORIZED = booleanPreferencesKey("offline_authorized")
    }

    override suspend fun getSessionInfo(): Result<OfflineRegistration> = runCatching {
        val token = getBearerToken()
        val session = offlineApi.getSession(token)
        val registration = OfflineRegistration(
            clientId = session.client.id,
            authorizedForOffline = session.client.authorizedForOffline,
            authorizedAt = null,
        )
        dataStore.edit { prefs ->
            prefs[Keys.CLIENT_ID] = registration.clientId
            prefs[Keys.AUTHORIZED] = registration.authorizedForOffline
        }
        registration
    }

    override suspend fun registerDeviceForOffline(): Result<OfflineRegistration> = runCatching {
        val token = getBearerToken()
        val session = offlineApi.getSession(token)
        val response = offlineApi.registerForOffline(token, session.userId)
        if (!response.isSuccessful) {
            throw RuntimeException(
                "Offline registration failed: HTTP ${response.code()}",
            )
        }
        val confirmed = offlineApi.getSessionById(token, session.sessionId)
        if (!confirmed.client.authorizedForOffline) {
            throw RuntimeException(
                "Device not authorized for offline after registration",
            )
        }
        val registration = OfflineRegistration(
            clientId = confirmed.client.id,
            authorizedForOffline = true,
            authorizedAt = System.currentTimeMillis(),
        )
        dataStore.edit { prefs ->
            prefs[Keys.CLIENT_ID] = registration.clientId
            prefs[Keys.AUTHORIZED] = true
        }
        registration
    }

    override suspend fun ensureOfflineAuthorized(): Result<OfflineRegistration> {
        val prefs = dataStore.data.first()
        val cachedClientId = prefs[Keys.CLIENT_ID]
        val cachedAuthorized = prefs[Keys.AUTHORIZED] ?: false

        if (cachedClientId != null && cachedAuthorized) {
            return Result.success(
                OfflineRegistration(
                    clientId = cachedClientId,
                    authorizedForOffline = true,
                    authorizedAt = null,
                ),
            )
        }

        val sessionResult = getSessionInfo()
        val session = sessionResult.getOrElse { return Result.failure(it) }

        if (session.authorizedForOffline) {
            return Result.success(session)
        }

        return registerDeviceForOffline()
    }

    override suspend fun registerCollectionOffline(
        collectionId: String,
        type: CollectionType,
    ): Result<Unit> = runCatching {
        val token = getBearerToken()
        val prefs = dataStore.data.first()
        val clientId = prefs[Keys.CLIENT_ID]
            ?: throw IllegalStateException("Device not registered for offline")

        val session = offlineApi.getSession(token)

        val response = when (type) {
            CollectionType.PLAYLIST ->
                offlineApi.registerPlaylistOffline(token, session.userId, clientId)
            CollectionType.ALBUM ->
                offlineApi.registerAlbumOffline(token, session.userId, clientId)
        }
        if (!response.isSuccessful) {
            throw RuntimeException(
                "Collection registration failed: HTTP ${response.code()}",
            )
        }
    }

    private suspend fun getBearerToken(): String {
        val result = credentialsProvider.getCredentials(null)
        val token = result.successData?.token
            ?: throw RuntimeException("Failed to obtain credentials")
        return "Bearer $token"
    }
}
