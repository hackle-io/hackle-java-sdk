package io.hackle.sdk.common

interface HackleCommonEvent {
    val key: String
    val value: Double?
    val properties: Map<String, Any>
    val internalProperties: Map<String, Any>?

    abstract class HackleCommonEventBuilder<T : HackleCommonEvent>(protected val key: String) {
        protected var value: Double? = null
        protected val properties = PropertiesBuilder()
        protected var internalProperties: PropertiesBuilder? = null

        fun value(value: Double) = apply { this.value = value }
        fun property(key: String, value: Any?) = apply { this.properties.add(key, value) }
        fun properties(properties: Map<String, Any?>?) = apply { properties?.let { this.properties.add(it) } }

        abstract fun build(): T
    }
}
