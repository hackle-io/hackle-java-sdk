package io.hackle.sdk.core.model

internal object ValueConverter {

    fun asString(value: Any): String {
        if (value is String) {
            return value
        }

        return value.toString()
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