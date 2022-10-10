package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation.*
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.Evaluator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class HackleInternalClientTest {

    @MockK
    private lateinit var evaluator: Evaluator

    @MockK
    private lateinit var workspaceFetcher: WorkspaceFetcher

    @RelaxedMockK
    private lateinit var eventProcessor: EventProcessor

    @InjectMockKs
    private lateinit var sut: HackleInternalClient

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
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `평가 결과로 노출 이벤트를 전송하고 평가된 결과로 결정한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val experiment = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace

            val defaultVariation = J
            val config = mockk<ParameterConfiguration> { every { id } returns 420 }
            val evaluation = Evaluation(320, H.name, TRAFFIC_ALLOCATED, config)
            every { evaluator.evaluate(workspace, experiment, user, defaultVariation.name) } returns evaluation

            // when
            val actual = sut.experiment(42, user, defaultVariation)

            // then
            expectThat(actual) isEqualTo Decision.of(H, TRAFFIC_ALLOCATED, config)
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Exposure>().and {
                            get { this.user } isSameInstanceAs user
                            get { this.experiment } isSameInstanceAs experiment
                            get { this.variationId } isEqualTo 320
                            get { this.variationKey } isEqualTo "H"
                            get { this.decisionReason } isEqualTo TRAFFIC_ALLOCATED
                            get { this.properties } isEqualTo hashMapOf("\$parameterConfigurationId" to 420L)
                        }
                })
            }
        }

        @Test
        fun `평가결과 Config 가 없으면 empty config 를 리턴한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val experiment = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getExperimentOrNull(any()) } returns experiment
            }
            every { workspaceFetcher.fetch() } returns workspace

            val defaultVariation = J
            val evaluation = Evaluation(320, H.name, TRAFFIC_ALLOCATED, null)
            every { evaluator.evaluate(workspace, experiment, user, defaultVariation.name) } returns evaluation

            // when
            val actual = sut.experiment(42, user, defaultVariation)

            // then
            expectThat(actual) isEqualTo Decision.of(H, TRAFFIC_ALLOCATED, ParameterConfig.empty())
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
                    experiment(1, 4, "A", EXPERIMENT_DRAFT, parameterConfiguration42),
                    experiment(3, 7, "B", EXPERIMENT_COMPLETED, parameterConfiguration43),
                    experiment(4, 10, "A", OVERRIDDEN, null),
                    experiment(7, 21, "C", TRAFFIC_ALLOCATED, null),
                    experiment(10, 27, "A", NOT_IN_EXPERIMENT_TARGET, null),
                )
            }

            every { workspaceFetcher.fetch() } returns workspace
            val user = HackleUser.of("TEST_USER_ID")

            // when
            val actual = sut.experiments(user)

            // then
            expectThat(actual) isEqualTo mapOf(
                1L to Decision.of(A, EXPERIMENT_DRAFT, parameterConfiguration42),
                3L to Decision.of(B, EXPERIMENT_COMPLETED, parameterConfiguration43),
                4L to Decision.of(A, OVERRIDDEN),
                7L to Decision.of(C, TRAFFIC_ALLOCATED),
                10L to Decision.of(A, NOT_IN_EXPERIMENT_TARGET),
            )
        }

        private fun experiment(
            experimentKey: Long,
            variationId: Long,
            variationKey: String,
            reason: DecisionReason,
            config: ParameterConfiguration? = null
        ): Experiment {

            val experiment = mockk<Experiment> {
                every { key } returns experimentKey
            }
            val evaluation = Evaluation(variationId, variationKey, reason, config)
            every { evaluator.evaluate(any(), experiment, any(), "A") } returns evaluation
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
            }
            verify { eventProcessor wasNot Called }
        }

        @Test
        fun `평가 결과 이벤트를 전송한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val config = mockk<ParameterConfiguration> { every { id } returns 420 }
            val evaluation = Evaluation(320, A.name, TRAFFIC_ALLOCATED, config)
            every { evaluator.evaluate(workspace, featureFlag, user, A.name) } returns evaluation

            // when
            sut.featureFlag(42, user)

            // then
            verify {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Exposure>()
                        .and {
                            get { this.user } isSameInstanceAs user
                            get { this.experiment } isSameInstanceAs featureFlag
                            get { this.variationId } isEqualTo 320
                            get { this.variationKey } isEqualTo "A"
                            get { this.decisionReason } isEqualTo TRAFFIC_ALLOCATED
                            get { this.properties } isEqualTo hashMapOf("\$parameterConfigurationId" to 420L)
                        }
                })
            }
        }

        @Test
        fun `평가 결과 Control 그룹이면 off로 결정한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val evaluation = Evaluation(320, A.name, TRAFFIC_ALLOCATED, null)
            every { evaluator.evaluate(workspace, featureFlag, user, A.name) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) {
                get { isOn }.isFalse()
                get { reason } isEqualTo TRAFFIC_ALLOCATED
            }
        }

        @Test
        fun `평가 결과가 Control 그룹이 아니면 on으로 결정한다 `() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val evaluation = Evaluation(320, B.name, TRAFFIC_ALLOCATED, null)
            every { evaluator.evaluate(workspace, featureFlag, user, A.name) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) {
                get { isOn }.isTrue()
                get { reason } isEqualTo TRAFFIC_ALLOCATED
            }
        }

        @Test
        fun `평가된 config를 사용한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val config = mockk<ParameterConfiguration> { every { id } returns 420 }
            val evaluation = Evaluation(320, B.name, TRAFFIC_ALLOCATED, config)
            every { evaluator.evaluate(workspace, featureFlag, user, A.name) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) isEqualTo FeatureFlagDecision.on(TRAFFIC_ALLOCATED, config)
        }

        @Test
        fun `평가된 config 가 없으면 empty config 를 사용한다`() {
            // given
            val user = HackleUser.of("TEST_USER_ID")
            val featureFlag = mockk<Experiment>()
            val workspace = mockk<Workspace> {
                every { getFeatureFlagOrNull(any()) } returns featureFlag
            }
            every { workspaceFetcher.fetch() } returns workspace

            val evaluation = Evaluation(320, B.name, TRAFFIC_ALLOCATED, null)
            every { evaluator.evaluate(workspace, featureFlag, user, A.name) } returns evaluation

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) isEqualTo FeatureFlagDecision.on(TRAFFIC_ALLOCATED, ParameterConfig.empty())
        }
    }


    @Nested
    inner class Track {

        @Test
        fun `Workspace를 가져오지 못하면 Undefined 이벤트를 전송한다`() {
            // given
            every { workspaceFetcher.fetch() } returns null

            // when
            sut.track(Event.of("test_event_key"), HackleUser.of("TEST_USER_ID"))

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Track>()
                        .and {
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
            sut.track(Event.of("undefined_event_key"), HackleUser.of("TEST_USER_ID"))

            //then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it)
                        .isA<UserEvent.Track>()
                        .and {
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
            sut.track(Event.of("custom_event_key"), HackleUser.of("TEST_USER_ID"))

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
}
