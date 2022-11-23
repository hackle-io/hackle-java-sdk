package io.hackle.sdk.common

interface Config {
    fun getString(key: String, defaultValue: String): String

    fun getInt(key: String, defaultValue: Int): Int

    fun getLong(key: String, defaultValue: Long): Long

    fun getDouble(key: String, defaultValue: Double): Double

    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}
