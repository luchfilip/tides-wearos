package dev.tidesapp.wearos.auth.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.flintsdk.Flint
import com.flintsdk.semantics.flintContent

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Flint.tools {
        tool("start_login", "Start device code login flow") {
            action { viewModel.onEvent(LoginUiEvent.StartLogin); null }
        }
        tool("retry_login", "Retry login after error") {
            action { viewModel.onEvent(LoginUiEvent.RetryLogin); null }
        }
    }

    Box(modifier = Modifier.height(0.dp).flintContent("login_state").semantics {
        text = AnnotatedString(when (uiState) {
            LoginUiState.Initial -> "initial"
            LoginUiState.Loading -> "loading"
            is LoginUiState.ShowingCode -> "showing_code"
            LoginUiState.Success -> "success"
            is LoginUiState.Error -> "error"
        })
    })

    LoginContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginUiEffect.NavigateToHome -> onNavigateToHome()
                is LoginUiEffect.ShowError -> { /* handled via state */ }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (uiState is LoginUiState.Initial) {
            viewModel.onEvent(LoginUiEvent.StartLogin)
        }
    }
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is LoginUiState.Initial,
        is LoginUiState.Loading,
        -> LoadingContent(modifier)

        is LoginUiState.ShowingCode -> DeviceCodeContent(
            userCode = uiState.userCode,
            verificationUri = uiState.verificationUri,
            modifier = modifier,
        )

        is LoginUiState.Success -> SuccessContent(modifier)

        is LoginUiState.Error -> ErrorContent(
            message = uiState.message,
            onRetry = { onEvent(LoginUiEvent.RetryLogin) },
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            indicatorColor = MaterialTheme.colors.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Signing in...",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface,
        )
    }
}

@Composable
private fun DeviceCodeContent(
    userCode: String,
    verificationUri: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Go to",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = verificationUri,
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Enter code",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userCode,
            style = MaterialTheme.typography.title1,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.flintContent("user_code"),
        )
        Spacer(modifier = Modifier.height(12.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            indicatorColor = MaterialTheme.colors.primary,
        )
    }
}

@Composable
private fun SuccessContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Signed in!",
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.primary,
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
