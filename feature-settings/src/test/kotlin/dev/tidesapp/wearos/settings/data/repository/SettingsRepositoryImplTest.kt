package dev.tidesapp.wearos.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
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
}
