package io.hackle.sdk.core.model

internal object ValueConverter {
    fun asStringOrNull(value: Any): String? {
        return when (value) {
            is String -> return value
            is Number -> value.toString()
            else -> null
        }
    }

    fun asDoubleOrNull(value: Any): Double? {
        return when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    fun asBooleanOrNull(value: Any): Boolean? {
        return value as? Boolean
    }

    fun asVersionOrNull(value: Any): Version? {
        return Version.parseOrNull(value)
    }
}
