package dev.tidesapp.wearos.player.playback

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StreamingPrivilegesManagerTest {

    private lateinit var source: StreamingPrivilegeSource
    private lateinit var manager: StreamingPrivilegesManager

    @Before
    fun setup() {
        source = mockk()
        manager = StreamingPrivilegesManager(source)
    }

    @Test
    fun `acquireStreamingPrivileges calls SDK source`() = runTest {
        coEvery { source.acquire() } returns Result.success(Unit)

        val result = manager.acquireStreamingPrivileges()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { source.acquire() }
    }

    @Test
    fun `acquireStreamingPrivileges propagates failure`() = runTest {
        coEvery { source.acquire() } returns Result.failure(RuntimeException("No privileges"))

        val result = manager.acquireStreamingPrivileges()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { source.acquire() }
    }

    @Test
    fun `revocation callback invokes listener`() {
        var revoked = false
        manager.setRevocationListener { revoked = true }

        manager.onPrivilegesRevoked()

        assertTrue(revoked)
    }

    @Test
    fun `revocation without listener does not crash`() {
        manager.onPrivilegesRevoked()
    }
}
