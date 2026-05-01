package dev.tidesapp.wearos.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.tidesapp.wearos.core.domain.model.AudioQualityPreference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { tempFolder.newFile("test_settings.preferences_pb") },
        )
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `getAudioQuality returns default when no value saved`() = testScope.runTest {
        val quality = repository.getAudioQuality().first()
        assertEquals(AudioQualityPreference.DEFAULT, quality)
    }

    @Test
    fun `setAudioQuality persists value`() = testScope.runTest {
        repository.setAudioQuality(AudioQualityPreference.LOSSLESS)
        val quality = repository.getAudioQuality().first()
        assertEquals(AudioQualityPreference.LOSSLESS, quality)
    }

    @Test
    fun `isWifiOnly returns false by default`() = testScope.runTest {
        val wifiOnly = repository.isWifiOnly().first()
        assertFalse(wifiOnly)
    }

    @Test
    fun `setWifiOnly persists value`() = testScope.runTest {
        repository.setWifiOnly(true)
        val wifiOnly = repository.isWifiOnly().first()
        assertTrue(wifiOnly)
    }

    @Test
    fun `getDownloadQuality returns HIGH by default`() = testScope.runTest {
        val quality = repository.getDownloadQuality().first()
        assertEquals(AudioQualityPreference.HIGH, quality)
    }

    @Test
    fun `setDownloadQuality persists and emits new value`() = testScope.runTest {
        repository.setDownloadQuality(AudioQualityPreference.LOSSLESS)
        val quality = repository.getDownloadQuality().first()
        assertEquals(AudioQualityPreference.LOSSLESS, quality)
    }

    @Test
    fun `getStorageLimitBytes returns 1GB by default`() = testScope.runTest {
        val limit = repository.getStorageLimitBytes().first()
        assertEquals(1_073_741_824L, limit)
    }

    @Test
    fun `setStorageLimitBytes persists and emits new value`() = testScope.runTest {
        val twoGb = 2_147_483_648L
        repository.setStorageLimitBytes(twoGb)
        val limit = repository.getStorageLimitBytes().first()
        assertEquals(twoGb, limit)
    }

    @Test
    fun `getDownloadQuality handles invalid stored value gracefully`() = testScope.runTest {
        // Write an invalid value directly to DataStore
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("download_quality")] = "INVALID_VALUE"
        }
        val quality = repository.getDownloadQuality().first()
        assertEquals(AudioQualityPreference.HIGH, quality)
    }
}
