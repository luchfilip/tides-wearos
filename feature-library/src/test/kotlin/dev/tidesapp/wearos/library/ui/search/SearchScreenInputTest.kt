package dev.tidesapp.wearos.library.ui.search

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SearchScreenInputTest {

    @Before
    fun setUp() {
        mockkStatic(RemoteInput::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(RemoteInput::class)
    }

    @Test
    fun `extractSearchQuery returns null when intent is null`() {
        assertNull(extractSearchQuery(null))
    }

    @Test
    fun `extractSearchQuery returns null when RemoteInput has no results`() {
        val intent = mockk<Intent>()
        every { RemoteInput.getResultsFromIntent(intent) } returns null

        assertNull(extractSearchQuery(intent))
    }

    @Test
    fun `extractSearchQuery returns null when query key missing`() {
        val intent = mockk<Intent>()
        val bundle = mockk<Bundle>()
        every { RemoteInput.getResultsFromIntent(intent) } returns bundle
        every { bundle.getCharSequence(REMOTE_INPUT_QUERY_KEY) } returns null

        assertNull(extractSearchQuery(intent))
    }

    @Test
    fun `extractSearchQuery returns null when query is blank`() {
        val intent = mockk<Intent>()
        val bundle = mockk<Bundle>()
        every { RemoteInput.getResultsFromIntent(intent) } returns bundle
        every { bundle.getCharSequence(REMOTE_INPUT_QUERY_KEY) } returns "   "

        assertNull(extractSearchQuery(intent))
    }

    @Test
    fun `extractSearchQuery returns trimmed query when non-blank`() {
        val intent = mockk<Intent>()
        val bundle = mockk<Bundle>()
        every { RemoteInput.getResultsFromIntent(intent) } returns bundle
        every { bundle.getCharSequence(REMOTE_INPUT_QUERY_KEY) } returns "  daft punk  "

        assertEquals("daft punk", extractSearchQuery(intent))
    }
}
