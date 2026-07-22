package io.hackle.sdk.core.evaluation.event

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.RemoteConfigs
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.parameterConfiguration
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class EvaluationEventFactoryTest {

    private val sut = EvaluationEventFactory(object : Clock {
        override fun currentMillis(): Long = 47
        override fun tick(): Long = 48
    })

    @Test
    fun `create`() {
        // given
        val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()
        val workspace = Workspaces.config(
            metadata = Workspaces.configMetadata(modifiedAt = "2024-01-01")
        )

        val variation1 = Experiments.variation(id = 42, key = "B", parameterConfiguration = parameterConfiguration(id = 42))
        val experiment1 = Experiments.config(id = 1, variations = listOf(variation1))
        val evaluation1 = Experiments.evaluation(
            entity = experiment1,
            result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, variation1)
        )

        val variation2 = Experiments.variation(id = 320, key = "A")
        val experiment2 = Experiments.config(
            id = 2,
            type = Experiment.Type.FEATURE_FLAG,
            version = 2,
            executionVersion = 3,
            variations = listOf(variation2)
        )
        val evaluation2 = Experiments.evaluation(
            entity = experiment2,
            result = Experiments.result(DecisionReason.DEFAULT_RULE, variation2)
        )

        val parameter = RemoteConfigs.config(id = 1, key = "rc")
        val response = RemoteConfigs.response(
            user = user,
            workspace = workspace,
            evaluation = RemoteConfigs.evaluation(
                parameter = parameter,
                result = RemoteConfigs.result(
                    reason = DecisionReason.TARGET_RULE_MATCH,
                    value = RemoteConfigs.value(id = 999, rawValue = "RC")
                )
            ),
            references = listOf(evaluation1, evaluation2)
        )

        // when
        val events = sut.create(response)

        // then
        expectThat(events).hasSize(3)
        expectThat(events[0])
            .isA<UserEvent.RemoteConfig>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs user
                get { parameter } isSameInstanceAs parameter
                get { valueId } isEqualTo 999
                get { decisionReason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { properties } isEqualTo emptyMap()
                get { internalProperties } isEqualTo mapOf("config_modified_at" to "2024-01-01")
            }

        expectThat(events[1])
            .isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs user
                get { experiment } isSameInstanceAs experiment1
                get { variationId } isEqualTo 42
                get { variationKey } isEqualTo "B"
                get { decisionReason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
                get { properties } isEqualTo mapOf(
                    "\$targetingRootType" to "REMOTE_CONFIG",
                    "\$targetingRootId" to 1L,
                    "\$parameterConfigurationId" to 42L,
                    "\$experiment_version" to 1,
                    "\$execution_version" to 1,
                )
                get { internalProperties } isEqualTo mapOf("config_modified_at" to "2024-01-01")
            }

        expectThat(events[2])
            .isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs user
                get { experiment } isSameInstanceAs experiment2
                get { variationId } isEqualTo 320
                get { variationKey } isEqualTo "A"
                get { decisionReason } isEqualTo DecisionReason.DEFAULT_RULE
                get { properties } isEqualTo mapOf(
                    "\$targetingRootType" to "REMOTE_CONFIG",
                    "\$targetingRootId" to 1L,
                    "\$experiment_version" to 2,
                    "\$execution_version" to 3,
                )
            }
    }

    @Test
    fun `create in-app message events`() {
        // given
        val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()
        val workspace = Workspaces.config()

        val variation = Experiments.variation(id = 42, key = "B")
        val experiment = Experiments.config(id = 1, variations = listOf(variation))
        val evaluation = Experiments.evaluation(
            entity = experiment,
            result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, variation)
        )

        val response = InAppMessages.eligibilityResponse(
            user = user,
            workspace = workspace,
            evaluation = InAppMessages.eligibilityEvaluation(
                inAppMessage = InAppMessages.config(id = 1),
                result = InAppMessages.eligibilityResult(
                    isEligible = true,
                    reason = DecisionReason.IN_APP_MESSAGE_TARGET
                )
            ),
            references = listOf(evaluation)
        )

        // when
        val events = sut.create(response)

        // then
        expectThat(events).hasSize(1)
        expectThat(events[0])
            .isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs user
                get { experiment } isSameInstanceAs experiment
                get { variationId } isEqualTo 42
                get { variationKey } isEqualTo "B"
                get { decisionReason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
                get { properties } isEqualTo mapOf(
                    "\$targetingRootType" to "IN_APP_MESSAGE",
                    "\$targetingRootId" to 1L,
                    "\$experiment_version" to 1,
                    "\$execution_version" to 1,
                )
            }
    }
}
