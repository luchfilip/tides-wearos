package dev.tidesapp.wearos.nav

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidesapp.wearos.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
sealed interface NavGraphUiState {
    data object Initial : NavGraphUiState
    data object Authenticated : NavGraphUiState
    data object Unauthenticated : NavGraphUiState
}

@Immutable
sealed interface NavGraphUiEvent {
    data object CheckAuthState : NavGraphUiEvent
}

@Immutable
sealed interface NavGraphUiEffect {
    data object NavigateToHome : NavGraphUiEffect
    data object NavigateToLogin : NavGraphUiEffect
}

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NavGraphUiState>(NavGraphUiState.Initial)
    val uiState: StateFlow<NavGraphUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<NavGraphUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: NavGraphUiEvent) {
        when (event) {
            NavGraphUiEvent.CheckAuthState -> checkAuthState()
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                _uiState.value = NavGraphUiState.Authenticated
                _uiEffect.send(NavGraphUiEffect.NavigateToHome)
            } else {
                _uiState.value = NavGraphUiState.Unauthenticated
            }
        }
    }
}
