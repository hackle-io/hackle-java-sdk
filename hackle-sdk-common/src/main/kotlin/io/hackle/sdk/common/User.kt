package io.hackle.sdk.common

import java.util.*

/**
 * @author Yong
 */
data class User internal constructor(
    val id: String,
    val properties: Map<String, Any>
) {

    class Builder(private val id: String) {
        private val properties = mutableMapOf<String, Any>()
        fun property(key: String, value: Int) = apply { properties[key] = value }
        fun property(key: String, value: Long) = apply { properties[key] = value }
        fun property(key: String, value: Double) = apply { properties[key] = value }
        fun property(key: String, value: Boolean) = apply { properties[key] = value }
        fun property(key: String, value: String?) = apply { value?.let { properties[key] = it } }
        fun build() = User(id, Collections.unmodifiableMap(properties))
    }

    companion object {

        @JvmStatic
        fun of(id: String): User = User(id, emptyMap())

        @JvmStatic
        fun builder(id: String): Builder = Builder(id)
    }
}
