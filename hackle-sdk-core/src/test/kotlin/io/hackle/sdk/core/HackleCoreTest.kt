package io.hackle.sdk.core

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.Variation.J
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility.InAppMessageEligibilityEvaluator
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.event.UserEventFactory
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceDsl
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.hackle.sdk.core.workspace.workspace
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class HackleCoreTest {

    @MockK
    private lateinit var experimentEvaluator: ExperimentEvaluator

    @MockK
    private lateinit var remoteConfigEvaluator: RemoteConfigEvaluator<*>

    @MockK
    private lateinit var inAppMessageEligibilityEvaluator: InAppMessageEligibilityEvaluator

    @MockK
    private lateinit var workspaceFetcher: WorkspaceFetcher

    @MockK
    private lateinit var eventFactory: UserEventFactory

    @RelaxedMockK
    private lateinit var eventProcessor: EventProcessor

    @MockK
    private lateinit var clock: Clock

    @InjectMockKs
    private lateinit var sut: HackleCore

    @BeforeEach
    fun beforeEach() {
        every { eventFactory.create(any(), any()) } returns listOf(mockk())
        every { clock.currentMillis() } returns 42L
    }

    @Nested
    inner class ExperimentTest {

        @Test
        fun `Workspace를 가져오지 못하면 defaultVariation으로 결정하고 노출 이벤트는 전송하지 않는다`() {
            // given
            every { workspaceFetcher.fetch() } returns null

            val defaultVariation = J

            // when
            val actual = sut.experiment(42, HackleUser.of("TEST_USER_ID"), defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo SDK_NOT_READY
                get { variation } isEqualTo defaultVariation
                get { experiment }.isNull()
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `experimentKey에 해당하는 experiment가 없으면 defaultVariation으로 결정하고 노출 이벤트는 전송하지 않는다`() {
            // given
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns null
            }

            every { workspaceFetcher.fetch() } returns workspace

            val defaultVariation = J

            // when
            val actual = sut.experiment(42, HackleUser.of("TEST_USER_ID"), defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo EXPERIMENT_NOT_FOUND
                get { variation } isEqualTo defaultVariation
                get { experiment }.isNull()
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `평가 결과로 노출 이벤트를 전송하고 평가된 결과로 결정한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val experiment = experiment()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace

            val defaultVariation = J
            val config = mockk<ParameterConfiguration> { every { id } returns 420 }
            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                experiment,
                320,
                "B",
                config
            )

            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation
            every { eventFactory.create(any(), evaluation) } returns listOf(mockk(), mockk())

            // when
            val actual = sut.experiment(42, user, defaultVariation)

            // then
            expectThat(actual) isEqualTo Decision.of(B, TRAFFIC_ALLOCATED, config, experiment)
            verify(exactly = 2) {
                eventProcessor.process(any())
            }
        }

        @Test
        fun `평가결과 Config 가 없으면 empty config 를 리턴한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val experiment = experiment()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace

            val defaultVariation = J

            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                experiment,
                320,
                "B",
                null
            )
            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation

            // when
            val actual = sut.experiment(42, user, defaultVariation)

            // then
            expectThat(actual) isEqualTo Decision.of(B, TRAFFIC_ALLOCATED, ParameterConfig.empty(), experiment)
        }
    }

    @Nested
    inner class ExperimentsTest {
        @Test
        fun `Workspace 를 가져오지 못하면 비어있는 map 을 리턴한다`() {
            // given
            every { workspaceFetcher.fetch() } returns null
            val user = HackleUser.of("TEST_USER_ID")

            // when
            val actual = sut.experiments(user)

            // then
            expectThat(actual).hasSize(0)
        }

        @Test
        fun `모든 실험에 대한 분배 결과를 리턴한다`() {
            // given
            val parameterConfiguration42 = mockk<ParameterConfiguration> { every { id } returns 42 }
            val parameterConfiguration43 = mockk<ParameterConfiguration> { every { id } returns 43 }
            val workspace = mockk<Workspace> {
                every { experiments } returns listOf(
                    experiment(1, 1, 4, "A", EXPERIMENT_DRAFT, parameterConfiguration42),
                    experiment(2, 3, 7, "B", EXPERIMENT_COMPLETED, parameterConfiguration43),
                    experiment(3, 4, 10, "A", OVERRIDDEN, null),
                    experiment(4, 7, 21, "C", TRAFFIC_ALLOCATED, null),
                    experiment(5, 10, 27, "A", NOT_IN_EXPERIMENT_TARGET, null),
                )
            }

            every { workspaceFetcher.fetch() } returns workspace
            val user = HackleUser.of("TEST_USER_ID")

            // when
            val actual = sut.experiments(user)

            // then
            expectThat(actual).hasSize(5)
        }

        private fun experiment(
            id: Long,
            experimentKey: Long,
            variationId: Long,
            variationKey: String,
            reason: DecisionReason,
            config: ParameterConfiguration? = null,
        ): Experiment {

            val experiment = mockk<Experiment> {
                every { this@mockk.id } returns id
                every { key } returns experimentKey
            }

            val evaluation = ExperimentEvaluation(
                reason,
                emptyList(),
                experiment,
                variationId,
                variationKey,
                config
            )
            val request = experimentRequest(experiment = experiment)
            every { experimentEvaluator.evaluate(eq(request), any()) } returns evaluation
            return experiment
        }
    }

    @Nested
    inner class FeatureFlagTest {
        @Test
        fun `Workspace를 가져오지 못하면 off로 결정하고 노출 이벤트는 전송하지 않는다`() {
            // given
            every { workspaceFetcher.fetch() } returns null


            // when
            val actual = sut.featureFlag(42, HackleUser.of("TEST_USER_ID"))

            //then
            expectThat(actual) {
                get { reason } isEqualTo SDK_NOT_READY
                get { isOn }.isFalse()
                get { featureFlag }.isNull()
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `featureFlagKey에 해당하는 featureFlag가 없으면 off로 결정하고 노출 이벤트는 전송하지 않는다`() {
            // given
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns null
            }

            every { workspaceFetcher.fetch() } returns workspace

            // when
            val actual = sut.featureFlag(42, HackleUser.of("TEST_USER_ID"))

            //then
            expectThat(actual) {
                get { reason } isEqualTo FEATURE_FLAG_NOT_FOUND
                get { isOn }.isFalse()
                get { featureFlag }.isNull()
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `평가 결과 이벤트를 전송한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = experiment()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val config = mockk<ParameterConfiguration> { every { id } returns 420 }

            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                featureFlag,
                320,
                "A",
                config
            )
            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation
            every { eventFactory.create(any(), evaluation) } returns listOf(mockk(), mockk())

            // when
            sut.featureFlag(42, user)

            // then
            verify(exactly = 2) {
                eventProcessor.process(any())
            }
        }

        @Test
        fun `평가 결과 Control 그룹이면 off로 결정한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = experiment()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                featureFlag,
                320,
                "A",
                null
            )
            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) {
                get { isOn }.isFalse()
                get { reason } isEqualTo TRAFFIC_ALLOCATED
                get { this@get.featureFlag } isEqualTo featureFlag
            }
        }

        @Test
        fun `평가 결과가 Control 그룹이 아니면 on으로 결정한다 `() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = experiment()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                featureFlag,
                320,
                "B",
                null
            )
            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation


            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) {
                get { isOn }.isTrue()
                get { reason } isEqualTo TRAFFIC_ALLOCATED
                get { this@get.featureFlag } isEqualTo featureFlag
            }
        }

        @Test
        fun `평가된 config를 사용한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = experiment()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val config = mockk<ParameterConfiguration> { every { id } returns 420 }

            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                featureFlag,
                320,
                "B",
                config
            )
            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) isEqualTo FeatureFlagDecision.on(TRAFFIC_ALLOCATED, config, featureFlag)
        }

        @Test
        fun `평가된 config 가 없으면 empty config 를 사용한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = experiment()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val evaluation = ExperimentEvaluation(
                TRAFFIC_ALLOCATED,
                emptyList(),
                featureFlag,
                320,
                "B",
                null
            )
            every { experimentEvaluator.evaluate(any(), any()) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) isEqualTo FeatureFlagDecision.on(TRAFFIC_ALLOCATED, ParameterConfig.empty(), featureFlag)
        }
    }

    @Nested
    inner class FeatureFlagsTest {

        @Test
        fun `Workspace 를 가져오지 못하면 비어있는 map 을 리턴한다`() {
            // given
            every { workspaceFetcher.fetch() } returns null
            val user = HackleUser.of("TEST_USER_ID")

            // when
            val actual = sut.featureFlags(user)

            // then
            expectThat(actual).hasSize(0)
        }

        @Test
        fun `모든 기능플래그 대한 분배 결과를 리턴한다`() {
            // given
            val parameterConfiguration42 = mockk<ParameterConfiguration> { every { id } returns 42 }
            val parameterConfiguration43 = mockk<ParameterConfiguration> { every { id } returns 43 }
            val workspace = mockk<Workspace> {
                every { featureFlags } returns listOf(
                    experiment(1, 1, 4, "A", FEATURE_FLAG_INACTIVE, parameterConfiguration42),
                    experiment(2, 3, 7, "B", INDIVIDUAL_TARGET_MATCH, parameterConfiguration43),
                    experiment(3, 4, 10, "A", TARGET_RULE_MATCH, null),
                    experiment(4, 7, 21, "B", DEFAULT_RULE, null),
                    experiment(5, 10, 27, "A", DEFAULT_RULE, null),
                )
            }

            every { workspaceFetcher.fetch() } returns workspace
            val user = HackleUser.of("TEST_USER_ID")

            // when
            val actual = sut.featureFlags(user)

            // then
            expectThat(actual).hasSize(5)
        }

        private fun experiment(
            id: Long,
            experimentKey: Long,
            variationId: Long,
            variationKey: String,
            reason: DecisionReason,
            config: ParameterConfiguration? = null,
        ): Experiment {

            val experiment = mockk<Experiment> {
                every { this@mockk.id } returns id
                every { key } returns experimentKey
            }
            val evaluation = ExperimentEvaluation(
                reason,
                emptyList(),
                experiment,
                variationId,
                variationKey,
                config
            )
            val request = experimentRequest(experiment = experiment)
            every { experimentEvaluator.evaluate(eq(request), any()) } returns evaluation
            return experiment
        }
    }

    @Nested
    inner class Track {

        @Test
        fun `Workspace를 가져오지 못하면 Undefined 이벤트를 전송한다`() {
            // given
            every { workspaceFetcher.fetch() } returns null

            // when
            sut.track(Event.of("test_event_key"), HackleUser.of("TEST_USER_ID"), 42)

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Track>()
                        .and {
                            get { timestamp } isEqualTo 42
                            get { eventType }
                                .isA<EventType.Undefined>()
                                .get { key }
                                .isEqualTo("test_event_key")
                        }
                })
            }
        }

        @Test
        fun `eventKey에 대한 eventType을 찾지 못하면 Undefined 이벤트를 전송한다`() {
            // given
            val workspace = mockk<Workspace> {
                every { getEventTypeOrNull(any()) } returns null
            }
            every { workspaceFetcher.fetch() } returns workspace

            // when
            sut.track(Event.of("undefined_event_key"), HackleUser.of("TEST_USER_ID"), 42)

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Track>()
                        .and {
                            get { timestamp } isEqualTo 42
                            get { eventType }
                                .isA<EventType.Undefined>()
                                .get { key }
                                .isEqualTo("undefined_event_key")
                        }
                })
            }
        }

        @Test
        fun `eventKey에 대한 evenType이 정의 되어 있으면 해당 이벤트를 전송한다`() {
            // given
            val eventType = mockk<EventType>()
            val workspace = mockk<Workspace> {
                every { getEventTypeOrNull(any()) } returns eventType
            }
            every { workspaceFetcher.fetch() } returns workspace

            // when
            sut.track(Event.of("custom_event_key"), HackleUser.of("TEST_USER_ID"), 42)

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .get { eventType }
                        .isSameInstanceAs(eventType)
                })
            }
        }
    }

    @Nested
    inner class RemoteConfigTest {
        @Test
        fun `Workspace 를 가져오지 못하면 defaultValue 로 결정하고 이벤트는 전송하지 않는다`() {
            // given
            every { workspaceFetcher.fetch() } returns null


            // when
            val actual = sut.remoteConfig("42", HackleUser.of("test"), ValueType.STRING, "default value")

            //then
            expectThat(actual) {
                get { reason } isEqualTo SDK_NOT_READY
                get { value } isEqualTo "default value"
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `parameterKey 에 해당하는 RemoteConfigParameter 가 없으면 defaultValue 로 결정하고 이벤트는 전송하지 않는다`() {
            // given
            val workspace = mockk<Workspace> {
                every { getRemoteConfigParameterOrNull(any()) } returns null
            }

            every { workspaceFetcher.fetch() } returns workspace


            // when
            val actual = sut.remoteConfig("42", HackleUser.of("test"), ValueType.STRING, "default value")

            //then
            expectThat(actual) {
                get { reason } isEqualTo REMOTE_CONFIG_PARAMETER_NOT_FOUND
                get { value } isEqualTo "default value"
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `평가 결과로 이벤트를 전송하고 평가된 결과로 결정한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val parameter = mockk<RemoteConfigParameter> {
                every { id } returns 42
            }
            val workspace = mockk<Workspace> {
                every { getRemoteConfigParameterOrNull(any()) } returns parameter
            }
            every { workspaceFetcher.fetch() } returns workspace


            val evaluation: RemoteConfigEvaluation<Any> = RemoteConfigEvaluation(
                DEFAULT_RULE,
                emptyList(),
                parameter,
                42,
                "vvv",
                emptyMap()
            )
            every { remoteConfigEvaluator.evaluate(any(), any()) } returns evaluation
            every { eventFactory.create(any(), evaluation) } returns listOf(mockk(), mockk())

            // when
            val actual = sut.remoteConfig("42", user, ValueType.STRING, "default value")

            // then
            expectThat(actual) isEqualTo RemoteConfigDecision.of("vvv", DEFAULT_RULE)
            verify(exactly = 2) {
                eventProcessor.process(any())
            }
        }
    }


    @Nested
    inner class EvaluateTest {
        @Test
        fun `evaluate`() {
            // given
            val request = mockk<Evaluator.Request>()
            val context = Evaluators.context()
            val evaluator = mockk<ContextualEvaluator<Evaluator.Request, Evaluator.Evaluation>>()
            val evaluation = mockk<Evaluator.Evaluation>()
            every { evaluator.evaluate(request, context) } returns evaluation

            val events = listOf(mockk<UserEvent>(), mockk<UserEvent>())
            every { eventFactory.create(request, evaluation) } returns events

            // when
            val actual = sut.evaluate(request, context, evaluator)

            // then
            expectThat(actual) isSameInstanceAs evaluation
            verify(exactly = 2) {
                eventProcessor.process(any())
            }

        }
    }

    @Nested
    inner class FlushTest {
        @Test
        fun `flush EventProcessor`() {
            sut.flush()
            verify(exactly = 1) {
                eventProcessor.flush()
            }
        }
    }

    @Nested
    inner class CloseTest {

        @Test
        fun `workspaceFetcher eventProcessor를 종료한다`() {
            mockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")

            // when
            sut.close()

            //then
            verify(exactly = 1) { workspaceFetcher.tryClose() }
            verify(exactly = 1) { eventProcessor.tryClose() }

            unmockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")
        }
    }


    @Test
    fun `not found experiment`() {
        val workspaceFetcher = workspaceFetcher {}

        val core = HackleCore.create(EvaluationContext.GLOBAL, workspaceFetcher, EventProcessorStub)

        core.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_NOT_FOUND)

    }

    @Test
    fun `draft`() {
        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = Experiment.Status.DRAFT) {
                variations(Variation.A, B, Variation.C)
                overrides {
                    Variation.A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            Target.Key.Type.USER_PROPERTY("grade")
                            Target.Match.Operator.IN("GOLD")
                        }
                    }
                }
            }

            experiment(key = 2, status = Experiment.Status.DRAFT) {
                variations(Variation.A, B, Variation.C)
            }
        }

        val core = HackleCore.create(EvaluationContext.GLOBAL, workspaceFetcher, EventProcessorStub)


        core.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(B, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)

        core.experiment(1, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_DRAFT)


        core.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_DRAFT)

        core.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_DRAFT)

        core.experiment(2, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_DRAFT)

        core.experiment(2, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_DRAFT)

    }

    @Test
    fun `paused`() {
        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = Experiment.Status.PAUSED) {
                variations(Variation.A, B, Variation.C)
                overrides {
                    Variation.A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            Target.Key.Type.USER_PROPERTY("grade")
                            Target.Match.Operator.IN("GOLD")
                        }
                    }
                }
                defaultRule {
                    bucket {
                        Variation.A(0..5000)
                        B(5000..10000)
                    }
                }
            }

            experiment(key = 2, status = Experiment.Status.PAUSED) {
                variations(Variation.A, B, Variation.C)
                defaultRule {
                    bucket {
                        Variation.A(0..5000)
                        B(5000..10000)
                    }
                }
            }
        }

        val core = HackleCore.create(EvaluationContext.GLOBAL, workspaceFetcher, EventProcessorStub)


        core.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(B, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)

        core.experiment(1, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_PAUSED)

        core.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_PAUSED)

        core.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_PAUSED)

        core.experiment(2, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_PAUSED)

        core.experiment(2, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, EXPERIMENT_PAUSED)

    }

    @Test
    fun `running`() {
        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = Experiment.Status.RUNNING) {
                variations(Variation.A, B, Variation.C)
                overrides {
                    Variation.A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            Target.Key.Type.USER_PROPERTY("grade")
                            Target.Match.Operator.IN("GOLD")
                        }
                    }
                }
                defaultRule {
                    bucket {
                        B(0..10000)
                    }
                }
            }

            experiment(key = 2, status = Experiment.Status.RUNNING) {
                variations(Variation.A, B)
                defaultRule {
                    bucket {
                        Variation.A(0..10000)
                    }
                }
            }

            experiment(key = 3, status = Experiment.Status.RUNNING) {
                variations(Variation.A, B)
            }

            experiment(key = 4, status = Experiment.Status.RUNNING) {
                variations {
                    Variation.A(10001, false)
                    B(10002, false)
                    Variation.C(10003, true)
                }
                defaultRule {
                    bucket {
                        Variation.C(0..10000)
                    }
                }
            }
        }

        val core = HackleCore.create(EvaluationContext.GLOBAL, workspaceFetcher, EventProcessorStub)


        core.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(B, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)

        core.experiment(1, HackleUser.of("c"), Variation.A)
            .expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)

        core.experiment(1, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), Variation.A)
            .expect(B, TRAFFIC_ALLOCATED)

        core.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, TRAFFIC_ALLOCATED)

        core.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, TRAFFIC_ALLOCATED)

        core.experiment(2, HackleUser.of(User.builder("c").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, TRAFFIC_ALLOCATED)

        core.experiment(2, HackleUser.of(User.builder("c").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, TRAFFIC_ALLOCATED)

        core.experiment(3, HackleUser.of(User.builder("a").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, TRAFFIC_NOT_ALLOCATED)

        core.experiment(4, HackleUser.of(User.builder("a").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.A, VARIATION_DROPPED)
    }

    @Test
    fun `complete`() {

        val workspaceFetcher = workspaceFetcher {
            experiment(key = 1, status = Experiment.Status.COMPLETED) {
                variations(Variation.A, B, Variation.C, Variation.D)
                winner(Variation.D)
                overrides {
                    Variation.A("a")
                    B("b")
                }
                audiences {
                    target {
                        condition {
                            Target.Key.Type.USER_PROPERTY("grade")
                            Target.Match.Operator.IN("GOLD")
                        }
                    }
                }
                defaultRule {
                    bucket {
                        Variation.A(0..5000)
                        B(5000..10000)
                    }
                }
            }

            experiment(key = 2, status = Experiment.Status.COMPLETED) {
                variations(Variation.A, B, Variation.C, Variation.D)
                winner(Variation.D)
                overrides {
                    Variation.A("a")
                    B("b")
                }
                defaultRule {
                    bucket {
                        Variation.A(0..5000)
                        B(5000..10000)
                    }
                }
            }
        }

        val core = HackleCore.create(EvaluationContext.GLOBAL, workspaceFetcher, EventProcessorStub)


        core.experiment(1, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(B, OVERRIDDEN)

        core.experiment(1, HackleUser.of(User.builder("abc").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)

        core.experiment(1, HackleUser.of(User.builder("abc").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.D, EXPERIMENT_COMPLETED)

        core.experiment(2, HackleUser.of(User.builder("a").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.A, OVERRIDDEN)

        core.experiment(2, HackleUser.of(User.builder("b").property("grade", "SILVER").build()), Variation.A)
            .expect(B, OVERRIDDEN)

        core.experiment(2, HackleUser.of(User.builder("abc").property("grade", "SILVER").build()), Variation.A)
            .expect(Variation.D, EXPERIMENT_COMPLETED)

        core.experiment(2, HackleUser.of(User.builder("abc").property("grade", "GOLD").build()), Variation.A)
            .expect(Variation.D, EXPERIMENT_COMPLETED)
    }


    private fun Decision.expect(variation: Variation, reason: DecisionReason) {
        expectThat(this) {
            get { this.variation } isEqualTo variation
            get { this.reason } isEqualTo reason
        }
    }

    private fun workspaceFetcher(init: WorkspaceDsl.() -> Unit): WorkspaceFetcher {
        return WorkspaceFetcherStub(workspace(init))
    }

    private class WorkspaceFetcherStub(private val workspace: Workspace) : WorkspaceFetcher {
        override fun fetch(): Workspace {
            return workspace
        }
    }

    private object EventProcessorStub : EventProcessor {
        override fun process(event: UserEvent) {
        }

        override fun flush() {
        }
    }
}
