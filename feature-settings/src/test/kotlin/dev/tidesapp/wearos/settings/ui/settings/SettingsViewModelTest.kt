package dev.tidesapp.wearos.settings.ui.settings

import app.cash.turbine.test
import dev.tidesapp.wearos.auth.domain.repository.AuthRepository
import dev.tidesapp.wearos.core.domain.model.AudioQualityPreference
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk()
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubDefaults(
        quality: AudioQualityPreference = AudioQualityPreference.HIGH,
        wifiOnly: Boolean = false,
        storageUsed: String = "0 MB",
    ) {
        every { settingsRepository.getAudioQuality() } returns flowOf(quality)
        every { settingsRepository.isWifiOnly() } returns flowOf(wifiOnly)
        coEvery { settingsRepository.getStorageUsed() } returns storageUsed
    }

    @Test
    fun `loads saved preferences on init`() = runTest {
        stubDefaults(
            quality = AudioQualityPreference.LOSSLESS,
            wifiOnly = true,
            storageUsed = "42 MB",
        )

        viewModel = SettingsViewModel(settingsRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SettingsUiState.Loaded)
        val loaded = state as SettingsUiState.Loaded
        assertEquals(AudioQualityPreference.LOSSLESS, loaded.quality)
        assertTrue(loaded.wifiOnly)
        assertEquals("42 MB", loaded.storageUsed)
    }

    @Test
    fun `ChangeQuality persists to DataStore`() = runTest {
        stubDefaults()
        coEvery { settingsRepository.setAudioQuality(any()) } returns Unit

        viewModel = SettingsViewModel(settingsRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.ChangeQuality(AudioQualityPreference.LOSSLESS))
        advanceUntilIdle()

        coVerify { settingsRepository.setAudioQuality(AudioQualityPreference.LOSSLESS) }
    }

    @Test
    fun `ToggleWifiOnly persists to DataStore`() = runTest {
        stubDefaults(wifiOnly = false)
        coEvery { settingsRepository.setWifiOnly(any()) } returns Unit

        viewModel = SettingsViewModel(settingsRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.ToggleWifiOnly)
        advanceUntilIdle()

        coVerify { settingsRepository.setWifiOnly(true) }
    }

    @Test
    fun `Logout sends NavigateToLogin effect`() = runTest {
        stubDefaults()
        coEvery { authRepository.logout() } just Runs

        viewModel = SettingsViewModel(settingsRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.Logout)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.NavigateToLogin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout event calls AuthRepository logout then emits NavigateToLogin`() = runTest {
        stubDefaults()
        coEvery { authRepository.logout() } just Runs

        viewModel = SettingsViewModel(settingsRepository, authRepository)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.Logout)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.NavigateToLogin)
            cancelAndIgnoreRemainingEvents()
        }

        coVerifyOrder {
            authRepository.logout()
        }
        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun `logout still navigates when repo fails`() = runTest {
        stubDefaults()
        coEvery { authRepository.logout() } throws RuntimeException("revoke failed")

        viewModel = SettingsViewModel(settingsRepository, authRepository)
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.Logout)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.NavigateToLogin)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { authRepository.logout() }
    }
}
