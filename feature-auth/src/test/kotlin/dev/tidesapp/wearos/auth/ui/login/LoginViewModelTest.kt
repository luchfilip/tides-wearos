package dev.tidesapp.wearos.auth.ui.login

import app.cash.turbine.test
import dev.tidesapp.wearos.auth.domain.model.DeviceCodeInfo
import dev.tidesapp.wearos.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Initial`() {
        viewModel = LoginViewModel(authRepository)
        assertEquals(LoginUiState.Initial, viewModel.uiState.value)
    }

    @Test
    fun `StartLogin transitions to Loading then ShowingCode`() = runTest {
        val deviceCodeInfo = createTestDeviceCodeInfo()
        coEvery { authRepository.initializeDeviceLogin() } returns Result.success(deviceCodeInfo)
        coEvery { authRepository.finalizeDeviceLogin(any()) } returns Result.success(Unit)

        viewModel = LoginViewModel(authRepository)

        viewModel.uiState.test {
            assertEquals(LoginUiState.Initial, awaitItem())

            viewModel.onEvent(LoginUiEvent.StartLogin)

            assertEquals(LoginUiState.Loading, awaitItem())
            val showingCode = awaitItem()
            assertTrue(showingCode is LoginUiState.ShowingCode)
            assertEquals("ABCD-1234", (showingCode as LoginUiState.ShowingCode).userCode)
            assertEquals("https://link.tidal.com", showingCode.verificationUri)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `polling success sends NavigateToHome effect via Channel`() = runTest {
        val deviceCodeInfo = createTestDeviceCodeInfo()
        coEvery { authRepository.initializeDeviceLogin() } returns Result.success(deviceCodeInfo)
        coEvery { authRepository.finalizeDeviceLogin("test-device-code") } returns Result.success(Unit)

        viewModel = LoginViewModel(authRepository)

        viewModel.onEvent(LoginUiEvent.StartLogin)
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(LoginUiState.Success, finalState)

        viewModel.uiEffect.test {
            val effect = awaitItem()
            assertTrue(effect is LoginUiEffect.NavigateToHome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `polling expired_token transitions to Error`() = runTest {
        val deviceCodeInfo = createTestDeviceCodeInfo()
        coEvery { authRepository.initializeDeviceLogin() } returns Result.success(deviceCodeInfo)
        coEvery { authRepository.finalizeDeviceLogin(any()) } returns Result.failure(
            RuntimeException("expired_token"),
        )

        viewModel = LoginViewModel(authRepository)
        viewModel.onEvent(LoginUiEvent.StartLogin)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("expired_token", (state as LoginUiState.Error).message)
    }

    @Test
    fun `duplicate StartLogin while polling is ignored`() = runTest {
        val deviceCodeInfo = createTestDeviceCodeInfo()
        coEvery { authRepository.initializeDeviceLogin() } returns Result.success(deviceCodeInfo)
        coEvery { authRepository.finalizeDeviceLogin(any()) } coAnswers {
            delay(10_000)
            Result.success(Unit)
        }

        viewModel = LoginViewModel(authRepository)
        viewModel.onEvent(LoginUiEvent.StartLogin)

        // Run all currently scheduled work (initializeDeviceLogin completes immediately,
        // finalizeDeviceLogin starts but hits delay(10_000) and suspends)
        testScheduler.runCurrent()

        // State should be ShowingCode — finalizeDeviceLogin is still suspended
        assertTrue(viewModel.uiState.value is LoginUiState.ShowingCode)

        // Send duplicate event while in ShowingCode state — should be ignored
        viewModel.onEvent(LoginUiEvent.StartLogin)
        testScheduler.runCurrent()

        coVerify(exactly = 1) { authRepository.initializeDeviceLogin() }
    }

    @Test
    fun `StartLogin failure transitions to Error`() = runTest {
        coEvery { authRepository.initializeDeviceLogin() } returns Result.failure(
            RuntimeException("Network error"),
        )

        viewModel = LoginViewModel(authRepository)
        viewModel.onEvent(LoginUiEvent.StartLogin)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Network error", (state as LoginUiState.Error).message)
    }

    @Test
    fun `RetryLogin from Error state starts new login flow`() = runTest {
        coEvery { authRepository.initializeDeviceLogin() } returns Result.failure(
            RuntimeException("Error"),
        ) andThen Result.success(createTestDeviceCodeInfo())
        coEvery { authRepository.finalizeDeviceLogin(any()) } returns Result.success(Unit)

        viewModel = LoginViewModel(authRepository)
        viewModel.onEvent(LoginUiEvent.StartLogin)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoginUiState.Error)

        viewModel.onEvent(LoginUiEvent.RetryLogin)
        advanceUntilIdle()

        assertEquals(LoginUiState.Success, viewModel.uiState.value)
        coVerify(exactly = 2) { authRepository.initializeDeviceLogin() }
    }

    private fun createTestDeviceCodeInfo(
        deviceCode: String = "test-device-code",
        userCode: String = "ABCD-1234",
        verificationUri: String = "https://link.tidal.com",
        expiresIn: Int = 300,
        interval: Int = 5,
    ) = DeviceCodeInfo(
        deviceCode = deviceCode,
        userCode = userCode,
        verificationUri = verificationUri,
        expiresIn = expiresIn,
        interval = interval,
    )
}
