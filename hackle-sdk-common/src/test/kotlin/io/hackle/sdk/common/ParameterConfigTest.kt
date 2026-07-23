package io.hackle.sdk.common

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class ParameterConfigTest {

    @Nested
    inner class EmptyTest {

        @Test
        fun `parameters 는 비어있다`() {
            expectThat(ParameterConfig.empty().parameters) isEqualTo emptyMap()
        }

        @Test
        fun `모든 파라미터를 기본값으로 리턴한다`() {
            val config = ParameterConfig.empty()

            expectThat(config) {
                get { getString("key", "default") } isEqualTo "default"
                get { getInt("key", 42) } isEqualTo 42
                get { getLong("key", 42L) } isEqualTo 42L
                get { getDouble("key", 0.42) } isEqualTo 0.42
            }
            expectThat(config.getBoolean("key", true)).isTrue()
            expectThat(config.getBoolean("key", false)).isFalse()
        }
    }
}
