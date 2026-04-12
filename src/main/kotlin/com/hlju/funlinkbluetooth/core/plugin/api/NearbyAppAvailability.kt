package com.hlju.funlinkbluetooth.core.plugin.api

import com.hlju.funlinkbluetooth.core.designsystem.widget.qualityLabel
import com.hlju.funlinkbluetooth.core.model.ConnectionRole

data class NearbyAppLaunchContext(
    val role: ConnectionRole,
    val isAdvertising: Boolean,
    val connectedEndpointCount: Int,
    val currentQuality: Int,
) {
    val hasConnectedPeers: Boolean
        get() = connectedEndpointCount > 0
}

enum class NearbyAppAvailabilityState {
    HOST_NEEDS_ROOM,
    WAITING_FOR_CONNECTION,
    CLIENT_NEEDS_CONNECTION,
    QUALITY_INSUFFICIENT,
    READY,
}

data class NearbyAppAvailability(
    val canOpen: Boolean,
    val canAssessQuality: Boolean,
    val state: NearbyAppAvailabilityState,
    val badgeText: String,
    val summaryText: String,
    val qualityText: String? = null,
)

fun GamePlugin.resolveNearbyAppAvailability(context: NearbyAppLaunchContext): NearbyAppAvailability {
    if (context.hasConnectedPeers) {
        val qualityText = if (requiredQuality > 0) {
            "当前带宽 ${qualityLabel(context.currentQuality)}，插件要求 ${qualityLabel(requiredQuality)}。"
        } else {
            "当前带宽 ${qualityLabel(context.currentQuality)}，此程序无额外带宽要求。"
        }
        if (!canRunWithQuality(context.currentQuality)) {
            return NearbyAppAvailability(
                canOpen = false,
                canAssessQuality = true,
                state = NearbyAppAvailabilityState.QUALITY_INSUFFICIENT,
                badgeText = "需 ${qualityLabel(requiredQuality)}",
                summaryText = "当前连接质量不足，需要至少 ${qualityLabel(requiredQuality)}。",
                qualityText = qualityText,
            )
        }
        return NearbyAppAvailability(
            canOpen = true,
            canAssessQuality = true,
            state = NearbyAppAvailabilityState.READY,
            badgeText = "可使用",
            summaryText = "已连接，可直接使用。",
            qualityText = qualityText,
        )
    }

    if (context.role == ConnectionRole.HOST && context.isAdvertising) {
        return NearbyAppAvailability(
            canOpen = true,
            canAssessQuality = false,
            state = NearbyAppAvailabilityState.WAITING_FOR_CONNECTION,
            badgeText = "待连接",
            summaryText = "等待设备加入后评估带宽。",
        )
    }

    if (context.role == ConnectionRole.HOST) {
        return NearbyAppAvailability(
            canOpen = false,
            canAssessQuality = false,
            state = NearbyAppAvailabilityState.HOST_NEEDS_ROOM,
            badgeText = "先建房",
            summaryText = "先创建房间后使用。",
        )
    }

    return NearbyAppAvailability(
        canOpen = false,
        canAssessQuality = false,
        state = NearbyAppAvailabilityState.CLIENT_NEEDS_CONNECTION,
        badgeText = "先连接",
        summaryText = "先连接房间后使用。",
    )
}
