package io.hackle.sdk.common

import java.util.*

class PropertyOperations private constructor(
    private val operations: Map<PropertyOperation, Map<String, Any>>
) : Map<PropertyOperation, Map<String, Any>> by operations {

    class Builder {

        private val operations = hashMapOf<PropertyOperation, PropertiesBuilder>()

        fun set(key: String, value: Any?) = apply {
            add(PropertyOperation.SET, key, value)
        }

        fun setOnce(key: String, value: Any?) = apply {
            add(PropertyOperation.SET_ONCE, key, value)
        }

        fun unset(key: String) = apply {
            add(PropertyOperation.UNSET, key, "-")
        }

        fun increment(key: String, value: Any?) = apply {
            add(PropertyOperation.INCREMENT, key, value)
        }

        fun append(key: String, value: Any?) = apply {
            add(PropertyOperation.APPEND, key, value)
        }

        fun appendOnce(key: String, value: Any?) = apply {
            add(PropertyOperation.APPEND_ONCE, key, value)
        }

        fun prepend(key: String, value: Any?) = apply {
            add(PropertyOperation.PREPEND, key, value)
        }

        fun prependOnce(key: String, value: Any?) = apply {
            add(PropertyOperation.PREPEND_ONCE, key, value)
        }

        fun remove(key: String, value: Any?) = apply {
            add(PropertyOperation.REMOVE, key, value)
        }

        fun clearAll() = apply {
            add(PropertyOperation.CLEAR_ALL, "clearAll", "-")
        }

        private fun add(operation: PropertyOperation, key: String, value: Any?) {
            if (containsKey(key)) {
                return
            }
            // Do NOT use computeIfAbsent to support below Android 24 & JDK 1.8
            val builder = operations.getOrPut(operation) { PropertiesBuilder() }
            builder.add(key, value)
        }

        private fun containsKey(key: String): Boolean {
            return operations.values.any { it.contains(key) }
        }

        fun build(): PropertyOperations {
            val operations =
                operations.mapValuesTo(EnumMap(PropertyOperation::class.java)) { (_, builder) -> builder.build() }
            return PropertyOperations(Collections.unmodifiableMap(operations))
        }
    }

    companion object {

        private val EMPTY = PropertyOperations(emptyMap())

        @JvmStatic
        fun empty(): PropertyOperations {
            return EMPTY
        }

        @JvmStatic
        fun clearAll(): PropertyOperations {
            return builder().clearAll().build()
        }

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
