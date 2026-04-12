package com.hlju.funlinkbluetooth.core.plugin.api

import android.net.Uri
import java.io.InputStream

sealed class FunLinkPayload(open val id: Long) {
    data class Bytes(override val id: Long = nextId(), val data: ByteArray) : FunLinkPayload(id)
    data class File(override val id: Long = nextId(), val uri: Uri?, val fileName: String?) : FunLinkPayload(id)
    data class Stream(override val id: Long = nextId(), val inputStream: InputStream?) : FunLinkPayload(id)

    companion object {
        private val idCounter = java.util.concurrent.atomic.AtomicLong(0)
        fun nextId(): Long = idCounter.incrementAndGet()
    }
}
