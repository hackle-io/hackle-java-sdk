package io.hackle.sdk.common

import java.util.*

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
        private val properties = mutableMapOf<String, Any>()
        fun value(value: Double) = apply { this.value = value }
        fun property(key: String, value: Int) = apply { properties[key] = value }
        fun property(key: String, value: Long) = apply { properties[key] = value }
        fun property(key: String, value: Double) = apply { properties[key] = value }
        fun property(key: String, value: String?) = apply { value?.let { properties[key] = it } }
        fun property(key: String, value: Boolean) = apply { properties[key] = value }
        fun build() = Event(key, value, Collections.unmodifiableMap(properties))
    }

    companion object {

        @JvmStatic
        fun of(key: String) = Event(key, null, emptyMap())

        @JvmStatic
        fun builder(key: String) = Builder(key)
    }
}
