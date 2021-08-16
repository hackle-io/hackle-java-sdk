package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA

internal class ConditionMatcherFactoryTest {

    @Test
    fun `getMatcher`() {
        val sut = ConditionMatcherFactory()
        expectThat(sut.getMatcher(Target.Key.Type.USER_PROPERTY)).isA<PropertyConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.HACKLE_PROPERTY)).isA<PropertyConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.SEGMENT)).isA<SegmentConditionMatcher>()
    }
}

