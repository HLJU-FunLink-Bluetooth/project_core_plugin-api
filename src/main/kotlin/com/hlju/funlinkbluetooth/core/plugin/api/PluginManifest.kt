package com.hlju.funlinkbluetooth.core.plugin.api

data class PluginManifest(
    val id: String,
    val name: String,
    val requiredQuality: Int = 0
)
