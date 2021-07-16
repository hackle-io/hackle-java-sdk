package io.hackle.sdk.core.model

/**
 * @author Yong
 */
sealed class EventType {
    abstract val id: Long
    abstract val key: String

    data class Custom(
        override val id: Long,
        override val key: String
    ) : EventType()

    data class Undefined(
        override val key: String
    ) : EventType() {
        override val id: Long get() = 0L
    }
}
