package io.hackle.sdk.common

import java.util.*

data class Identifiers internal constructor(
    private val identifiers: Map<String, String>
) : Map<String, String> by identifiers {

    operator fun get(type: Type): String? {
        return get(type.key)
    }

    enum class Type(val key: String) {
        ID("\$id"),
        USER("\$userId"),
        DEVICE("\$deviceId"),
    }

    internal class Builder {

        private val identifiers = hashMapOf<String, String>()

        fun add(type: Type, value: String?) = apply {
            add(type.key, value)
        }

        fun add(type: String, value: String?) = apply {
            if (value != null && isValid(type, value)) {
                identifiers[type] = value
            }
        }

        private fun isValid(type: String, value: String): Boolean {
            if (type.length > MAX_IDENTIFIER_TYPE_LENGTH) {
                return false
            }

            if (value.length > MAX_IDENTIFIER_VALUE_LENGTH) {
                return false
            }

            return true
        }

        fun build(): Identifiers {
            return Identifiers(Collections.unmodifiableMap(identifiers))
        }

    }

    companion object {
        private const val MAX_IDENTIFIER_TYPE_LENGTH = 128
        private const val MAX_IDENTIFIER_VALUE_LENGTH = 512

        internal fun builder(): Builder {
            return Builder()
        }
    }
}
