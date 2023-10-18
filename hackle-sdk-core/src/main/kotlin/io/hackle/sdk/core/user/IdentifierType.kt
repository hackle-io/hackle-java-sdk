package io.hackle.sdk.core.user

import io.hackle.sdk.common.IdentifiersBuilder

enum class IdentifierType(
    val key: String
) {
    ID("\$id"),
    USER("\$userId"),
    DEVICE("\$deviceId"),
    SESSION("\$sessionId"),
    HACKLE_DEVICE_ID("\$hackleDeviceId"),
}

fun IdentifiersBuilder.add(type: IdentifierType, value: String?, overwrite: Boolean = true) = apply {
    add(type.key, value, overwrite)
}