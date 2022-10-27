package io.hackle.sdk.core.model

import io.hackle.sdk.common.ParameterConfig

data class ParameterConfiguration(
    val id: Long,
    override val parameters: Map<String, Any>
) : ParameterConfig {

    override fun getString(key: String, defaultValue: String): String {
        return getOrNull(key) ?: defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getOrNull<Number>(key)?.toInt() ?: defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getOrNull<Number>(key)?.toLong() ?: defaultValue
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getOrNull<Number>(key)?.toDouble() ?: defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getOrNull(key) ?: defaultValue
    }

    private inline fun <reified T> getOrNull(key: String): T? {
        val parameterValue = parameters[key] ?: return null
        return parameterValue as? T
    }
}
