package com.hlju.funlinkbluetooth.core.plugin.api

import com.hlju.funlinkbluetooth.core.model.NearbyEndpointInfo

interface PluginHostBindings {
    val connectedEndpointIds: List<String>
    val connectedEndpoints: List<NearbyEndpointInfo>
    val isConnected: Boolean
    val maxBytesSize: Int

    fun sendPayload(endpointIds: List<String>, payload: FunLinkPayload): Long?
    fun cancelPayload(payloadId: Long)
}
