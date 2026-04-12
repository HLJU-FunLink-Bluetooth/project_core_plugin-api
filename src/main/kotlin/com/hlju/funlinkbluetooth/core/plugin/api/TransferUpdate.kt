package com.hlju.funlinkbluetooth.core.plugin.api

data class TransferUpdate(
    val payloadId: Long,
    val status: TransferStatus,
    val bytesTransferred: Long,
    val totalBytes: Long
)

enum class TransferStatus {
    IN_PROGRESS,
    SUCCESS,
    FAILURE,
    CANCELED
}
