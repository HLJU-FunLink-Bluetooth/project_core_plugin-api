package com.hlju.funlinkbluetooth.core.plugin.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hlju.funlinkbluetooth.core.model.ConnectionRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NearbyAppAvailabilityTest {

    private val plugin = TestPlugin(requiredQuality = 2)

    @Test
    fun hostWithoutAdvertising_cannotOpenPlugin() {
        val availability = plugin.resolveNearbyAppAvailability(
            NearbyAppLaunchContext(
                role = ConnectionRole.HOST,
                isAdvertising = false,
                connectedEndpointCount = 0,
                currentQuality = 0,
            )
        )

        assertFalse(availability.canOpen)
        assertFalse(availability.canAssessQuality)
        assertEquals(NearbyAppAvailabilityState.HOST_NEEDS_ROOM, availability.state)
    }

    @Test
    fun hostAdvertisingWithoutPeers_canOpenBeforeQualityAssessment() {
        val availability = plugin.resolveNearbyAppAvailability(
            NearbyAppLaunchContext(
                role = ConnectionRole.HOST,
                isAdvertising = true,
                connectedEndpointCount = 0,
                currentQuality = 0,
            )
        )

        assertTrue(availability.canOpen)
        assertFalse(availability.canAssessQuality)
        assertEquals(NearbyAppAvailabilityState.WAITING_FOR_CONNECTION, availability.state)
    }

    @Test
    fun clientWithoutConnection_cannotOpenPlugin() {
        val availability = plugin.resolveNearbyAppAvailability(
            NearbyAppLaunchContext(
                role = ConnectionRole.CLIENT,
                isAdvertising = false,
                connectedEndpointCount = 0,
                currentQuality = 0,
            )
        )

        assertFalse(availability.canOpen)
        assertFalse(availability.canAssessQuality)
        assertEquals(NearbyAppAvailabilityState.CLIENT_NEEDS_CONNECTION, availability.state)
    }

    @Test
    fun connectedContextWithEnoughQuality_canOpenPlugin() {
        val availability = plugin.resolveNearbyAppAvailability(
            NearbyAppLaunchContext(
                role = ConnectionRole.CLIENT,
                isAdvertising = false,
                connectedEndpointCount = 1,
                currentQuality = 3,
            )
        )

        assertTrue(availability.canOpen)
        assertTrue(availability.canAssessQuality)
        assertEquals(NearbyAppAvailabilityState.READY, availability.state)
    }

    @Test
    fun connectedContextWithInsufficientQuality_blocksPlugin() {
        val availability = plugin.resolveNearbyAppAvailability(
            NearbyAppLaunchContext(
                role = ConnectionRole.HOST,
                isAdvertising = true,
                connectedEndpointCount = 1,
                currentQuality = 1,
            )
        )

        assertFalse(availability.canOpen)
        assertTrue(availability.canAssessQuality)
        assertEquals(NearbyAppAvailabilityState.QUALITY_INSUFFICIENT, availability.state)
    }

    private class TestPlugin(requiredQuality: Int) : GamePlugin(
        PluginManifest(id = "test", name = "Test", requiredQuality = requiredQuality)
    ) {
        @Composable
        override fun AppIcon(modifier: Modifier) = Unit

        @Composable
        override fun Content() = Unit
    }
}
