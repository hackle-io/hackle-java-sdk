package io.hackle.sdk.internal.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

internal val OBJECT_MAPPER: ObjectMapper =
    jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

internal fun Any.toJson(): String = OBJECT_MAPPER.writeValueAsString(this)
internal inline fun <reified T> String.parseJson(): T = OBJECT_MAPPER.readValue(this)
