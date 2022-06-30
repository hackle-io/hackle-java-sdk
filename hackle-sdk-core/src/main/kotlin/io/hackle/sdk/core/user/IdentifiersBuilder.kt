package io.hackle.sdk.core.user

import java.util.*

internal class IdentifiersBuilder {

    private val identifiers = hashMapOf<String, String>()

    fun add(identifiers: Map<String, String>) = apply {
        for ((type, value) in identifiers) {
            add(type, value)
        }
    }

    fun add(type: IdentifierType, value: String?) = apply {
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
