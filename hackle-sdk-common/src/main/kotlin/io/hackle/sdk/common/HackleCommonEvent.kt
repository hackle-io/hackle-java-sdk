package io.hackle.sdk.common

interface HackleCommonEvent {
    val key: String
    val value: Double?
    val properties: Map<String, Any>
    val internalProperties: Map<String, Any>?
}