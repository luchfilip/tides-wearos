package dev.tidesapp.wearos.player.playback

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicServiceControllerTest {

    @Test
    fun `inactivity timeout triggers stop callback`() = runTest {
        val controller = MusicServiceController(this)
        var stopped = false
        controller.setStopCallback { stopped = true }

        controller.startInactivityTimer()
        advanceTimeBy(MusicServiceController.INACTIVITY_TIMEOUT_MS + 1)

        assertTrue(stopped)
    }

    @Test
    fun `cancel inactivity timer prevents stop`() = runTest {
        val controller = MusicServiceController(this)
        var stopped = false
        controller.setStopCallback { stopped = true }

        controller.startInactivityTimer()
        advanceTimeBy(MusicServiceController.INACTIVITY_TIMEOUT_MS / 2)
        controller.cancelInactivityTimer()
        advanceTimeBy(MusicServiceController.INACTIVITY_TIMEOUT_MS)

        assertEquals(false, stopped)
    }

    @Test
    fun `onTaskRemoved cancels timer and invokes stop`() = runTest {
        val controller = MusicServiceController(this)
        var stopCount = 0
        controller.setStopCallback { stopCount++ }

        controller.startInactivityTimer()
        controller.onTaskRemoved()

        assertEquals(1, stopCount)
    }

    @Test
    fun `onTaskRemoved invokes stop only once`() = runTest {
        val controller = MusicServiceController(this)
        var stopCount = 0
        controller.setStopCallback { stopCount++ }

        controller.onTaskRemoved()
        controller.onTaskRemoved()

        assertEquals(1, stopCount)
    }

    @Test
    fun `onDestroy cancels inactivity timer`() = runTest {
        val controller = MusicServiceController(this)
        var stopped = false
        controller.setStopCallback { stopped = true }

        controller.startInactivityTimer()
        controller.onDestroy()
        advanceTimeBy(MusicServiceController.INACTIVITY_TIMEOUT_MS + 1)

        assertEquals(false, stopped)
    }
}
