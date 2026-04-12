package com.hlju.funlinkbluetooth.core.plugin.api.support

import com.hlju.funlinkbluetooth.core.plugin.api.FunLinkPayload

data class OutgoingPayloadTracker(
    val label: String,
    val payload: FunLinkPayload?,
    val expectedEndpoints: Int,
    val completedEndpoints: MutableSet<String> = mutableSetOf()
)
