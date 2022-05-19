package io.hackle.sdk.common

import java.util.*


internal class PropertiesBuilder {

    private val properties = hashMapOf<String, Any>()

    fun add(key: String, value: Any): PropertiesBuilder = apply {
        if (isValid(key, value)) {
            properties[key] = value
        }
    }

    private fun isValid(key: String, value: Any): Boolean {
        if (properties.size >= MAX_PROPERTIES_COUNT) {
            return false
        }

        if (key.length > MAX_PROPERTY_KEY_LENGTH) {
            return false
        }

        return when (value) {
            is String -> value.length <= MAX_PROPERTY_VALUE_LENGTH
            is Number -> true
            is Boolean -> true
            else -> false
        }
    }

    fun build(): Map<String, Any> {
        return Collections.unmodifiableMap(properties)
    }

    companion object {
        private const val MAX_PROPERTIES_COUNT = 128
        private const val MAX_PROPERTY_KEY_LENGTH = 128
        private const val MAX_PROPERTY_VALUE_LENGTH = 1024
    }
}
