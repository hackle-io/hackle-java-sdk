package io.hackle.sdk.core.model

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

internal class ParameterConfigurationTest {

    @Test
    fun `ParameterConfiguration`() {

        val parameterConfiguration = ParameterConfiguration(
            42,
            mapOf(
                Pair("string_key", "string_value"),
                Pair("empty_string_key", ""),
                Pair("int_key", 42.0),
                Pair("zero_int_key", 0),
                Pair("negative_int_key", -1),
                Pair("max_int_key", Int.MAX_VALUE),
                Pair("long_key", 320.0),
                Pair("long_key2", 92147483647.0),
                Pair("double_key", 0.42),
                Pair("true_boolean_key", true),
                Pair("false_boolean_key", false),
            )
        )

        expectThat(parameterConfiguration) {

            get { id } isEqualTo 42
            get { parameters } hasSize 11

            get { getString("string_key", "!!") } isEqualTo "string_value"
            get { getString("empty_string_key", "!!") } isEqualTo ""
            get { getString("invalid_key", "!!") } isEqualTo "!!"

            get { getInt("int_key", 999) } isEqualTo 42
            get { getInt("zero_int_key", 999) } isEqualTo 0
            get { getInt("negative_int_key", 999) } isEqualTo -1
            get { getInt("max_int_key", 999) } isEqualTo 2147483647
            get { getInt("invalid_int_key", 999) } isEqualTo 999
            get { getInt("double_key", 999) } isEqualTo 0

            get { getLong("long_key", 999L) } isEqualTo 320L
            get { getLong("long_key2", 999L) } isEqualTo 92147483647L
            get { getLong("invalid_long_key", 999L) } isEqualTo 999L

            get { getDouble("double_key", 99.9) } isEqualTo 0.42
            get { getDouble("invalid_double_key", 99.9) } isEqualTo 99.9
            get { getDouble("int_key", 99.9) } isEqualTo 42.0

            get { getBoolean("true_boolean_key", false) } isEqualTo true
            get { getBoolean("false_boolean_key", true) } isEqualTo false
            get { getBoolean("invalid_boolean_key", true) } isEqualTo true
        }
    }
}
