package com.hlju.funlinkbluetooth.core.plugin.api.support

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.hlju.funlinkbluetooth.core.plugin.api.TransferStatus
import com.hlju.funlinkbluetooth.core.plugin.api.TransferUpdate

class OutgoingTransferManager {
    val outgoingTrackers = mutableMapOf<Long, OutgoingPayloadTracker>()
    val progressCheckpoints = mutableMapOf<Long, Int>()
    val lastOutgoingPayloadId: MutableState<Long?> = mutableStateOf(null)

    fun handleTransferUpdate(
        endpointId: String,
        update: TransferUpdate,
        appendLog: (String) -> Unit,
        onTerminalStatus: ((payloadId: Long, endpointId: String) -> Unit)? = null,
        resolveLabel: (payloadId: Long) -> String = { "payload#${it}" }
    ) {
        val payloadId = update.payloadId
        val outgoingTracker = outgoingTrackers[payloadId]

        when (update.status) {
            TransferStatus.IN_PROGRESS -> {
                val checkpoint = PayloadHelper.calcProgressCheckpoint(update)
                if (checkpoint != null) {
                    val old = progressCheckpoints[payloadId]
                    if (old != checkpoint) {
                        progressCheckpoints[payloadId] = checkpoint
                        val label = resolveLabel(payloadId)
                        appendLog("[$endpointId] $label 传输进度: ${checkpoint * 10}%")
                    }
                }
            }

            TransferStatus.SUCCESS,
            TransferStatus.FAILURE,
            TransferStatus.CANCELED -> {
                onTerminalStatus?.invoke(payloadId, endpointId)

                if (outgoingTracker != null) {
                    outgoingTracker.completedEndpoints.add(endpointId)
                    val allDone = outgoingTracker.completedEndpoints.size >= outgoingTracker.expectedEndpoints
                    if (allDone) {
                        outgoingTrackers.remove(payloadId)
                        progressCheckpoints.remove(payloadId)
                        if (lastOutgoingPayloadId.value == payloadId) {
                            lastOutgoingPayloadId.value = null
                        }
                    }
                } else {
                    progressCheckpoints.remove(payloadId)
                }

                val label = resolveLabel(payloadId)
                appendLog(
                    "[$endpointId] $label 传输${PayloadHelper.statusLabel(update.status)} " +
                        "(${update.bytesTransferred}/${update.totalBytes})"
                )
            }
        }
    }

    fun cleanupOutgoing(payloadId: Long) {
        outgoingTrackers.remove(payloadId)
        progressCheckpoints.remove(payloadId)
        if (lastOutgoingPayloadId.value == payloadId) {
            lastOutgoingPayloadId.value = null
        }
    }
}
