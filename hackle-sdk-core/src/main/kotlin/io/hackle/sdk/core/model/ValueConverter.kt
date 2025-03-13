package io.hackle.sdk.core.model

internal object ValueConverter {
    fun asStringOrNull(value: Any?): String? {
        return when (value) {
            is String -> return value
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> null
        }
    }

    fun asDoubleOrNull(value: Any?): Double? {
        return when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    fun asBooleanOrNull(value: Any?): Boolean? {
        return when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> null
        }
    }

    fun asVersionOrNull(value: Any?): Version? {
        return Version.parseOrNull(value)
    }

    private fun String.toBoolean(): Boolean? {
        if (length > 5) return null
        // "true" or "false"
        return when (this) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

}
