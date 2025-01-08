package io.hackle.sdk.core.model

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

internal class ValueConverterTest {

    @Test
    fun `string`() {
        expectThat(ValueConverter.asStringOrNull("string")).isEqualTo("string")
        expectThat(ValueConverter.asStringOrNull(42)).isEqualTo("42")
        expectThat(ValueConverter.asStringOrNull(42L)).isEqualTo("42")
        expectThat(ValueConverter.asStringOrNull(42.0)).isEqualTo("42.0")
        expectThat(ValueConverter.asStringOrNull(42.42)).isEqualTo("42.42")
        expectThat(ValueConverter.asStringOrNull(true)).isNull()
    }

    @Test
    fun `double`() {
        expectThat(ValueConverter.asDoubleOrNull(42)).isEqualTo(42.0)
        expectThat(ValueConverter.asDoubleOrNull(42L)).isEqualTo(42.0)
        expectThat(ValueConverter.asDoubleOrNull(42.0)).isEqualTo(42.0)
        expectThat(ValueConverter.asDoubleOrNull(42.42)).isEqualTo(42.42)

        expectThat(ValueConverter.asDoubleOrNull("42")).isEqualTo(42.0)
        expectThat(ValueConverter.asDoubleOrNull("42.0")).isEqualTo(42.0)
        expectThat(ValueConverter.asDoubleOrNull("42.42")).isEqualTo(42.42)

        expectThat(ValueConverter.asDoubleOrNull(false)).isNull()
    }

    @Test
    fun `boolean`() {
        expectThat(ValueConverter.asBooleanOrNull(true)).isEqualTo(true)
        expectThat(ValueConverter.asBooleanOrNull(false)).isEqualTo(false)

        expectThat(ValueConverter.asBooleanOrNull("true")).isEqualTo(true)
        expectThat(ValueConverter.asBooleanOrNull("TRUE")).isEqualTo(true)
        expectThat(ValueConverter.asBooleanOrNull("false")).isEqualTo(false)
        expectThat(ValueConverter.asBooleanOrNull("FALSE")).isEqualTo(false)

        expectThat(ValueConverter.asBooleanOrNull("trues")).isNull()
        expectThat(ValueConverter.asBooleanOrNull("strings")).isNull()
        expectThat(ValueConverter.asBooleanOrNull(1)).isNull()
        expectThat(ValueConverter.asBooleanOrNull(0)).isNull()
    }

    @Test
    fun `version`() {
        expectThat(ValueConverter.asVersionOrNull("1")).isNotNull()
        expectThat(ValueConverter.asVersionOrNull("1.0")).isNotNull()
        expectThat(ValueConverter.asVersionOrNull("1.0.0")).isNotNull()
        expectThat(ValueConverter.asVersionOrNull("1.0.0-beta")).isNotNull()
        expectThat(ValueConverter.asVersionOrNull("1.0.0-beta+42")).isNotNull()
        expectThat(ValueConverter.asVersionOrNull("1.0.0+42")).isNotNull()
        expectThat(ValueConverter.asVersionOrNull(1)).isNull()
        expectThat(ValueConverter.asVersionOrNull(true)).isNull()
    }
}