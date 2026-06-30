package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.HackleContext
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentContainerResolver
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentOverrideResolver
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentTargetRuleDeterminer
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentManualOverrideStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.evaluation.target.*
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.get
import io.hackle.sdk.core.getOrNull
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
        val sut = HackleContext()

        assertThrows<IllegalArgumentException> { sut.get<EvaluationContextTest>() }
        expectThat(sut.getOrNull<EvaluationContextTest>()).isNull()

        val instance = EvaluationContextTest()
        sut.register(instance)

        expectThat(sut.get<EvaluationContextTest>()) isSameInstanceAs instance
    }

    @Test
    fun `initialize`() {

        val sut = HackleContext()

        val evaluator = mockk<Evaluator>()
        val manualOverrideStorage = mockk<ExperimentManualOverrideStorage>()
        sut.initialize(evaluator, manualOverrideStorage, clock = Clock.SYSTEM)


        expectThat(sut.get<Evaluator>()) isSameInstanceAs evaluator
        expectThat(sut.get<ExperimentManualOverrideStorage>()) isSameInstanceAs manualOverrideStorage

        expectThat(sut.getOrNull<Bucketer>()).isNotNull()
        expectThat(sut.getOrNull<ConditionMatcherFactory>()).isNotNull()
        expectThat(sut.getOrNull<TargetMatcher>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentActionResolver>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentOverrideResolver>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentContainerResolver>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentTargetDeterminer>()).isNotNull()
        expectThat(sut.getOrNull<ExperimentTargetRuleDeterminer>()).isNotNull()
        expectThat(sut.getOrNull<RemoteConfigParameterTargetRuleDeterminer.Matcher>()).isNotNull()
        expectThat(sut.getOrNull<RemoteConfigParameterTargetRuleDeterminer>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageUserOverrideMatcher>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageTargetMatcher>()).isNotNull()
        expectThat(sut.getOrNull<InAppMessageHiddenMatcher>()).isNotNull()
    }
}
