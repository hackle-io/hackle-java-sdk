package io.hackle.sdk.common

import java.util.*


class PropertiesBuilder {

    private val properties = hashMapOf<String, Any>()

    fun add(properties: Map<String, Any?>): PropertiesBuilder = apply {
        for ((key, value) in properties) {
            add(key, value)
        }
    }

    fun add(key: String, value: Any?): PropertiesBuilder = apply {
        if (properties.size >= MAX_PROPERTIES_COUNT) {
            return@apply
        }

        if (key.length > MAX_PROPERTY_KEY_LENGTH) {
            return@apply
        }

        val sanitizedValue = sanitize(value) ?: return@apply
        properties[key] = sanitizedValue
    }

    private fun sanitize(value: Any?): Any? {

        if (value == null) {
            return null
        }

        if (value is Collection<*>) {
            return value.asSequence().filterNotNull().filter { isValidElement(it) }.toList()
        }

        if (value is Array<*>) {
            return value.asSequence().filterNotNull().filter { isValidElement(it) }.toList()
        }

        if (isValidValue(value)) {
            return value
        }

        return null
    }

    private fun isValidValue(value: Any): Boolean {
        return when (value) {
            is String -> value.length <= MAX_PROPERTY_VALUE_LENGTH
            is Number -> true
            is Boolean -> true
            else -> false
        }
    }

    private fun isValidElement(element: Any): Boolean {
        return when (element) {
            is String -> element.length <= MAX_PROPERTY_VALUE_LENGTH
            is Number -> true
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
