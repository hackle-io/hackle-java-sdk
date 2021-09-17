package io.hackle.sdk.internal.workspace

import java.util.*

/**
 * @author Yong
 */
internal class Sdk(
    val key: String,
    val name: String,
    val version: String
)

internal fun loadVersion(): String =
    try {
        val properties = Sdk::class.java.getResourceAsStream("/hackle-server-sdk.properties").use {
            Properties().apply { load(it) }
        }
        properties.getProperty("sdk.version", "unknown")
    } catch (e: Exception) {
        "unknown"
    }
