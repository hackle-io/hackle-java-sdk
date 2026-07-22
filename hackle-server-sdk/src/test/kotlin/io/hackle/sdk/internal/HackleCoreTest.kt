package io.hackle.sdk.internal

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.HackleCoreContext
import io.hackle.sdk.core.decision.LocalDecisionProcessor
import io.hackle.sdk.core.evaluation.EvaluateProcessor
import io.hackle.sdk.core.evaluation.service.experiment.match.NoopExperimentManualOverrideStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.NoopInAppMessageHiddenStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.NoopInAppMessageImpressionStorage
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.common.Variation
import io.hackle.sdk.internal.event.InMemoryEventProcessor
import io.hackle.sdk.internal.event.toPayload
import io.hackle.sdk.internal.utils.toJson
import io.hackle.sdk.internal.workspace.ResourcesWorkspaceFetcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.*
import java.util.*

internal class HackleCoreTest {

    private fun core(fileName: String, eventProcessor: EventProcessor): HackleCore {
        val workspaceFetcher = ResourcesWorkspaceFetcher(fileName)
        val evaluateProcessor = EvaluateProcessor.create(
            context = HackleCoreContext.create(),
            clock = Clock.SYSTEM,
            eventProcessor = eventProcessor,
            overrideStorage = NoopExperimentManualOverrideStorage,
            impressionStorage = NoopInAppMessageImpressionStorage,
            hiddenStorage = NoopInAppMessageHiddenStorage,
        )
        return HackleCore(
            workspaceFetcher = workspaceFetcher,
            decisionProcessor = LocalDecisionProcessor(workspaceFetcher, evaluateProcessor),
            eventProcessor = eventProcessor
        )
    }

    /*
     *       RC(1)
     *      /     \
     *     /       \
     *  AB(2)     FF(4)
     *    |   \     |
     *    |     \   |
     *  AB(3)     FF(5)
     *              |
     *              |
     *            AB(6)
     */
    @Test
    fun `target_experiment`() {
        val eventProcessor = InMemoryEventProcessor()
        val core = core("target_experiment.json", eventProcessor)

        val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()
        val decision = core.remoteConfig("rc", user, ValueType.STRING, "42")

        expectThat(decision) isEqualTo RemoteConfigDecision.of("Targeting!!", DecisionReason.TARGET_RULE_MATCH)
        expectThat(eventProcessor.processedEvents).hasSize(6)

        expectThat(eventProcessor.processedEvents.first())
            .isA<UserEvent.RemoteConfig>().and {
                get { parameter.key } isEqualTo "rc"
                get { decisionReason } isEqualTo DecisionReason.TARGET_RULE_MATCH
            }
        expectThat(eventProcessor.processedEvents.drop(1)).all {
            isA<UserEvent.Exposure>().and {
                get { properties["\$targetingRootType"] } isEqualTo "REMOTE_CONFIG"
                get { properties["\$targetingRootId"] } isEqualTo 1L
            }
        }

    }

    /*
     *     RC(1)
     *      ↓
     * ┌── AB(2)
     * ↑    ↓
     * |   FF(3)
     * ↑    ↓
     * |   AB(4)
     * └────┘
     */
    @Test
    fun `target_experiment_circular`() {
        val eventProcessor = InMemoryEventProcessor()
        val core = core("target_experiment_circular.json", eventProcessor)

        val user = HackleUser.builder().identifier(IdentifierType.ID, "a").build()
        val exception = assertThrows<IllegalArgumentException> {
            core.remoteConfig("rc", user, ValueType.STRING, "XXX")
        }

        expectThat(exception.message)
            .isNotNull()
            .startsWith("Circular evaluation has occurred")
    }

    /*
     *                     Container(1)
     * ┌──────────────┬───────────────────────────────────────┐
     * | ┌──────────┐ |                                       |
     * | |   AB(2)  | |                                       |
     * | └──────────┘ |                                       |
     * └──────────────┴───────────────────────────────────────┘
     *       25 %                        75 %
     */
    @Test
    fun `container`() {
        val eventProcessor = InMemoryEventProcessor()
        val core = core("container.json", eventProcessor)

        val decision = List(10000) {
            val user = HackleUser.builder().identifier(IdentifierType.ID, UUID.randomUUID().toString()).build()
            core.experiment(2, user)
        }

        expectThat(eventProcessor.processedEvents).hasSize(10000)
        expectThat(decision).hasSize(10000)
        expectThat(decision.count { it.reason == DecisionReason.TRAFFIC_ALLOCATED })
            .isIn(2300..2700)

        expectThat(decision.count { it.reason == DecisionReason.NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT })
            .isIn(7300..7700)
    }

    @Test
    fun `dto`() {
        val eventProcessor = InMemoryEventProcessor()
        val core = core("target_experiment.json", eventProcessor)

        val user = HackleUser.builder()
            .identifier(IdentifierType.ID, "user")
            .identifier("custom", "value")
            .property("age", 30)
            .build()
        core.remoteConfig("rc", user, ValueType.STRING, "42")

        val event = Event.builder("purchase")
            .value(320.0)
            .property("amount", 320)
            .build()

        core.track(event, user, 42)

        val payload = eventProcessor.processedEvents.toPayload()
        expectThat(payload.exposureEvents).hasSize(5)
        expectThat(payload.remoteConfigEvents).hasSize(1)
        expectThat(payload.trackEvents).hasSize(1)

        expectThat(payload.toJson().length).isGreaterThan(2000)
    }

    @Test
    fun `segment_match`() {
        val eventProcessor = InMemoryEventProcessor()
        val core = core("segment_match.json", eventProcessor)

        val user1 = HackleUser.builder().identifier(IdentifierType.ID, "matched_id").build()
        val decision1 = core.experiment(1, user1)
        expectThat(decision1) {
            get { variation } isEqualTo Variation.A
            get { reason } isEqualTo DecisionReason.OVERRIDDEN
            get { experiment }.isNotNull()
        }

        val user2 = HackleUser.builder().identifier(IdentifierType.ID, "not_matched_id").build()
        val decision2 = core.experiment(1, user2)
        expectThat(decision2) {
            get { variation } isEqualTo Variation.A
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            get { experiment }.isNotNull()
        }
    }
}
