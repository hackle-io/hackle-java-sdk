package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target.Match
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.Target.Match.Type.NOT_MATCH
import io.hackle.sdk.core.model.ValueType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValueOperatorMatcherTest {

    val sut = ValueOperatorMatcher(ValueOperatorMatcherFactory())

    @Test
    fun `matches`() {

        // A 는 [A] 중 하나
        verify(MATCH, "A", listOf("A"), true)

        // A 는 [A, B] 중 하나
        verify(MATCH, "A", listOf("A", "B"), true)

        // B 는 [A, B] 중 하나
        verify(MATCH, "B", listOf("A", "B"), true)

        // A 는 [B] 중 하나
        verify(MATCH, "A", listOf("B"), false)

        // A 는 [B, C] 중 하나
        verify(MATCH, "A", listOf("B", "C"), false)

        // [] 는 [A] 중 하나
        verify(MATCH, listOf<String>(), listOf("A"), false)

        // [A] 는 [A] 중 하나
        verify(MATCH, listOf("A"), listOf("A"), true)

        // [A] 는 [A, B] 중 하나
        verify(MATCH, listOf("A"), listOf("A", "B"), true)

        // [B] 는 [A, B] 중 하나
        verify(MATCH, listOf("B"), listOf("A", "B"), true)

        // [A] 는 [B] 중 하나
        verify(MATCH, listOf("A"), listOf("B"), false)

        // [A] 는 [B, C] 중 하나
        verify(MATCH, listOf("A"), listOf("B", "C"), false)

        // [A, B] 는 [A] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("A"), true)

        // [A, B] 는 [B] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("B"), true)

        // [A, B] 는 [C] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("C"), false)

        // [A, B] 는 [A, B] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("A", "B"), true)

        // [A, B] 는 [A, C] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("A", "C"), true)

        // [A, B] 는 [B, C] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("B", "C"), true)

        // [A, B] 는 [A, C] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("A", "C"), true)

        // [A, B] 는 [C, A] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("C", "A"), true)

        // [A, B] 는 [C, D] 중 하나
        verify(MATCH, listOf("A", "B"), listOf("C", "D"), false)

        // A 는 [A] 중 하나가 아닌
        verify(NOT_MATCH, "A", listOf("A"), false)

        // A 는 [A, B] 중 하나가 아닌
        verify(NOT_MATCH, "A", listOf("A", "B"), false)

        // B 는 [A, B] 중 하나가 아닌
        verify(NOT_MATCH, "B", listOf("A", "B"), false)

        // A 는 [B] 중 하나가 아닌
        verify(NOT_MATCH, "A", listOf("B"), true)

        // A 는 [B, C] 중 하나가 아닌
        verify(NOT_MATCH, "A", listOf("B", "C"), true)

        // [] 는 [A] 중 하나가 아닌
        verify(NOT_MATCH, listOf<String>(), listOf("A"), true)

        // [A] 는 [A] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A"), listOf("A"), false)

        // [A] 는 [A, B] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A"), listOf("A", "B"), false)

        // [B] 는 [A, B] 중 하나가 아닌
        verify(NOT_MATCH, listOf("B"), listOf("A", "B"), false)

        // [A] 는 [B] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A"), listOf("B"), true)

        // [A] 는 [B, C] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A"), listOf("B", "C"), true)

        // [A, B] 는 [A] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("A"), false)

        // [A, B] 는 [B] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("B"), false)

        // [A, B] 는 [C] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("C"), true)

        // [A, B] 는 [A, B] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("A", "B"), false)

        // [A, B] 는 [A, C] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("A", "C"), false)

        // [A, B] 는 [B, C] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("B", "C"), false)

        // [A, B] 는 [A, C] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("A", "C"), false)

        // [A, B] 는 [C, A] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("C", "A"), false)

        // [A, B] 는 [C, D] 중 하나가 아닌
        verify(NOT_MATCH, listOf("A", "B"), listOf("C", "D"), true)
    }

    private fun verify(type: Match.Type, userValue: Any, matchValues: List<String>, expected: Boolean) {
        val match = Match(type, Match.Operator.IN, ValueType.STRING, matchValues)
        val actual = sut.matches(userValue, match)
        assertEquals(expected, actual)
    }
}
