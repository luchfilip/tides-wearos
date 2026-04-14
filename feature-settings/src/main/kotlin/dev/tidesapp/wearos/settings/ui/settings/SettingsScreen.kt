package dev.tidesapp.wearos.settings.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import dev.tidesapp.wearos.core.domain.model.AudioQualityPreference
import dev.tidesapp.wearos.core.ui.components.LoadingScreen
import kotlinx.coroutines.delay

private const val ERROR_DISMISS_MILLIS = 4_000L

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    SettingsContent(
        uiState = uiState,
        errorMessage = errorMessage,
        onEvent = viewModel::onEvent,
    )

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingsUiEffect.NavigateToLogin -> onNavigateToLogin()
                is SettingsUiEffect.ShowError -> errorMessage = effect.message
            }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(ERROR_DISMISS_MILLIS)
            errorMessage = null
        }
    }
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    errorMessage: String?,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is SettingsUiState.Initial -> LoadingScreen(modifier)
        is SettingsUiState.Loaded -> LoadedContent(
            state = uiState,
            errorMessage = errorMessage,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadedContent(
    state: SettingsUiState.Loaded,
    errorMessage: String?,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            ListHeader {
                Text("Settings")
            }
        }

        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Chip(
                onClick = {
                    onEvent(SettingsUiEvent.ChangeQuality(state.quality.next()))
                },
                label = { Text("Audio Quality") },
                secondaryLabel = { Text(state.quality.name) },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            ToggleChip(
                checked = state.wifiOnly,
                onCheckedChange = { onEvent(SettingsUiEvent.ToggleWifiOnly) },
                label = { Text("WiFi Only") },
                toggleControl = {
                    Icon(
                        imageVector = ToggleChipDefaults.switchIcon(checked = state.wifiOnly),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Chip(
                onClick = { onEvent(SettingsUiEvent.Logout) },
                label = { Text("Logout") },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun AudioQualityPreference.next(): AudioQualityPreference {
    val values = AudioQualityPreference.entries
    return values[(ordinal + 1) % values.size]
}
