package io.hackle.sdk.common

interface ParameterConfig {

    fun getString(key: String, defaultValue: String): String

    fun getInt(key: String, defaultValue: Int): Int

    fun getLong(key: String, defaultValue: Long): Long

    fun getDouble(key: String, defaultValue: Double): Double

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    companion object {
        fun empty(): ParameterConfig {
            return EmptyParameterConfig
        }
    }
}

private object EmptyParameterConfig : ParameterConfig {
    override fun getString(key: String, defaultValue: String): String = defaultValue
    override fun getInt(key: String, defaultValue: Int): Int = defaultValue
    override fun getLong(key: String, defaultValue: Long): Long = defaultValue
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
    override fun getDouble(key: String, defaultValue: Double): Double = defaultValue
}
