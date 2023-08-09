package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.target.ExperimentTargetRuleDeterminer
import io.hackle.sdk.core.evaluation.target.OverrideResolver
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.workspace
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
class ExperimentFlowEvaluatorTest {

    private lateinit var nextFlow: ExperimentFlow
    private lateinit var evaluation: ExperimentEvaluation
    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        evaluation = mockk()
        nextFlow = mockk {
            every { evaluate(any(), any()) } returns evaluation
        }
        context = Evaluators.context()
    }


    @Nested
    inner class OverrideEvaluatorTest {

        @MockK
        private lateinit var overrideResolver: OverrideResolver

        @InjectMockKs
        private lateinit var sut: OverrideEvaluator


        @Test
        fun `AbTest 인 경우 override된 사용자인 경우 overriddenVariation, OVERRIDDEN 으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST)
            val variation = experiment.variations.first()
            every { overrideResolver.resolveOrNull(any(), any()) } returns variation

            val request = experimentRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.OVERRIDDEN
                get { variationId } isEqualTo variation.id
            }
        }

        @Test
        fun `FeatureFlag 인 경우override된 사용자인 경우 overriddenVariation, INDIVIDUAL_TARGET_MATCH 으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG)
            val variation = experiment.variations.first()
            every { overrideResolver.resolveOrNull(any(), any()) } returns variation

            val request = experimentRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.INDIVIDUAL_TARGET_MATCH
                get { variationId } isEqualTo variation.id
            }
        }

        @Test
        fun `override된 사용자가 아닌경우 다음 Flow로 평가한다`() {
            // given
            every { overrideResolver.resolveOrNull(any(), any()) } returns null


            // when
            val actual = sut.evaluate(experimentRequest(), Evaluators.context(), nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class DraftExperimentEvaluatorTest {

        @Test
        fun `DRAFT상태면 기본그룹으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.DRAFT) {
                variations {
                    Variation.A(42)
                    Variation.B(43)
                }
            }
            val request = experimentRequest(experiment = experiment)
            val sut = DraftExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_DRAFT
                get { variationId } isEqualTo 42
            }
        }

        @Test
        fun `DRAFT상태가 아니면 다음Flow로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)
            val sut = DraftExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class PausedExperimentEvaluatorTest {

        @Test
        fun `AB 테스트가 PAUSED 상태면 기본그룹, EXPERIMENT_PAUSED으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.PAUSED) {
                variations {
                    Variation.A(41)
                    Variation.B(42)
                }
            }
            val request = experimentRequest(experiment = experiment)
            val sut = PausedExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_PAUSED
                get { variationId } isEqualTo 41
            }
        }

        @Test
        fun `기능 플래그가 PAUSED 상태면 기본그룹, FEATURE_FLAG_INACTIVE 로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.PAUSED) {
                variations {
                    Variation.A(42)
                    Variation.B(43)
                }
            }
            val request = experimentRequest(experiment = experiment)
            val sut = PausedExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.FEATURE_FLAG_INACTIVE
                get { variationId } isEqualTo 42
            }
        }

        @Test
        fun `PAUSED 상태가 아니면 다음 플로우를 실행한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.COMPLETED)
            val request = experimentRequest(experiment = experiment)
            val sut = PausedExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class CompletedExperimentEvaluatorTest {
        @Test
        fun `COMPLETED 상태면 위너 그룹 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.COMPLETED) {
                variations {
                    Variation.A(320)
                    Variation.B(321)
                    Variation.C(322)
                }
                winner(Variation.C)
            }

            val request = experimentRequest(experiment = experiment)
            val sut = CompletedExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_COMPLETED
                get { variationId } isEqualTo 322
            }
        }

        @Test
        fun `COMPLETED 상태이지만 winner variation 이 없으면 예외 발생`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.COMPLETED) {
                variations {
                    Variation.A(320)
                    Variation.B(321)
                    Variation.C(322)
                }
            }

            val request = experimentRequest(experiment = experiment)
            val sut = CompletedExperimentEvaluator()

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
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.DRAFT) {
                variations(Variation.A, Variation.B)
            }
            val request = experimentRequest(experiment = experiment)
            val sut = CompletedExperimentEvaluator()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class ExperimentTargetEvaluatorTest {

        @MockK
        private lateinit var experimentTargetDeterminer: ExperimentTargetDeterminer

        @InjectMockKs
        private lateinit var sut: ExperimentTargetEvaluator

        @Test
        fun `AB_TEST 타입이 아니면 예외 발생`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

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
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

            every { experimentTargetDeterminer.isUserInExperimentTarget(any(), any()) } returns true

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `사용자가 실험 참여 대상이 아니면 기본그룹으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)
            every { experimentTargetDeterminer.isUserInExperimentTarget(any(), any()) } returns false

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_EXPERIMENT_TARGET
                get { variationKey } isEqualTo "A"
            }
        }
    }

    @Nested
    inner class TrafficAllocateEvaluatorTest {

        @MockK
        private lateinit var actionResolver: ActionResolver

        @InjectMockKs
        private lateinit var sut: TrafficAllocateEvaluator

        @Test
        fun `실행중이 아니면 예외 발생`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.DRAFT)
            val request = experimentRequest(experiment = experiment)

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
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

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
        fun `기본룰에 해당하는 Variation이 없으면 기본그룹으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns null

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
                get { variationKey } isEqualTo "A"
            }
        }

        @Test
        fun `할당된 Variation이 드랍되었으면 기본그룹으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING) {
                variations {
                    Variation.A(41, false)
                    Variation.B(42, false)
                    Variation.C(43, true)
                }
            }
            val request = experimentRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.getVariationOrNull("C")

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.VARIATION_DROPPED
                get { variationKey } isEqualTo "A"
            }
        }

        @Test
        fun `할당된 Variation으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING) {
                variations {
                    Variation.A(41, false)
                    Variation.B(42, false)
                }
            }
            val request = experimentRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.getVariationOrNull("B")

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
                get { variationId } isEqualTo 42
            }
        }
    }

    @Nested
    inner class TargetRuleEvaluatorTest {

        @MockK
        private lateinit var targetRuleDeterminer: ExperimentTargetRuleDeterminer

        @MockK
        private lateinit var actionResolver: ActionResolver

        @InjectMockKs
        private lateinit var sut: TargetRuleEvaluator

        @Test
        fun `실행중이 아니면 예외 발생`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.DRAFT)
            val request = experimentRequest(experiment = experiment)

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
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

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
            val experiment = experiment(
                type = Experiment.Type.FEATURE_FLAG,
                status = Experiment.Status.RUNNING,
                identifierType = "customId"
            )
            val request = experimentRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }

        @Test
        fun `타겟룰에 해당하지 않으면 다음 플로우를 실행한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

            every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns null

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }

        @Test
        fun `타겟룰에 매치했지만 Action에 해당하는 Variation이 결정되지 않으면 예외 발생`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)
            val action = mockk<Action>()
            val targetRule = TargetRule(mockk(), action)

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
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

            val targetRule = mockk<TargetRule> {
                every { action } returns mockk()
            }

            every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns targetRule

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.variations.first()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { variationId } isEqualTo experiment.variations.first().id
            }
        }
    }

    @Nested
    inner class DefaultRuleEvaluatorTest {

        @MockK
        private lateinit var actionResolver: ActionResolver

        @InjectMockKs
        private lateinit var sut: DefaultRuleEvaluator

        @Test
        fun `실행중이 아니면 예외 발생`() {
            // given
            val experiment = mockk<Experiment>(relaxed = true)
            val request = experimentRequest(experiment = experiment)

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
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

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
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)
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
        fun `identifierType에 해당하는 식별자가 없으면 defaultVariation 을 리턴한다`() {
            // given
            val experiment = experiment(
                type = Experiment.Type.FEATURE_FLAG,
                status = Experiment.Status.RUNNING,
                identifierType = "customId"
            ) {
                variations {
                    Variation.A(41, false)
                    Variation.B(42, false)
                }
            }
            val request = experimentRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual.reason) isEqualTo DecisionReason.DEFAULT_RULE
            expectThat(actual.variationId) isEqualTo 41
        }

        @Test
        fun `기본룰에 해당하는 Variation으로 평가한다`() {
            // given
            val experiment = experiment(type = Experiment.Type.FEATURE_FLAG, status = Experiment.Status.RUNNING)
            val request = experimentRequest(experiment = experiment)

            every { actionResolver.resolveOrNull(any(), any()) } returns experiment.variations.first()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual.reason) isEqualTo DecisionReason.DEFAULT_RULE
            expectThat(actual.variationId) isEqualTo experiment.variations.first().id
        }
    }

    @Nested
    inner class ContainerEvaluatorTest {

        @MockK
        private lateinit var containerResolver: ContainerResolver

        @InjectMockKs
        private lateinit var sut: ContainerEvaluator

        private val user = HackleUser.of("test_id")
        private val defaultVariationKey = VariationKey.A.name

        @Test
        fun `실험이 상호배타에 속하지 않은 실험은 Next Flow로 진행한다`() {
            val workspace = workspace {}
            val experiment = experiment()
            val request = experimentRequest(workspace, user, experiment)

            val actual = sut.evaluate(request, context, nextFlow)

            expectThat(actual) isSameInstanceAs evaluation
            verify {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `실험이 상호배타에 속해있지만 container 정보를 찾을 수 없을때 Exception 발생`() {
            val workspace = workspace {}
            val experiment = experiment(containerId = 42L)
            val request = experimentRequest(workspace, user, experiment)

            val actual = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            expectThat(actual.message) isEqualTo "Container[42]"
        }

        @Test
        fun `실험이 상호배타에 속해있고 상호배타 그룹에 해당하면 Next Flow 진행 `() {
            val container = mockk<Container> {
                every { id } returns 1
                every { bucketId } returns 1
            }
            val workspace = mockk<Workspace> {
                every { getContainerOrNull(1) } returns container
            }
            val experiment = experiment(containerId = 1L)
            val request = experimentRequest(workspace, user, experiment)

            every { containerResolver.isUserInContainerGroup(any(), any()) } returns true

            val actual = sut.evaluate(request, context, nextFlow)

            expectThat(actual) isSameInstanceAs evaluation
            verify {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `실험이 상호배타에 속해있지만 상호배타 그룹에 해당하지 않으면 defaultVariation 결과를 리턴`() {
            val container = mockk<Container> {
                every { id } returns 1
                every { bucketId } returns 1
            }
            val workspace = mockk<Workspace> {
                every { getContainerOrNull(1) } returns container
            }
            val experiment = experiment(containerId = 1L)
            val request = experimentRequest(workspace, user, experiment)

            every { containerResolver.isUserInContainerGroup(any(), any()) } returns false

            val actual = sut.evaluate(request, context, nextFlow)

            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT
                get { variationKey } isEqualTo "A"
            }
        }
    }

    @Nested
    inner class IdentifierEvaluatorTest {

        private val sut = IdentifierEvaluator()

        @Test
        fun `identifierType 에 대한 식별자가 있으면 다음 플로우 실행`() {
            // given
            val experiment = experiment(type = Experiment.Type.AB_TEST, status = Experiment.Status.RUNNING) {
                variations {
                    Variation.A(42)
                    Variation.B(43)
                }
            }
            val request = experimentRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `identifierType 에 대한 식별자가 없으면 IDENTIFIER_NOT_FOUND`() {
            // given
            val experiment = experiment(
                type = Experiment.Type.AB_TEST,
                status = Experiment.Status.RUNNING,
                identifierType = "hello"
            ) {
                variations {
                    Variation.A(42)
                    Variation.B(43)
                }
            }
            val request = experimentRequest(experiment = experiment)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
                get { variationKey } isEqualTo "A"
            }
        }
    }

}