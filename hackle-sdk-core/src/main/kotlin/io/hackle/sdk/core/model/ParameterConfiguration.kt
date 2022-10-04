package io.hackle.sdk.core.model

import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.core.model.Parameter.Type.*

data class ParameterConfiguration(
    val id: Long,
    val parameters: List<Parameter>
) : ParameterConfig {

    private val parameterMap = parameters.associateBy { it.key }

    override fun getString(key: String, defaultValue: String): String {
        return getOrNull(key, STRING) ?: defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getOrNull<Number>(key, NUMBER)?.toInt() ?: defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getOrNull<Number>(key, NUMBER)?.toLong() ?: defaultValue
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getOrNull<Number>(key, NUMBER)?.toDouble() ?: defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getOrNull(key, BOOLEAN) ?: defaultValue
    }

    override fun getJson(key: String, defaultValue: Map<String, Any>): Map<String, Any> {
        return getOrNull(key, JSON) ?: defaultValue
    }

    private inline fun <reified T> getOrNull(key: String, parameterType: Parameter.Type): T? {
        val parameter = parameterMap[key] ?: return null
        if (parameter.type != parameterType) {
            return null
        }
        return parameter.value as? T
    }
}
