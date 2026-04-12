package com.hlju.funlinkbluetooth.core.plugin.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.hlju.funlinkbluetooth.core.model.NearbyEndpointInfo
import com.hlju.funlinkbluetooth.core.plugin.api.support.OutgoingPayloadTracker
import com.hlju.funlinkbluetooth.core.plugin.api.support.PayloadHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePluginTest {

    @Test
    fun bind_allowsRebindingAndUsesLatestBindings() {
        val plugin = TestPlugin()
        val first = FakeBindings()
        val second = FakeBindings()

        plugin.bind(first)
        plugin.bind(second)

        assertSame(second, plugin.hostBindings)
    }

    @Test
    fun payloadHelper_tracksAndCancelsRealNearbyPayloadId() {
        val bindings = FakeBindings(sentPayloadId = 88L)
        val payload = FunLinkPayload.Bytes(data = "hello".toByteArray())
        val outgoingTrackers = mutableMapOf<Long, OutgoingPayloadTracker>()
        val lastOutgoingPayloadId = mutableStateOf<Long?>(null)
        val logs = mutableListOf<String>()

        val result = PayloadHelper.sendPayloadToConnectedEndpoints(
            hostBindings = bindings,
            payload = payload,
            label = "文本消息",
            shouldCloseWhenFinished = false,
            outgoingTrackers = outgoingTrackers,
            lastOutgoingPayloadId = lastOutgoingPayloadId,
            appendLog = logs::add
        )

        assertEquals(88L, result)
        assertFalse(outgoingTrackers.containsKey(payload.id))
        assertTrue(outgoingTrackers.containsKey(88L))
        assertEquals(88L, lastOutgoingPayloadId.value)

        PayloadHelper.cancelLastPayload(
            hostBindings = bindings,
            lastOutgoingPayloadId = lastOutgoingPayloadId,
            appendLog = logs::add
        )

        assertEquals(88L, bindings.canceledPayloadId)
    }

    private class TestPlugin : GamePlugin(PluginManifest(id = "test", name = "Test")) {
        @Composable
        override fun AppIcon(modifier: Modifier) = Unit

        @Composable
        override fun Content() = Unit
    }

    private class FakeBindings(
        private val sentPayloadId: Long = 1L
    ) : PluginHostBindings {
        var canceledPayloadId: Long? = null

        override val connectedEndpointIds: List<String> = listOf("peer-a", "peer-b")
        override val connectedEndpoints: List<NearbyEndpointInfo> = emptyList()
        override val isConnected: Boolean = true
        override val maxBytesSize: Int = 32768

        override fun sendPayload(endpointIds: List<String>, payload: FunLinkPayload): Long? = sentPayloadId

        override fun cancelPayload(payloadId: Long) {
            canceledPayloadId = payloadId
        }
    }
}
