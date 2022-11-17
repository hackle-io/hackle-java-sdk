package io.hackle.sdk.common

interface HackleRemoteConfig {

    fun getString(key: String, user: User, defaultValue: String): String

    fun getInt(key: String, user: User, defaultValue: Int): Int

    fun getLong(key: String, user: User, defaultValue: Long): Long

    fun getDouble(key: String, user: User, defaultValue: Double): Double

    fun getBoolean(key: String, user: User, defaultValue: Boolean): Boolean
}
