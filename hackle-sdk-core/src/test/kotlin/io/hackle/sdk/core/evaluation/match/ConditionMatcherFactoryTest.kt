package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA

internal class ConditionMatcherFactoryTest {

    @Test
    fun `getMatcher`() {
        val sut = ConditionMatcherFactory(mockk())
        expectThat(sut.getMatcher(Target.Key.Type.USER_ID)).isA<UserConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.USER_PROPERTY)).isA<UserConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.HACKLE_PROPERTY)).isA<UserConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.SEGMENT)).isA<SegmentConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.AB_TEST)).isA<ExperimentConditionMatcher>()
        expectThat(sut.getMatcher(Target.Key.Type.FEATURE_FLAG)).isA<ExperimentConditionMatcher>()
    }
}
