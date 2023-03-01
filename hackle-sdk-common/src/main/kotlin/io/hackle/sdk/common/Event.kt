package io.hackle.sdk.common

/**
 * @author Yong
 */
data class Event internal constructor(
    val key: String,
    val value: Double?,
    val properties: Map<String, Any>
) {

    class Builder(private val key: String) {
        private var value: Double? = null
        private val properties = PropertiesBuilder()
        fun value(value: Double) = apply { this.value = value }
        fun property(key: String, value: Any?) = apply { this.properties.add(key, value) }
        fun properties(properties: Map<String, Any?>) = apply { this.properties.add(properties) }
        fun build() = Event(key, value, properties.build())
    }

    companion object {

        @JvmStatic
        fun of(key: String) = Event(key, null, emptyMap())

        @JvmStatic
        fun builder(key: String) = Builder(key)
    }
}
