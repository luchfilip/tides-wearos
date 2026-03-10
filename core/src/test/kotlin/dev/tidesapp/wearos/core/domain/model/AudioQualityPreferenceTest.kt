package dev.tidesapp.wearos.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioQualityPreferenceTest {

    @Test
    fun `all enum values exist`() {
        val values = AudioQualityPreference.entries
        assertEquals(3, values.size)
        assertTrue(values.contains(AudioQualityPreference.LOW))
        assertTrue(values.contains(AudioQualityPreference.HIGH))
        assertTrue(values.contains(AudioQualityPreference.LOSSLESS))
    }

    @Test
    fun `DEFAULT is HIGH`() {
        assertEquals(AudioQualityPreference.HIGH, AudioQualityPreference.DEFAULT)
    }
}
