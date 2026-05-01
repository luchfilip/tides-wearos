package dev.tidesapp.wearos.download.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.tidal.sdk.auth.CredentialsProvider
import dev.tidesapp.wearos.download.data.api.TidesOfflineApi
import dev.tidesapp.wearos.download.data.api.dto.SessionClientInfo
import dev.tidesapp.wearos.download.data.api.dto.SessionResponse
import dev.tidesapp.wearos.download.domain.model.CollectionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineRegistrationRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var api: TidesOfflineApi
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var repository: OfflineRegistrationRepositoryImpl

    private val fakeSession = SessionResponse(
        sessionId = "session-1",
        userId = 42L,
        countryCode = "US",
        client = SessionClientInfo(
            id = 100L,
            name = "Tides Watch",
            authorizedForOffline = true,
            authorizedForOfflineDate = "2026-01-01",
        ),
    )

    private val unauthorizedSession = fakeSession.copy(
        client = fakeSession.client.copy(authorizedForOffline = false),
    )

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { tempFolder.newFile("test_offline.preferences_pb") },
        )
        api = mockk()
        credentialsProvider = mockk {
            coEvery { getCredentials(any()) } returns mockk {
                every { successData } returns mockk {
                    every { token } returns "fake-token"
                }
            }
        }
        repository = OfflineRegistrationRepositoryImpl(api, credentialsProvider, dataStore)
    }

    // -- ensureOfflineAuthorized --

    @Test
    fun `ensureOfflineAuthorized returns cached state when already authorized`() =
        testScope.runTest {
            // Pre-populate DataStore with cached authorized state
            dataStore.edit { prefs ->
                prefs[longPreferencesKey("offline_client_id")] = 100L
                prefs[booleanPreferencesKey("offline_authorized")] = true
            }

            val result = repository.ensureOfflineAuthorized()

            assertTrue(result.isSuccess)
            val reg = result.getOrThrow()
            assertEquals(100L, reg.clientId)
            assertTrue(reg.authorizedForOffline)
            // No API calls should have been made
            coVerify(exactly = 0) { api.getSession(any()) }
        }

    @Test
    fun `ensureOfflineAuthorized calls API when not cached`() = testScope.runTest {
        // DataStore is empty — should call API
        coEvery { api.getSession(any()) } returns fakeSession

        val result = repository.ensureOfflineAuthorized()

        assertTrue(result.isSuccess)
        val reg = result.getOrThrow()
        assertEquals(100L, reg.clientId)
        assertTrue(reg.authorizedForOffline)
        coVerify(exactly = 1) { api.getSession("Bearer fake-token") }
    }

    @Test
    fun `ensureOfflineAuthorized registers when session not authorized`() =
        testScope.runTest {
            coEvery { api.getSession(any()) } returns unauthorizedSession
            coEvery { api.registerForOffline(any(), any()) } returns Response.success(Unit)
            coEvery { api.getSessionById(any(), any()) } returns fakeSession

            val result = repository.ensureOfflineAuthorized()

            assertTrue(result.isSuccess)
            val reg = result.getOrThrow()
            assertTrue(reg.authorizedForOffline)
            coVerify(exactly = 1) { api.registerForOffline("Bearer fake-token", 42L) }
            coVerify(exactly = 1) { api.getSessionById("Bearer fake-token", "session-1") }
        }

    // -- getSessionInfo --

    @Test
    fun `getSessionInfo caches clientId and authorized status`() = testScope.runTest {
        coEvery { api.getSession(any()) } returns fakeSession

        val result = repository.getSessionInfo()

        assertTrue(result.isSuccess)
        val reg = result.getOrThrow()
        assertEquals(100L, reg.clientId)
        assertTrue(reg.authorizedForOffline)

        // Verify DataStore was updated — ensureOfflineAuthorized should now use cache
        val cachedResult = repository.ensureOfflineAuthorized()
        assertTrue(cachedResult.isSuccess)
        // getSession was only called once (for getSessionInfo), not again for ensure
        coVerify(exactly = 1) { api.getSession(any()) }
    }

    @Test
    fun `getSessionInfo wraps API errors in Result failure`() = testScope.runTest {
        coEvery { api.getSession(any()) } throws RuntimeException("network error")

        val result = repository.getSessionInfo()

        assertTrue(result.isFailure)
        assertEquals("network error", result.exceptionOrNull()?.message)
    }

    // -- registerCollectionOffline --

    @Test
    fun `registerCollectionOffline dispatches to playlist endpoint`() =
        testScope.runTest {
            // Pre-populate cached clientId
            dataStore.edit { prefs ->
                prefs[longPreferencesKey("offline_client_id")] = 100L
                prefs[booleanPreferencesKey("offline_authorized")] = true
            }
            coEvery { api.getSession(any()) } returns fakeSession
            coEvery {
                api.registerPlaylistOffline(any(), any(), any())
            } returns Response.success(Unit)

            val result = repository.registerCollectionOffline("playlist-1", CollectionType.PLAYLIST)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                api.registerPlaylistOffline("Bearer fake-token", 42L, 100L)
            }
        }

    @Test
    fun `registerCollectionOffline dispatches to album endpoint`() =
        testScope.runTest {
            dataStore.edit { prefs ->
                prefs[longPreferencesKey("offline_client_id")] = 100L
                prefs[booleanPreferencesKey("offline_authorized")] = true
            }
            coEvery { api.getSession(any()) } returns fakeSession
            coEvery {
                api.registerAlbumOffline(any(), any(), any())
            } returns Response.success(Unit)

            val result = repository.registerCollectionOffline("album-1", CollectionType.ALBUM)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                api.registerAlbumOffline("Bearer fake-token", 42L, 100L)
            }
        }

    @Test
    fun `registerCollectionOffline throws when device not registered`() =
        testScope.runTest {
            // DataStore has no clientId
            coEvery { api.getSession(any()) } returns fakeSession

            val result = repository.registerCollectionOffline("album-1", CollectionType.ALBUM)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }
}
