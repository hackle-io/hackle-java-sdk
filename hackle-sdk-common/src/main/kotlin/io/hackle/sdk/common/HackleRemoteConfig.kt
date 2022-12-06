package io.hackle.sdk.common

interface HackleRemoteConfig : Config {
    override fun getString(key: String, defaultValue: String): String

    override fun getInt(key: String, defaultValue: Int): Int

    override fun getLong(key: String, defaultValue: Long): Long

    override fun getDouble(key: String, defaultValue: Double): Double

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean
}
