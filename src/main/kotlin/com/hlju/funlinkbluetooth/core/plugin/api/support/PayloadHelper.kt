package com.hlju.funlinkbluetooth.core.plugin.api.support

import androidx.compose.runtime.MutableState
import com.hlju.funlinkbluetooth.core.plugin.api.FunLinkPayload
import com.hlju.funlinkbluetooth.core.plugin.api.PluginHostBindings
import com.hlju.funlinkbluetooth.core.plugin.api.TransferStatus
import com.hlju.funlinkbluetooth.core.plugin.api.TransferUpdate

object PayloadHelper {

    fun sendPayloadToConnectedEndpoints(
        hostBindings: PluginHostBindings,
        payload: FunLinkPayload,
        label: String,
        shouldCloseWhenFinished: Boolean,
        outgoingTrackers: MutableMap<Long, OutgoingPayloadTracker>,
        lastOutgoingPayloadId: MutableState<Long?>,
        appendLog: (String) -> Unit
    ): Long? {
        val endpointIds = hostBindings.connectedEndpointIds
        if (endpointIds.isEmpty()) {
            appendLog("--- 未连接设备，无法发送 $label ---")
            return null
        }

        val result = hostBindings.sendPayload(endpointIds, payload)
        if (result == null) {
            appendLog("--- 发送失败：$label ---")
            return null
        }

        outgoingTrackers[result] = OutgoingPayloadTracker(
            label = label,
            payload = if (shouldCloseWhenFinished) payload else null,
            expectedEndpoints = endpointIds.size
        )
        lastOutgoingPayloadId.value = result

        appendLog("--- 开始发送 $label (id=$result) 到 ${endpointIds.size} 个端点 ---")

        return result
    }

    fun cancelLastPayload(
        hostBindings: PluginHostBindings,
        lastOutgoingPayloadId: MutableState<Long?>,
        appendLog: (String) -> Unit
    ) {
        val payloadId = lastOutgoingPayloadId.value
        if (payloadId == null) {
            appendLog("--- 暂无可取消的发送任务 ---")
            return
        }

        hostBindings.cancelPayload(payloadId)
        appendLog("--- 已请求取消发送：$payloadId ---")
        lastOutgoingPayloadId.value = null
    }

    fun calcProgressCheckpoint(update: TransferUpdate): Int? {
        val total = update.totalBytes
        if (total <= 0L) return null
        val percent = ((update.bytesTransferred * 100L) / total)
            .toInt()
            .coerceIn(0, 100)
        return percent / 10
    }

    fun statusLabel(status: TransferStatus): String {
        return when (status) {
            TransferStatus.SUCCESS -> "成功"
            TransferStatus.FAILURE -> "失败"
            TransferStatus.CANCELED -> "已取消"
            TransferStatus.IN_PROGRESS -> "传输中"
        }
    }
}
