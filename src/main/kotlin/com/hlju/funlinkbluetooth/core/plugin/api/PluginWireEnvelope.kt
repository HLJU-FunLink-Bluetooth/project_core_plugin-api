package com.hlju.funlinkbluetooth.core.plugin.api

import org.json.JSONObject
import java.util.Base64

sealed interface PluginWireEnvelope {
    val pluginId: String

    fun encode(): ByteArray

    data class BytesMessage(
        override val pluginId: String,
        val data: ByteArray
    ) : PluginWireEnvelope {
        override fun encode(): ByteArray = JSONObject()
            .put(KEY_VERSION, PROTOCOL_VERSION)
            .put(KEY_KIND, KIND_BYTES)
            .put(KEY_PLUGIN_ID, pluginId)
            .put(KEY_DATA, Base64.getEncoder().encodeToString(data))
            .toString()
            .toByteArray(Charsets.UTF_8)
    }

    data class PayloadMeta(
        override val pluginId: String,
        val payloadId: Long,
        val payloadType: PayloadType,
        val fileName: String? = null
    ) : PluginWireEnvelope {
        override fun encode(): ByteArray = JSONObject()
            .put(KEY_VERSION, PROTOCOL_VERSION)
            .put(KEY_KIND, KIND_PAYLOAD_META)
            .put(KEY_PLUGIN_ID, pluginId)
            .put(KEY_PAYLOAD_ID, payloadId)
            .put(KEY_PAYLOAD_TYPE, payloadType.wireName)
            .apply {
                if (fileName != null) {
                    put(KEY_FILE_NAME, fileName)
                }
            }
            .toString()
            .toByteArray(Charsets.UTF_8)
    }

    enum class PayloadType(val wireName: String) {
        FILE("file"),
        STREAM("stream");

        companion object {
            fun fromWireName(value: String?): PayloadType? = entries.firstOrNull { it.wireName == value }
        }
    }

    companion object {
        private const val PROTOCOL_VERSION = 1
        private const val KEY_VERSION = "v"
        private const val KEY_KIND = "kind"
        private const val KEY_PLUGIN_ID = "pluginId"
        private const val KEY_DATA = "dataBase64"
        private const val KEY_PAYLOAD_ID = "payloadId"
        private const val KEY_PAYLOAD_TYPE = "payloadType"
        private const val KEY_FILE_NAME = "fileName"

        private const val KIND_BYTES = "bytes"
        private const val KIND_PAYLOAD_META = "payload_meta"

        fun decode(bytes: ByteArray): PluginWireEnvelope? {
            return try {
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                if (json.optInt(KEY_VERSION, -1) != PROTOCOL_VERSION) {
                    return null
                }

                val pluginId = json.optString(KEY_PLUGIN_ID).takeIf { it.isNotBlank() } ?: return null
                when (json.optString(KEY_KIND)) {
                    KIND_BYTES -> {
                        val encoded = json.optString(KEY_DATA).takeIf { it.isNotBlank() } ?: return null
                        BytesMessage(
                            pluginId = pluginId,
                            data = Base64.getDecoder().decode(encoded)
                        )
                    }

                    KIND_PAYLOAD_META -> {
                        val payloadType = PayloadType.fromWireName(json.optString(KEY_PAYLOAD_TYPE)) ?: return null
                        val payloadId = json.optLong(KEY_PAYLOAD_ID, -1L)
                        if (payloadId < 0L) {
                            return null
                        }
                        PayloadMeta(
                            pluginId = pluginId,
                            payloadId = payloadId,
                            payloadType = payloadType,
                            fileName = json.optString(KEY_FILE_NAME).takeIf { it.isNotBlank() }
                        )
                    }

                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }

        fun bytes(pluginId: String, data: ByteArray): ByteArray {
            return BytesMessage(pluginId = pluginId, data = data).encode()
        }

        fun payloadMeta(
            pluginId: String,
            payloadId: Long,
            payloadType: PayloadType,
            fileName: String? = null
        ): ByteArray {
            return PayloadMeta(
                pluginId = pluginId,
                payloadId = payloadId,
                payloadType = payloadType,
                fileName = fileName
            ).encode()
        }
    }
}
