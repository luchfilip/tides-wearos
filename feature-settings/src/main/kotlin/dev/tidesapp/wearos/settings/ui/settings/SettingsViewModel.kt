package dev.tidesapp.wearos.settings.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.auth.domain.repository.AuthRepository
import dev.tidesapp.wearos.core.domain.model.AudioQualityPreference
import dev.tidesapp.wearos.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
sealed interface SettingsUiState {
    data object Initial : SettingsUiState
    data class Loaded(
        val quality: AudioQualityPreference,
        val wifiOnly: Boolean,
    ) : SettingsUiState
}

@Immutable
sealed interface SettingsUiEvent {
    data class ChangeQuality(val quality: AudioQualityPreference) : SettingsUiEvent
    data object ToggleWifiOnly : SettingsUiEvent
    data object Logout : SettingsUiEvent
}

@Immutable
sealed interface SettingsUiEffect {
    data object NavigateToLogin : SettingsUiEffect
    data class ShowError(val message: String) : SettingsUiEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Initial)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SettingsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeSettings()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ChangeQuality -> changeQuality(event.quality)
            SettingsUiEvent.ToggleWifiOnly -> toggleWifiOnly()
            SettingsUiEvent.Logout -> logout()
        }
    }

    /**
     * Subscribe to the underlying preference flows so the UI reflects external
     * changes (e.g. the SDK writing a quality override after a stream starts).
     * Runs once on construction and stays collecting for the VM's lifetime.
     */
    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getAudioQuality(),
                settingsRepository.isWifiOnly(),
            ) { quality, wifiOnly ->
                SettingsUiState.Loaded(
                    quality = quality,
                    wifiOnly = wifiOnly,
                )
            }.catch { e ->
                _uiEffect.send(
                    SettingsUiEffect.ShowError(e.message ?: "Failed to load settings"),
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun changeQuality(quality: AudioQualityPreference) {
        viewModelScope.launch {
            runCatching { settingsRepository.setAudioQuality(quality) }
                .onFailure { e ->
                    _uiEffect.send(
                        SettingsUiEffect.ShowError(e.message ?: "Failed to save quality"),
                    )
                }
        }
    }

    private fun toggleWifiOnly() {
        viewModelScope.launch {
            val currentState = _uiState.value as? SettingsUiState.Loaded ?: return@launch
            runCatching { settingsRepository.setWifiOnly(!currentState.wifiOnly) }
                .onFailure { e ->
                    _uiEffect.send(
                        SettingsUiEffect.ShowError(e.message ?: "Failed to save preference"),
                    )
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            // Clear the session before navigating. If logout throws (e.g. the
            // SDK fails to revoke the token), we still navigate to login rather
            // than trap the user on the settings screen.
            runCatching { authRepository.logout() }
            _uiEffect.send(SettingsUiEffect.NavigateToLogin)
        }
    }
}
