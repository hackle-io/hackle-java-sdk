package io.hackle.sdk.internal.workspace

import java.util.*

/**
 * @author Yong
 */
internal class Sdk(
    val key: String,
    val name: String,
    val version: String
) {
    companion object {
        fun load(sdkKey: String): Sdk {
            return try {
                val properties = Sdk::class.java.getResourceAsStream("/hackle-server-sdk.properties").use {
                    Properties().apply { load(it) }
                }
                val sdkName = properties.getProperty("sdk.name", "unknown")
                val sdkVersion = properties.getProperty("sdk.version", "unknown")
                Sdk(sdkKey, sdkName, sdkVersion)
            } catch (e: Exception) {
                Sdk(sdkKey, "unknown", "unknown")
            }
        }
    }
}
