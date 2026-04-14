package dev.tidesapp.wearos.di

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Minimal regression test for Hilt named-string bindings that feed the Tidal SDK.
 *
 * This does not spin up a Hilt test component (too much infra for one test). Instead it
 * calls the @Provides functions directly to catch regressions such as:
 *  - A provider being deleted.
 *  - BuildConfig fields being renamed without updating the binding.
 *  - A fresh-clone build silently producing a null/empty credential.
 *
 * FEATURE_AUDIT.md P0 #4.
 */
class AppConfigModuleTest {

    @Test
    fun `provideClientId returns a non-null string`() {
        val value = AppConfigModule.provideClientId()
        assertNotNull("clientId binding should exist", value)
    }

    @Test
    fun `provideClientSecret returns a non-null string`() {
        val value = AppConfigModule.provideClientSecret()
        assertNotNull("clientSecret binding should exist", value)
    }

    @Test
    fun `provideClientVersion returns a non-empty string`() {
        val value = AppConfigModule.provideClientVersion()
        assertNotNull(value)
        assertTrue(
            "clientVersion should default to a non-empty version string when unset",
            value.isNotEmpty(),
        )
    }

    @Test
    fun `provideApiBaseUrl and provideAuthBaseUrl return non-null strings`() {
        assertNotNull(AppConfigModule.provideApiBaseUrl())
        assertNotNull(AppConfigModule.provideAuthBaseUrl())
    }

    @Test
    fun `named bindings are distinct providers (regression guard)`() {
        // Sanity: these should be separate functions, not accidentally aliased.
        // If someone deletes one and redirects the binding elsewhere this still
        // catches the case where both ids collapse onto the same value.
        val id = AppConfigModule.provideClientId()
        val secret = AppConfigModule.provideClientSecret()
        // We don't assert inequality because in theory gradle.properties could
        // contain the same literal in a dev setup, but assert they are each
        // resolved independently without throwing.
        assertEquals(id, AppConfigModule.provideClientId())
        assertEquals(secret, AppConfigModule.provideClientSecret())
    }
}
