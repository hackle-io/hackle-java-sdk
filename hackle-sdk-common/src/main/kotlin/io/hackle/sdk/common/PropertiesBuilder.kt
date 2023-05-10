package io.hackle.sdk.common

import java.util.*


class PropertiesBuilder {

    private val properties = hashMapOf<String, Any>()

    @JvmOverloads
    fun add(properties: Map<String, Any?>, setOnce: Boolean = false): PropertiesBuilder = apply {
        for ((key, value) in properties) {
            add(key, value, setOnce)
        }
    }

    @JvmOverloads
    fun add(key: String, value: Any?, setOnce: Boolean = false): PropertiesBuilder = apply {

        if (setOnce && contains(key)) {
            return@apply
        }

        if (properties.size >= MAX_PROPERTIES_COUNT) {
            return@apply
        }

        if (key.length > MAX_PROPERTY_KEY_LENGTH) {
            return@apply
        }

        val sanitizedValue = sanitize(key, value) ?: return@apply
        properties[key] = sanitizedValue
    }

    fun remove(key: String) = apply {
        properties.remove(key)
    }

    fun remove(properties: Map<String, Any>) = apply {
        for ((key, _) in properties) {
            remove(key)
        }
    }

    fun compute(key: String, remapping: (Any?) -> Any?) = apply {
        // Do NOT use Map.compute() to support below Android 24 & JDK 1.8
        val oldValue = properties[key]
        val newValue = remapping(oldValue)
        if (newValue != null) {
            add(key, newValue)
        } else {
            if (oldValue != null || contains(key)) {
                remove(key)
            }
        }
    }

    operator fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }

    private fun sanitize(key: String, value: Any?): Any? {

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

        if (key.startsWith(SYSTEM_PROPERTY_KEY_PREFIX)) {
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
        private const val SYSTEM_PROPERTY_KEY_PREFIX = '$'
        private const val MAX_PROPERTIES_COUNT = 128
        private const val MAX_PROPERTY_KEY_LENGTH = 128
        private const val MAX_PROPERTY_VALUE_LENGTH = 1024
    }
}
