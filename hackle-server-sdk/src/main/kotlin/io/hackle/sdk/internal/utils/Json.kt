package io.hackle.sdk.internal.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal val GSON: Gson = GsonBuilder().create()

internal fun Any.toJson(): String = GSON.toJson(this)
internal inline fun <reified T> String.parseJson(): T = GSON.fromJson(this, T::class.java)
