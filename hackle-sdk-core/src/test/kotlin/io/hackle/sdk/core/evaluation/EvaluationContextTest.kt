package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.target.*
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class EvaluationContextTest {

    @Test
    fun `register and get`() {
        val sut = EvaluationContext()

        assertThrows<IllegalArgumentException> { sut.get<EvaluationContextTest>() }
        expectThat(sut.getOrNull<EvaluationContextTest>()).isNull()

        val instance = EvaluationContextTest()
        sut.register(instance)

        expectThat(sut.get<EvaluationContextTest>()) isSameInstanceAs instance
    }

    @Test
    fun `initialize`() {

        val sut = EvaluationContext()

        val evaluator = mockk<Evaluator>()
        val manualOverrideStorage = mockk<ManualOverrideStorage>()
        sut.initialize(evaluator, manualOverrideStorage, clock = Clock.SYSTEM)


        expectThat(sut.get<Evaluator>()) isSameInstanceAs evaluator
        expectThat(sut.get<ManualOverrideStorage>()) isSameInstanceAs manualOverrideStorage

        expectThat(sut.getOrNull<Bucketer>()).isNotNull()
        expectThat(sut.getOrNull<ConditionMatcherFactory>()).isNotNull()
        expectThat(sut.getOrNull<TargetMatcher>()).isNotNull()
        expectThat(sut.getOrNull<ActionResolver>()).isNotNull()
        expectThat(sut.getOrNull<OverrideResolver>()).isNotNull()
        expectThat(sut.getOrNull<ContainerResolver>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentTargetDeterminer>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentTargetRuleDeterminer>()).isNotNull()
        expectThat(sut.getOrNull<RemoteConfigParameterTargetRuleDeterminer.Matcher>()).isNotNull()
        expectThat(sut.getOrNull<RemoteConfigParameterTargetRuleDeterminer>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageResolver>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageUserOverrideMatcher>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageTargetMatcher>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageHiddenMatcher>()).isNotNull()
    }
}