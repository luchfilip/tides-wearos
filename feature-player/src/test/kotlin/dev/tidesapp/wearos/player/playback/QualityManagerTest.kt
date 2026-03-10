package dev.tidesapp.wearos.player.playback

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.tidal.sdk.player.common.model.AudioQuality
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class QualityManagerTest {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var qualityManager: QualityManager
    private lateinit var network: Network
    private lateinit var capabilities: NetworkCapabilities

    @Before
    fun setup() {
        connectivityManager = mockk()
        network = mockk()
        capabilities = mockk()
        qualityManager = QualityManager(connectivityManager)
    }

    @Test
    fun `resolveQuality returns LOSSLESS on WiFi`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        val quality = qualityManager.resolveQuality()

        assertEquals(AudioQuality.LOSSLESS, quality)
    }

    @Test
    fun `resolveQuality returns HIGH on cellular`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false

        val quality = qualityManager.resolveQuality()

        assertEquals(AudioQuality.HIGH, quality)
    }

    @Test
    fun `resolveQuality returns HIGH when no network`() {
        every { connectivityManager.activeNetwork } returns null

        val quality = qualityManager.resolveQuality()

        assertEquals(AudioQuality.HIGH, quality)
    }

    @Test
    fun `resolveQuality never returns HI_RES_LOSSLESS`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        val qualityWifi = qualityManager.resolveQuality()
        assertNotEquals(AudioQuality.HI_RES_LOSSLESS, qualityWifi)

        every { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false

        val qualityCellular = qualityManager.resolveQuality()
        assertNotEquals(AudioQuality.HI_RES_LOSSLESS, qualityCellular)
    }
}
