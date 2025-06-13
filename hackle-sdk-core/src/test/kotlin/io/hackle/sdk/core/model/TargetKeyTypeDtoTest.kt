package io.hackle.sdk.core.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TargetKeyTypeDtoTest {
    @Test
    fun `Should return TargetKeyTypeDto when rawValue is valid`() {
        // given
        val rawValue = "USER_PROPERTY"

        // when
        val actual = TargetKeyTypeDto.from(rawValue)

        // then
        assertThat(actual).isNotNull()
        assertThat(actual?.type).isEqualTo(Target.Key.Type.USER_PROPERTY)
    }

    @Test
    fun `Should return null when rawValue is invalid`() {
        // given
        val rawValue = "INVALID_TYPE"

        // when
        val actual = TargetKeyTypeDto.from(rawValue)

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `Should return null when rawValue is in lowercase`() {
        // given
        val rawValue = "user_property"

        // when
        val actual = TargetKeyTypeDto.from(rawValue)

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `Should return null when rawValue is empty`() {
        // given
        val rawValue = ""

        // when
        val actual = TargetKeyTypeDto.from(rawValue)

        // then
        assertThat(actual).isNull()
    }
}
