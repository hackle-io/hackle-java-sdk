package io.hackle.sdk.core.user

enum class IdentifierType(
    val key: String
) {
    ID("\$id"),
    USER("\$userId"),
    DEVICE("\$deviceId"),
    SESSION("\$sessionId"),

    HACKLE_DEVICE_ID("\$hackleDeviceId"),
}
