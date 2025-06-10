package io.hackle.sdk.common

/**
 * @author Yong
 */
data class Event internal constructor(
    override val key: String,
    override val value: Double?,
    override val properties: Map<String, Any>
) : HackleCommonEvent {
    override val internalProperties: Map<String, Any>? = null

    class Builder(key: String) : HackleCommonEvent.HackleCommonEventBuilder<Event>(key) {
        override fun build() = Event(key, value, properties.build())
    }

    companion object {

        @JvmStatic
        fun of(key: String) = Event(key, null, emptyMap())

        @JvmStatic
        fun builder(key: String) = Builder(key)
    }
}
