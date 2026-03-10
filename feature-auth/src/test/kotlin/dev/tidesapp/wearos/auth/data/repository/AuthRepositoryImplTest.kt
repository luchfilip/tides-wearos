package dev.tidesapp.wearos.auth.data.repository

import com.tidal.sdk.auth.Auth
import com.tidal.sdk.auth.CredentialsProvider
import com.tidal.sdk.auth.model.AuthResult
import com.tidal.sdk.auth.model.DeviceAuthorizationResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var auth: Auth
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        auth = mockk()
        credentialsProvider = mockk()
        repository = AuthRepositoryImpl(auth, credentialsProvider)
    }

    @Test
    fun `initializeDeviceLogin returns device code on success`() = runTest {
        val response = createTestDeviceAuthorizationResponse()
        coEvery { auth.initializeDeviceLogin() } returns AuthResult.Success(response)

        val result = repository.initializeDeviceLogin()

        assertTrue(result.isSuccess)
        val deviceCodeInfo = result.getOrNull()!!
        assertEquals("test-device-code", deviceCodeInfo.deviceCode)
        assertEquals("ABCD-1234", deviceCodeInfo.userCode)
        assertEquals("https://link.tidal.com", deviceCodeInfo.verificationUri)
        assertEquals(300, deviceCodeInfo.expiresIn)
        assertEquals(5, deviceCodeInfo.interval)
        coVerify(exactly = 1) { auth.initializeDeviceLogin() }
    }

    @Test
    fun `initializeDeviceLogin returns Result failure on SDK failure`() = runTest {
        coEvery { auth.initializeDeviceLogin() } returns AuthResult.Failure(null)

        val result = repository.initializeDeviceLogin()

        assertTrue(result.isFailure)
    }

    @Test
    fun `initializeDeviceLogin returns Result failure on network error`() = runTest {
        coEvery { auth.initializeDeviceLogin() } throws RuntimeException("Network error")

        val result = repository.initializeDeviceLogin()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `finalizeDeviceLogin returns success on SDK success`() = runTest {
        coEvery { auth.finalizeDeviceLogin("test-code") } returns AuthResult.Success(null)

        val result = repository.finalizeDeviceLogin("test-code")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { auth.finalizeDeviceLogin("test-code") }
    }

    @Test
    fun `finalizeDeviceLogin returns failure on SDK failure`() = runTest {
        coEvery { auth.finalizeDeviceLogin(any()) } returns AuthResult.Failure(null)

        val result = repository.finalizeDeviceLogin("test-code")

        assertTrue(result.isFailure)
    }

    @Test
    fun `finalizeDeviceLogin returns failure on exception`() = runTest {
        coEvery { auth.finalizeDeviceLogin(any()) } throws RuntimeException("Timeout")

        val result = repository.finalizeDeviceLogin("test-code")

        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }

    private fun createTestDeviceAuthorizationResponse(
        deviceCode: String = "test-device-code",
        userCode: String = "ABCD-1234",
        verificationUri: String = "https://link.tidal.com",
        verificationUriComplete: String? = null,
        expiresIn: Int = 300,
        interval: Int = 5,
    ) = DeviceAuthorizationResponse(
        deviceCode = deviceCode,
        userCode = userCode,
        verificationUri = verificationUri,
        verificationUriComplete = verificationUriComplete,
        expiresIn = expiresIn,
        interval = interval,
    )
}
