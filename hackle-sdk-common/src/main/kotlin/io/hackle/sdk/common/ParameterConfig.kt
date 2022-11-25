package io.hackle.sdk.common

interface ParameterConfig : Config {

    val parameters: Map<String, Any>

    override fun getString(key: String, defaultValue: String): String

    override fun getInt(key: String, defaultValue: Int): Int

    override fun getLong(key: String, defaultValue: Long): Long

    override fun getDouble(key: String, defaultValue: Double): Double

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean

    companion object {
        fun empty(): ParameterConfig {
            return EmptyParameterConfig
        }
    }
}

private object EmptyParameterConfig : ParameterConfig {
    override val parameters: Map<String, Any> = emptyMap()
    override fun getString(key: String, defaultValue: String): String = defaultValue
    override fun getInt(key: String, defaultValue: Int): Int = defaultValue
    override fun getLong(key: String, defaultValue: Long): Long = defaultValue
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
    override fun getDouble(key: String, defaultValue: Double): Double = defaultValue
}
