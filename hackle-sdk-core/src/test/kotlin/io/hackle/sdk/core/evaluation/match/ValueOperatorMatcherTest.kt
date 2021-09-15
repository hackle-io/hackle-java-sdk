package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target.Match
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ValueOperatorMatcherTest {

    @Test
    fun `match values중 하나라도 일지하는 값이 있으면 true`() {
        // given
        val match = Match(
            type = Match.Type.MATCH,
            operator = Match.Operator.IN,
            valueType = Match.ValueType.NUMBER,
            values = listOf(1, 2, 3)
        )

        val sut = ValueOperatorMatcher(ValueOperatorMatcherFactory())

        // when
        val actual = sut.matches(3, match)

        // then
        assertTrue(actual)
    }

    @Test
    fun `match values중 일치하는 값이 하나도 없으면 false`() {
        // given
        val match = Match(
            type = Match.Type.MATCH,
            operator = Match.Operator.IN,
            valueType = Match.ValueType.NUMBER,
            values = listOf(1, 2, 3)
        )

        val sut = ValueOperatorMatcher(ValueOperatorMatcherFactory())

        // when
        val actual = sut.matches(4, match)

        // then
        assertFalse(actual)
    }

    @Test
    fun `일치하는 값이 있지만 MatchType이 NOT_MATCH면 false`() {
        // given
        val match = Match(
            type = Match.Type.NOT_MATCH,
            operator = Match.Operator.IN,
            valueType = Match.ValueType.NUMBER,
            values = listOf(1, 2, 3)
        )

        val sut = ValueOperatorMatcher(ValueOperatorMatcherFactory())

        // when
        val actual = sut.matches(3, match)

        // then
        assertFalse(actual)
    }

    @Test
    fun `일치하는 값이 없지만 MatchType이 NOT_MATCH면 true`() {
        // given
        val match = Match(
            type = Match.Type.NOT_MATCH,
            operator = Match.Operator.IN,
            valueType = Match.ValueType.NUMBER,
            values = listOf(1, 2, 3)
        )

        val sut = ValueOperatorMatcher(ValueOperatorMatcherFactory())

        // when
        val actual = sut.matches(4, match)

        // then
        assertTrue(actual)
    }
}