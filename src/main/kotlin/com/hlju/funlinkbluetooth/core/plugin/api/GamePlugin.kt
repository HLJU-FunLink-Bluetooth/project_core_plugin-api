package com.hlju.funlinkbluetooth.core.plugin.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.hlju.funlinkbluetooth.core.model.NearbyEndpointInfo
import com.hlju.funlinkbluetooth.core.plugin.api.support.LogHelper

abstract class GamePlugin(
    val manifest: PluginManifest
) {
    val id: String get() = manifest.id
    val name: String get() = manifest.name
    val requiredQuality: Int get() = manifest.requiredQuality

    open val displayName: String
        get() = name

    private var _hostBindings: PluginHostBindings? = null

    val hostBindings: PluginHostBindings
        get() = _hostBindings ?: error("hostBindings not set. Call bind() first.")

    fun bind(bindings: PluginHostBindings) {
        _hostBindings = bindings
    }

    val connectedEndpointIds: List<String>
        get() = hostBindings.connectedEndpointIds

    val connectedEndpoints: List<NearbyEndpointInfo>
        get() = hostBindings.connectedEndpoints

    val isConnected: Boolean
        get() = hostBindings.isConnected

    fun canRunWithQuality(quality: Int): Boolean = quality >= requiredQuality

    val eventLogs: SnapshotStateList<String> = LogHelper.createLogList()

    protected fun appendLog(message: String) {
        LogHelper.appendLog(eventLogs, message)
    }

    @Composable
    abstract fun AppIcon(modifier: Modifier = Modifier)

    @Composable
    abstract fun Content()

    open fun onEndpointConnected(endpointId: String, endpointName: String) {}
    open fun onMessageReceived(endpointId: String, bytes: ByteArray) {}
    open fun onPayloadReceived(endpointId: String, payload: FunLinkPayload) {}
    open fun onPayloadTransferUpdate(endpointId: String, update: TransferUpdate) {}
    open fun onBandwidthChanged(endpointId: String, quality: Int) {}
    open fun onEndpointDisconnected(endpointId: String) {}
}
