package io.hackle.sdk.core.evaluation.service.experiment.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResult
import io.hackle.sdk.core.evaluation.service.experiment.match.*
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Targets
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.container
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class ExperimentLocalFlowEvaluatorTest {

    private lateinit var nextFlow: ExperimentLocalEvaluationFlow
    private lateinit var result: ExperimentEvaluateResult
    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        result = Experiments.result()
        nextFlow = mockk {
            every { evaluate(any(), any()) } returns result
        }
        context = Evaluators.context()
    }

    @Nested
    inner class OverrideExperimentLocalFlowEvaluatorTest {

        @MockK
        private lateinit var overrideResolver: ExperimentOverrideResolver

        @InjectMockKs
        private lateinit var sut: OverrideExperimentLocalFlowEvaluator


        @Test
        fun `AbTest 인 경우 override된 사용자인 경우 overriddenVariation, OVERRIDDEN 으로 평가한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST)
            val overriddenVariation = experiment.variations.first()
            every { overrideResolver.resolveOrNull(any(), any()) } returns overriddenVariation

            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.OVERRIDDEN
                get { variation } isSameInstanceAs overriddenVariation
            }
        }

        @Test
        fun `FeatureFlag 인 경우override된 사용자인 경우 overriddenVariation, INDIVIDUAL_TARGET_MATCH 으로 평가한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG)
            val overriddenVariation = experiment.variations.first()
            every { overrideResolver.resolveOrNull(any(), any()) } returns overriddenVariation

            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.INDIVIDUAL_TARGET_MATCH
                get { variation } isSameInstanceAs overriddenVariation
            }
        }

        @Test
        fun `override된 사용자가 아닌경우 다음 Flow로 평가한다`() {
            // given
            every { overrideResolver.resolveOrNull(any(), any()) } returns null

            // when
            val actual = sut.evaluate(Experiments.localRequest(), context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class DraftExperimentLocalFlowEvaluatorTest {

        private val sut = DraftExperimentLocalFlowEvaluator()

        @Test
        fun `DRAFT상태면 Control Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.DRAFT,
                variations = listOf(
                    Experiments.variation(id = 42, key = "A"),
                    Experiments.variation(id = 43, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_DRAFT
                get { variation.id } isEqualTo 42L
            }
        }

        @Test
        fun `DRAFT상태가 아니면 다음Flow로 평가한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class PausedExperimentLocalFlowEvaluatorTest {

        private val sut = PausedExperimentLocalFlowEvaluator()

        @Test
        fun `AB 테스트가 PAUSED 상태면 Control Variation, EXPERIMENT_PAUSED 으로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.PAUSED,
                variations = listOf(
                    Experiments.variation(id = 41, key = "A"),
                    Experiments.variation(id = 42, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_PAUSED
                get { variation.id } isEqualTo 41L
            }
        }

        @Test
        fun `기능 플래그가 PAUSED 상태면 Control Variation, FEATURE_FLAG_INACTIVE 로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.FEATURE_FLAG,
                status = Experiment.Status.PAUSED,
                variations = listOf(
                    Experiments.variation(id = 42, key = "A"),
                    Experiments.variation(id = 43, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.FEATURE_FLAG_INACTIVE
                get { variation.id } isEqualTo 42L
            }
        }

        @Test
        fun `PAUSED 상태가 아니면 다음 플로우를 실행한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.FEATURE_FLAG,
                status = Experiment.Status.COMPLETED,
                winnerVariationKey = "A"
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class CompletedExperimentLocalFlowEvaluatorTest {

        private val sut = CompletedExperimentLocalFlowEvaluator()

        @Test
        fun `COMPLETED 상태면 Winner Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.COMPLETED,
                variations = listOf(
                    Experiments.variation(id = 320, key = "A"),
                    Experiments.variation(id = 321, key = "B"),
                    Experiments.variation(id = 322, key = "C"),
                ),
                winnerVariationKey = "C"
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_COMPLETED
                get { variation.id } isEqualTo 322L
            }
        }

        @Test
        fun `COMPLETED 상태이지만 winner variation 이 없으면 예외 발생`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.COMPLETED,
                variations = listOf(
                    Experiments.variation(id = 320, key = "A"),
                    Experiments.variation(id = 321, key = "B"),
                    Experiments.variation(id = 322, key = "C"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("winner variation")
        }

        @Test
        fun `COMPLETED 상태가 아니면 다음 플로우를 실행한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.DRAFT)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class TargetExperimentLocalFlowEvaluatorTest {

        @MockK
        private lateinit var experimentTargetDeterminer: ExperimentTargetDeterminer

        @InjectMockKs
        private lateinit var sut: TargetExperimentLocalFlowEvaluator

        @Test
        fun `AB_TEST 타입이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment type must be AB_TEST")
        }

        @Test
        fun `사용자가 실험 참여 대상이면 다음 플로우를 실행한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            every { experimentTargetDeterminer.isUserInExperimentTarget(any(), any()) } returns true

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `사용자가 실험 참여 대상이 아니면 Control Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)
            every { experimentTargetDeterminer.isUserInExperimentTarget(any(), any()) } returns false

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_EXPERIMENT_TARGET
                get { variation.key } isEqualTo "A"
            }
        }
    }

    @Nested
    inner class TrafficAllocateExperimentLocalFlowEvaluatorTest {

        @MockK
        private lateinit var actionResolver: ExperimentActionResolver

        @InjectMockKs
        private lateinit var sut: TrafficAllocateExperimentLocalFlowEvaluator

        @Test
        fun `실행중이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.DRAFT)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment status must be RUNNING")
        }

        @Test
        fun `AB_TEST 타입이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment type must be AB_TEST")
        }

        @Test
        fun `기본룰에 해당하는 Variation이 없으면 Control Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns null

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).and {
                get { reason } isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
                get { variation.key } isEqualTo "A"
            }
        }

        @Test
        fun `할당된 Variation이 드랍되었으면 Control Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.RUNNING,
                variations = listOf(
                    Experiments.variation(id = 41, key = "A"),
                    Experiments.variation(id = 42, key = "B"),
                    Experiments.variation(id = 43, key = "C", isDropped = true),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.getVariationOrNull("C")

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).and {
                get { reason } isEqualTo DecisionReason.VARIATION_DROPPED
                get { variation.key } isEqualTo "A"
            }
        }

        @Test
        fun `할당된 Variation으로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.RUNNING,
                variations = listOf(
                    Experiments.variation(id = 41, key = "A"),
                    Experiments.variation(id = 42, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.getVariationOrNull("B")

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).and {
                get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
                get { variation.id } isEqualTo 42L
            }
        }
    }

    @Nested
    inner class TargetRuleExperimentLocalFlowEvaluatorTest {

        @MockK
        private lateinit var targetRuleDeterminer: ExperimentTargetRuleDeterminer

        @MockK
        private lateinit var actionResolver: ExperimentActionResolver

        @InjectMockKs
        private lateinit var sut: TargetRuleExperimentLocalFlowEvaluator

        @Test
        fun `실행중이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.DRAFT)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment status must be RUNNING")
        }

        @Test
        fun `FEATURE_FLAG 타입이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment type must be FEATURE_FLAG")
        }

        @Test
        fun `identifierType에 해당하는 식별자가 없으면 다음 플로우를 실행한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.FEATURE_FLAG,
                status = Experiment.Status.RUNNING,
                identifierType = "customId"
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `타겟룰에 해당하지 않으면 다음 플로우를 실행한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns null

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `타겟룰에 매치했지만 Action에 해당하는 Variation이 결정되지 않으면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)
            val targetRule = TargetRule(Targets.create(), Action.Bucket(42))

            every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns targetRule
            every { actionResolver.resolveOrNull(any(), any()) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("FeatureFlag must decide the Variation")
        }

        @Test
        fun `일치하는 타겟룰이 있는경우 해당 룰에 해당하는 Variation으로 결정한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)
            val targetRule = TargetRule(Targets.create(), Action.Bucket(42))

            every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns targetRule
            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.variations.first()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { variation.id } isEqualTo experiment.variations.first().id
            }
        }
    }

    @Nested
    inner class DefaultRuleExperimentLocalFlowEvaluatorTest {

        @MockK
        private lateinit var actionResolver: ExperimentActionResolver

        @InjectMockKs
        private lateinit var sut: DefaultRuleExperimentLocalFlowEvaluator

        @Test
        fun `실행중이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.DRAFT)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment status must be RUNNING")
        }

        @Test
        fun `FEATURE_FLAG 타입이 아니면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("experiment type must be FEATURE_FLAG")
        }


        @Test
        fun `기본룰에 해당하는 Variation을 결정하지 못하면 예외 발생`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)
            every { actionResolver.resolveOrNull(any(), any()) } returns null

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("FeatureFlag must decide the Variation")
        }

        @Test
        fun `identifierType에 해당하는 식별자가 없으면 Control Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.FEATURE_FLAG,
                status = Experiment.Status.RUNNING,
                identifierType = "customId",
                variations = listOf(
                    Experiments.variation(id = 41, key = "A"),
                    Experiments.variation(id = 42, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.DEFAULT_RULE
                get { variation.id } isEqualTo 41L
            }
        }

        @Test
        fun `기본룰에 해당하는 Variation으로 평가한다`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.variations.first()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.DEFAULT_RULE
                get { variation.id } isEqualTo experiment.variations.first().id
            }
        }
    }

    @Nested
    inner class ContainerExperimentLocalFlowEvaluatorTest {

        @MockK
        private lateinit var containerResolver: ExperimentContainerResolver

        @InjectMockKs
        private lateinit var sut: ContainerExperimentLocalFlowEvaluator

        @Test
        fun `실험이 상호배타에 속하지 않은 실험은 Next Flow로 진행한다`() {
            // given
            val experiment = Experiments.config()
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `실험이 상호배타에 속해있지만 container 정보를 찾을 수 없을때 Exception 발생`() {
            // given
            val experiment = Experiments.config(containerId = 42L)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(actual.message) isEqualTo "Container[42]"
        }

        @Test
        fun `실험이 상호배타에 속해있고 상호배타 그룹에 해당하면 Next Flow 진행 `() {
            // given
            val experiment = Experiments.config(containerId = 1L)
            val request = Experiments.localRequest(
                workspace = Workspaces.config(containers = listOf(container(id = 1, bucketId = 1))),
                experiment = experiment
            )

            every { containerResolver.isUserInContainerGroup(any(), any()) } returns true

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `실험이 상호배타에 속해있지만 상호배타 그룹에 해당하지 않으면 Control Variation 으로 평가한다`() {
            // given
            val experiment = Experiments.config(containerId = 1L)
            val request = Experiments.localRequest(
                workspace = Workspaces.config(containers = listOf(container(id = 1, bucketId = 1))),
                experiment = experiment
            )

            every { containerResolver.isUserInContainerGroup(any(), any()) } returns false

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT
                get { variation.key } isEqualTo "A"
            }
        }
    }

    @Nested
    inner class IdentifierExperimentLocalFlowEvaluatorTest {

        private val sut = IdentifierExperimentLocalFlowEvaluator()

        @Test
        fun `identifierType 에 대한 식별자가 있으면 다음 플로우 실행`() {
            // given
            val experiment = Experiments.config(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `identifierType 에 대한 식별자가 없으면 IDENTIFIER_NOT_FOUND`() {
            // given
            val experiment = Experiments.config(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.RUNNING,
                identifierType = "hello",
                variations = listOf(
                    Experiments.variation(id = 42, key = "A"),
                    Experiments.variation(id = 43, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
                get { variation.id } isEqualTo 42L
            }
        }
    }

}
