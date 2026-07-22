package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Targets
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class ExperimentOverrideResolverTest {

    @MockK
    private lateinit var manualOverrideStorage: ExperimentManualOverrideStorage

    @MockK
    private lateinit var targetMatcher: TargetMatcher

    @MockK
    private lateinit var actionResolver: ExperimentActionResolver

    @InjectMockKs
    private lateinit var sut: ExperimentOverrideResolver

    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        every { manualOverrideStorage[any(), any()] } returns null
        context = Evaluators.context()
    }

    @Test
    fun `manual override 를 가장 먼저 확인한다`() {
        // given
        val variation = Experiments.variation()
        every { manualOverrideStorage[any(), any()] } returns variation
        val request = Experiments.localRequest()

        // when
        val actual = sut.resolveOrNull(request, context)

        // then
        expectThat(actual) isSameInstanceAs variation
        verify { targetMatcher wasNot Called }
    }

    @Test
    fun `Experiment identifierType에 해당하는 식별자가 없는 경우 Segment Override 를 평가한다`() {
        // given
        val variations = Experiments.variations("A", "B")
        val experiment = Experiments.config(
            identifierType = "customId",
            variations = variations,
            userOverrides = mapOf("user_02" to variations[1].id),
            segmentOverrides = listOf(TargetRule(Targets.create(), Action.Variation(variations[0].id)))
        )
        val user = HackleUser.builder().identifier(IdentifierType.ID, "user_01").build()
        val request = Experiments.localRequest(experiment = experiment, user = user)

        every { targetMatcher.matches(any(), any(), any()) } returns true

        val overriddenVariation = Experiments.variation()
        every { actionResolver.resolveOrNull(any(), any()) } returns overriddenVariation

        // when
        val actual = sut.resolveOrNull(request, context)

        // then
        expectThat(actual) isSameInstanceAs overriddenVariation
    }

    @Test
    fun `직접 입력으로 override 되어있지 않으면 SegmentOverride 를 확인한다`() {
        // given
        val variations = Experiments.variations("A", "B")
        val experiment = Experiments.config(
            variations = variations,
            userOverrides = mapOf("user_02" to variations[1].id),
            segmentOverrides = listOf(TargetRule(Targets.create(), Action.Variation(variations[0].id)))
        )
        val user = HackleUser.builder().identifier(IdentifierType.ID, "user_01").build()
        val request = Experiments.localRequest(experiment = experiment, user = user)

        every { targetMatcher.matches(any(), any(), any()) } returns true

        val overriddenVariation = Experiments.variation()
        every { actionResolver.resolveOrNull(any(), any()) } returns overriddenVariation

        // when
        val actual = sut.resolveOrNull(request, context)

        // then
        expectThat(actual) isSameInstanceAs overriddenVariation
    }

    @Test
    fun `직접 입력한 override 를 먼저 평가한다`() {
        // given
        val variations = Experiments.variations("A", "B")
        val experiment = Experiments.config(
            variations = variations,
            userOverrides = mapOf("user_01" to variations[1].id),
            segmentOverrides = listOf(TargetRule(Targets.create(), Action.Variation(variations[0].id)))
        )
        val user = HackleUser.builder().identifier(IdentifierType.ID, "user_01").build()
        val request = Experiments.localRequest(experiment = experiment, user = user)

        // when
        val actual = sut.resolveOrNull(request, context)

        // then
        expectThat(actual)
            .isNotNull()
            .get { key } isEqualTo "B"
        verify { targetMatcher wasNot Called }
        verify { actionResolver wasNot Called }
    }

    @Test
    fun `직접입력, Segment 둘다 override 되어 있지않으면 null 리턴`() {
        // given
        val variations = Experiments.variations("A", "B")
        val experiment = Experiments.config(
            variations = variations,
            userOverrides = mapOf("user_02" to variations[1].id),
            segmentOverrides = listOf(TargetRule(Targets.create(), Action.Variation(variations[0].id)))
        )
        val user = HackleUser.builder().identifier(IdentifierType.ID, "user_01").build()
        val request = Experiments.localRequest(experiment = experiment, user = user)

        every { targetMatcher.matches(any(), any(), any()) } returns false

        // when
        val actual = sut.resolveOrNull(request, context)

        // then
        expectThat(actual).isNull()
    }
}
