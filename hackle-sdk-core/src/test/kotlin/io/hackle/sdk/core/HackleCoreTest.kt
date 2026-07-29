package io.hackle.sdk.core

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.decision.DecisionProcessor
import io.hackle.sdk.core.decision.LocalDecisionProcessor
import io.hackle.sdk.core.evaluation.EvaluateProcessor
import io.hackle.sdk.core.evaluation.service.experiment.match.NoopExperimentManualOverrideStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.NoopInAppMessageHiddenStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.NoopInAppMessageImpressionStorage
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.support.*
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class HackleCoreTest {

    @MockK
    private lateinit var workspaceFetcher: WorkspaceFetcher

    @MockK
    private lateinit var decisionProcessor: DecisionProcessor

    @RelaxedMockK
    private lateinit var eventProcessor: EventProcessor

    @InjectMockKs
    private lateinit var sut: HackleCore

    private val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()

    @Nested
    inner class ExperimentTest {

        @Test
        fun `experiment - DecisionProcessor 에 위임한다`() {
            // given
            val decision = Decision.of(Variation.B, TRAFFIC_ALLOCATED)
            every { decisionProcessor.experiment(42, user) } returns decision

            // when
            val actual = sut.experiment(42, user)

            // then
            expectThat(actual) isSameInstanceAs decision
        }

        @Test
        fun `experiments - DecisionProcessor 에 위임한다`() {
            // given
            val decisions = mapOf<Experiment, Decision>(
                Experiments.config() to Decision.of(Variation.A, TRAFFIC_ALLOCATED)
            )
            every { decisionProcessor.experiments(user) } returns decisions

            // when
            val actual = sut.experiments(user)

            // then
            expectThat(actual) isSameInstanceAs decisions
        }
    }

    @Nested
    inner class FeatureFlagTest {

        @Test
        fun `featureFlag - DecisionProcessor 에 위임한다`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { decisionProcessor.featureFlag(42, user) } returns decision

            // when
            val actual = sut.featureFlag(42, user)

            // then
            expectThat(actual) isSameInstanceAs decision
        }

        @Test
        fun `featureFlags - DecisionProcessor 에 위임한다`() {
            // given
            val decisions = mapOf<Experiment, FeatureFlagDecision>(
                Experiments.config(type = Experiment.Type.FEATURE_FLAG) to FeatureFlagDecision.off(DEFAULT_RULE)
            )
            every { decisionProcessor.featureFlags(user) } returns decisions

            // when
            val actual = sut.featureFlags(user)

            // then
            expectThat(actual) isSameInstanceAs decisions
        }
    }

    @Nested
    inner class RemoteConfigTest {

        @Test
        fun `remoteConfig - DecisionProcessor 에 위임한다`() {
            // given
            val decision = RemoteConfigDecision.of("value", DEFAULT_RULE)
            every {
                decisionProcessor.remoteConfig("parameter_key", user, ValueType.STRING, "default")
            } returns decision

            // when
            val actual = sut.remoteConfig("parameter_key", user, ValueType.STRING, "default")

            // then
            expectThat(actual) isSameInstanceAs decision
        }
    }

    @Nested
    inner class TrackTest {

        @Test
        fun `workspace 가 없으면 internalProperties 없이 track 이벤트를 전송한다`() {
            // given
            every { workspaceFetcher.workspace(any()) } returns null

            // when
            sut.track(Event.of("test_event_key"), user, 42)

            // then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it).isA<UserEvent.Track>().and {
                        get { timestamp } isEqualTo 42L
                        get { event.key } isEqualTo "test_event_key"
                        get { internalProperties } isEqualTo emptyMap()
                    }
                })
            }
        }

        @Test
        fun `workspace 가 있으면 workspace 정보를 internalProperties 에 포함해 전송한다`() {
            // given
            val workspace = Workspaces.config(
                metadata = Workspaces.configMetadata(modifiedAt = "2024-01-01")
            )
            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            sut.track(Event.of("test_event_key"), user, 42)

            // then
            verify(exactly = 1) {
                eventProcessor.process(withArg {
                    expectThat(it).isA<UserEvent.Track>().and {
                        get { internalProperties } isEqualTo mapOf("config_modified_at" to "2024-01-01")
                    }
                })
            }
        }
    }

    @Nested
    inner class FlushTest {

        @Test
        fun `flush - EventProcessor 를 flush 한다`() {
            sut.flush()
            verify(exactly = 1) {
                eventProcessor.flush()
            }
        }
    }

    @Nested
    inner class CloseTest {

        @Test
        fun `close - workspaceFetcher 와 eventProcessor 를 종료한다`() {
            mockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")

            // when
            sut.close()

            //then
            verify(exactly = 1) { workspaceFetcher.tryClose() }
            verify(exactly = 1) { eventProcessor.tryClose() }

            unmockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")
        }
    }

    @Nested
    inner class IntegrationTest {

        private val userA = HackleUser.builder().identifier(IdentifierType.ID, "a").property("grade", "SILVER").build()
        private val userB = HackleUser.builder().identifier(IdentifierType.ID, "b").property("grade", "SILVER").build()
        private val userC = HackleUser.builder().identifier(IdentifierType.ID, "c").property("grade", "SILVER").build()
        private val userGold = HackleUser.builder().identifier(IdentifierType.ID, "c").property("grade", "GOLD").build()

        private val goldAudience = Targets.create(
            Targets.condition(
                key = Targets.key(Target.Key.Type.USER_PROPERTY, "grade"),
                match = Targets.match(values = listOf("GOLD"))
            )
        )

        private fun core(workspace: WorkspaceConfig): HackleCore {
            val workspaceFetcher = FixedWorkspaceConfigFetcher(workspace)
            val eventProcessor = mockk<EventProcessor>(relaxed = true)
            val evaluateProcessor = EvaluateProcessor.create(
                context = HackleCoreContext.create(),
                clock = Clock.SYSTEM,
                eventProcessor = eventProcessor,
                overrideStorage = NoopExperimentManualOverrideStorage,
                impressionStorage = NoopInAppMessageImpressionStorage,
                hiddenStorage = NoopInAppMessageHiddenStorage,
            )
            return HackleCore(
                workspaceFetcher,
                LocalDecisionProcessor(workspaceFetcher, evaluateProcessor),
                eventProcessor
            )
        }

        private fun Decision.expect(variation: Variation, reason: DecisionReason) {
            expectThat(this) {
                get { this.variation } isEqualTo variation
                get { this.reason } isEqualTo reason
            }
        }

        @Test
        fun `not found experiment`() {
            val core = core(Workspaces.config())

            core.experiment(1, userA).expect(Variation.A, EXPERIMENT_NOT_FOUND)
        }

        @Test
        fun `draft`() {
            val variations1 = Experiments.variations("A", "B", "C")
            val experiment1 = Experiments.config(
                id = 1,
                key = 1,
                status = Experiment.Status.DRAFT,
                variations = variations1,
                userOverrides = mapOf("a" to variations1[0].id, "b" to variations1[1].id),
                targetAudiences = listOf(goldAudience),
            )
            val experiment2 = Experiments.config(
                id = 2,
                key = 2,
                status = Experiment.Status.DRAFT,
                variations = Experiments.variations("A", "B", "C"),
            )
            val core = core(Workspaces.config(experiments = listOf(experiment1, experiment2)))

            core.experiment(1, userA).expect(Variation.A, OVERRIDDEN)
            core.experiment(1, userB).expect(Variation.B, OVERRIDDEN)
            core.experiment(1, userC).expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)
            core.experiment(1, userGold).expect(Variation.A, EXPERIMENT_DRAFT)

            core.experiment(2, userA).expect(Variation.A, EXPERIMENT_DRAFT)
            core.experiment(2, userB).expect(Variation.A, EXPERIMENT_DRAFT)
            core.experiment(2, userC).expect(Variation.A, EXPERIMENT_DRAFT)
            core.experiment(2, userGold).expect(Variation.A, EXPERIMENT_DRAFT)
        }

        @Test
        fun `paused`() {
            val variations1 = Experiments.variations("A", "B", "C")
            val experiment1 = Experiments.config(
                id = 1,
                key = 1,
                status = Experiment.Status.PAUSED,
                variations = variations1,
                userOverrides = mapOf("a" to variations1[0].id, "b" to variations1[1].id),
                targetAudiences = listOf(goldAudience),
            )
            val experiment2 = Experiments.config(
                id = 2,
                key = 2,
                status = Experiment.Status.PAUSED,
                variations = Experiments.variations("A", "B", "C"),
            )
            val core = core(Workspaces.config(experiments = listOf(experiment1, experiment2)))

            core.experiment(1, userA).expect(Variation.A, OVERRIDDEN)
            core.experiment(1, userB).expect(Variation.B, OVERRIDDEN)
            core.experiment(1, userC).expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)
            core.experiment(1, userGold).expect(Variation.A, EXPERIMENT_PAUSED)

            core.experiment(2, userA).expect(Variation.A, EXPERIMENT_PAUSED)
            core.experiment(2, userB).expect(Variation.A, EXPERIMENT_PAUSED)
            core.experiment(2, userC).expect(Variation.A, EXPERIMENT_PAUSED)
            core.experiment(2, userGold).expect(Variation.A, EXPERIMENT_PAUSED)
        }

        @Test
        fun `running`() {
            val variations1 = Experiments.variations("A", "B", "C")
            val bucket1 = bucket(id = 1, slots = listOf(slot(0, 10000, variations1[1].id)))
            val experiment1 = Experiments.config(
                id = 1,
                key = 1,
                status = Experiment.Status.RUNNING,
                variations = variations1,
                userOverrides = mapOf("a" to variations1[0].id, "b" to variations1[1].id),
                targetAudiences = listOf(goldAudience),
                defaultRule = Action.Bucket(bucket1.id),
            )

            val variations2 = Experiments.variations("A", "B")
            val bucket2 = bucket(id = 2, slots = listOf(slot(0, 10000, variations2[0].id)))
            val experiment2 = Experiments.config(
                id = 2,
                key = 2,
                status = Experiment.Status.RUNNING,
                variations = variations2,
                defaultRule = Action.Bucket(bucket2.id),
            )

            val bucket3 = bucket(id = 3, slots = emptyList())
            val experiment3 = Experiments.config(
                id = 3,
                key = 3,
                status = Experiment.Status.RUNNING,
                variations = Experiments.variations("A", "B"),
                defaultRule = Action.Bucket(bucket3.id),
            )

            val variations4 = listOf(
                Experiments.variation(id = 10001, key = "A"),
                Experiments.variation(id = 10002, key = "B"),
                Experiments.variation(id = 10003, key = "C", isDropped = true),
            )
            val bucket4 = bucket(id = 4, slots = listOf(slot(0, 10000, 10003)))
            val experiment4 = Experiments.config(
                id = 4,
                key = 4,
                status = Experiment.Status.RUNNING,
                variations = variations4,
                defaultRule = Action.Bucket(bucket4.id),
            )

            val core = core(
                Workspaces.config(
                    experiments = listOf(experiment1, experiment2, experiment3, experiment4),
                    buckets = listOf(bucket1, bucket2, bucket3, bucket4),
                )
            )

            core.experiment(1, userA).expect(Variation.A, OVERRIDDEN)
            core.experiment(1, userB).expect(Variation.B, OVERRIDDEN)
            core.experiment(1, userC).expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)
            core.experiment(1, HackleUser.builder().identifier(IdentifierType.ID, "c").build())
                .expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)
            core.experiment(1, userGold).expect(Variation.B, TRAFFIC_ALLOCATED)

            core.experiment(2, userA).expect(Variation.A, TRAFFIC_ALLOCATED)
            core.experiment(2, userB).expect(Variation.A, TRAFFIC_ALLOCATED)
            core.experiment(2, userC).expect(Variation.A, TRAFFIC_ALLOCATED)
            core.experiment(2, userGold).expect(Variation.A, TRAFFIC_ALLOCATED)

            core.experiment(3, userA).expect(Variation.A, TRAFFIC_NOT_ALLOCATED)

            core.experiment(4, userA).expect(Variation.A, VARIATION_DROPPED)
        }

        @Test
        fun `complete`() {
            val variations1 = Experiments.variations("A", "B", "C", "D")
            val experiment1 = Experiments.config(
                id = 1,
                key = 1,
                status = Experiment.Status.COMPLETED,
                variations = variations1,
                userOverrides = mapOf("a" to variations1[0].id, "b" to variations1[1].id),
                targetAudiences = listOf(goldAudience),
                winnerVariationKey = "D",
            )

            val variations2 = Experiments.variations("A", "B", "C", "D")
            val experiment2 = Experiments.config(
                id = 2,
                key = 2,
                status = Experiment.Status.COMPLETED,
                variations = variations2,
                userOverrides = mapOf("a" to variations2[0].id, "b" to variations2[1].id),
                winnerVariationKey = "D",
            )

            val core = core(Workspaces.config(experiments = listOf(experiment1, experiment2)))

            core.experiment(1, userA).expect(Variation.A, OVERRIDDEN)
            core.experiment(1, userB).expect(Variation.B, OVERRIDDEN)
            core.experiment(1, userC).expect(Variation.A, NOT_IN_EXPERIMENT_TARGET)
            core.experiment(1, userGold).expect(Variation.D, EXPERIMENT_COMPLETED)

            core.experiment(2, userA).expect(Variation.A, OVERRIDDEN)
            core.experiment(2, userB).expect(Variation.B, OVERRIDDEN)
            core.experiment(2, userC).expect(Variation.D, EXPERIMENT_COMPLETED)
            core.experiment(2, userGold).expect(Variation.D, EXPERIMENT_COMPLETED)
        }

        @Test
        fun `remote config`() {
            val parameter = RemoteConfigs.config(
                key = "rc_key",
                defaultValue = RemoteConfigs.value(rawValue = "remote_value")
            )
            val core = core(Workspaces.config(remoteConfigParameters = listOf(parameter)))

            val actual = core.remoteConfig("rc_key", userA, ValueType.STRING, "default")

            expectThat(actual) isEqualTo RemoteConfigDecision.of("remote_value", DEFAULT_RULE)
        }
    }
}
