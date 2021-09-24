package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target.Match.Operator.*
import io.hackle.sdk.core.model.Target.Match.ValueType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValueOperatorMatcherFactoryTest {

    @Test
    fun `ValueMatcherFactory`() {
        assertEquals(StringMatcher, ValueOperatorMatcherFactory().getValueMatcher(STRING))
        assertEquals(NumberMatcher, ValueOperatorMatcherFactory().getValueMatcher(NUMBER))
        assertEquals(BooleanMatcher, ValueOperatorMatcherFactory().getValueMatcher(BOOLEAN))
        assertEquals(VersionMatcher, ValueOperatorMatcherFactory().getValueMatcher(VERSION))
    }

    @Test
    fun `OperatorMatcherFactory`() {
        assertEquals(InMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(IN))
        assertEquals(ContainsMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(CONTAINS))
        assertEquals(StartsWithMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(STARTS_WITH))
        assertEquals(EndsWithMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(ENDS_WITH))
        assertEquals(GreaterThanMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(GT))
        assertEquals(GreaterThanOrEqualToMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(GTE))
        assertEquals(LessThanMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(LT))
        assertEquals(LessThanOrEqualToMatcher, ValueOperatorMatcherFactory().getOperatorMatcher(LTE))
    }
}