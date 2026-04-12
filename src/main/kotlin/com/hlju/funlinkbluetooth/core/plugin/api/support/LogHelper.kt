package com.hlju.funlinkbluetooth.core.plugin.api.support

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class EventLogType {
    SELF,
    PEER,
    SYSTEM
}

data class EventLogUi(
    val type: EventLogType,
    val content: String
)

object LogHelper {

    private const val SELF_LOG_PREFIX = "我:"
    private const val TEXT_MESSAGE_MARKER = "文本消息:"
    private const val MAX_LOG_ENTRIES = 300

    private val mainHandler = Handler(Looper.getMainLooper())

    fun createLogList(): SnapshotStateList<String> = mutableStateListOf()

    fun appendLog(logs: SnapshotStateList<String>, message: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doAppendLog(logs, message)
        } else {
            mainHandler.post { doAppendLog(logs, message) }
        }
    }

    private fun doAppendLog(logs: SnapshotStateList<String>, message: String) {
        if (logs.size >= MAX_LOG_ENTRIES) {
            logs.removeAt(0)
        }
        logs.add(message)
    }

    fun parseEventLog(raw: String): EventLogUi {
        val isSelfText = raw.startsWith(SELF_LOG_PREFIX) && raw.contains(TEXT_MESSAGE_MARKER)
        if (isSelfText) {
            return EventLogUi(
                type = EventLogType.SELF,
                content = raw.substringAfter(TEXT_MESSAGE_MARKER).trim()
            )
        }

        val isPeerText = raw.startsWith("[") && raw.contains(TEXT_MESSAGE_MARKER)
        if (isPeerText) {
            return EventLogUi(
                type = EventLogType.PEER,
                content = raw.substringAfter(TEXT_MESSAGE_MARKER).trim()
            )
        }

        return EventLogUi(
            type = EventLogType.SYSTEM,
            content = raw
        )
    }

    fun formatPeerTextLog(endpointId: String, text: String): String {
        return "[$endpointId] $TEXT_MESSAGE_MARKER $text"
    }

    fun formatSelfTextLog(text: String): String {
        return "$SELF_LOG_PREFIX $TEXT_MESSAGE_MARKER $text"
    }
}
