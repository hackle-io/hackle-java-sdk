package io.hackle.sdk.common

import java.util.*

class IdentifiersBuilder {

    private val identifiers = hashMapOf<String, String>()

    fun add(identifiers: Map<String, String>, overwrite: Boolean = true) = apply {
        for ((type, value) in identifiers) {
            add(type, value, overwrite)
        }
    }

    fun add(type: String, value: String?, overwrite: Boolean = true) = apply {

        if (!overwrite && identifiers.containsKey(type)) {
            return@apply
        }

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

        if (value.isBlank()) {
            return false
        }

        return true
    }

    fun build(): Map<String, String> {
        return Collections.unmodifiableMap(identifiers)
    }

    companion object {
        private const val MAX_IDENTIFIER_TYPE_LENGTH = 128
        private const val MAX_IDENTIFIER_VALUE_LENGTH = 512
    }
}
