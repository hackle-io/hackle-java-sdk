package io.hackle.sdk.core.model

import io.hackle.sdk.common.ParameterConfig

data class ParameterConfiguration(
    val id: Long,
    val parameters: List<Parameter>
) : ParameterConfig {

    private val parameterMap = parameters.associateBy { it.key }

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
        val parameter = parameterMap[key] ?: return null
        return parameter.value as? T
    }
}
