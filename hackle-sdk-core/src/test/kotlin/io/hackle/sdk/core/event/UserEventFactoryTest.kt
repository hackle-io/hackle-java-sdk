package io.hackle.sdk.core.event

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.remoteConfigRequest
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.InAppMessages
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class UserEventFactoryTest {

    private lateinit var sut: UserEventFactory

    @BeforeEach
    fun before() {
        sut = UserEventFactory(
            object : WorkspaceFetcher {
                override val lastModified: String get() = "Tue, 16 Jan 2024 07:39:44 GMT"
                override fun fetch(): Workspace? = null
            },
            object : Clock {
                override fun currentMillis(): Long = 47
                override fun tick(): Long = 48
            }
        )
    }

    @Test
    fun `create`() {
        val context = Evaluators.context()
        val evaluation1 = ExperimentEvaluation(
            DecisionReason.TRAFFIC_ALLOCATED,
            emptyList(),
            experiment(id = 1),
            42,
            "B",
            ParameterConfiguration(42, emptyMap())
        )

        val evaluation2 = ExperimentEvaluation(
            DecisionReason.DEFAULT_RULE,
            emptyList(),
            experiment(id = 2, type = Experiment.Type.FEATURE_FLAG, version = 2, executionVersion = 3),
            320,
            "A",
            null
        )

        context.add(evaluation1)
        context.add(evaluation2)

        val request = remoteConfigRequest<Any>()
        val evaluation = RemoteConfigEvaluation.of(
            request,
            context,
            999,
            "RC",
            DecisionReason.TARGET_RULE_MATCH,
            PropertiesBuilder()
        )

        val events = sut.create(request, evaluation)

        expectThat(events).hasSize(3)
        expectThat(events[0])
            .isA<UserEvent.RemoteConfig>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs request.user
                get { parameter } isSameInstanceAs request.parameter
                get { valueId } isEqualTo 999
                get { decisionReason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { properties } isEqualTo mapOf(
                    "returnValue" to "RC"
                )
                get { internalProperties } isEqualTo mapOf(
                    "\$config_last_modified_at" to "Tue, 16 Jan 2024 07:39:44 GMT"
                )
            }

        expectThat(events[1])
            .isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs request.user
                get { experiment } isSameInstanceAs evaluation1.experiment
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
                get { internalProperties } isEqualTo mapOf(
                    "\$config_last_modified_at" to "Tue, 16 Jan 2024 07:39:44 GMT"
                )
            }

        expectThat(events[2])
            .isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs request.user
                get { experiment } isSameInstanceAs evaluation2.experiment
                get { variationId } isEqualTo 320
                get { variationKey } isEqualTo "A"
                get { decisionReason } isEqualTo DecisionReason.DEFAULT_RULE
                get { properties } isEqualTo mapOf(
                    "\$targetingRootType" to "REMOTE_CONFIG",
                    "\$targetingRootId" to 1L,
                    "\$experiment_version" to 2,
                    "\$execution_version" to 3,
                )
                get { internalProperties } isEqualTo mapOf(
                    "\$config_last_modified_at" to "Tue, 16 Jan 2024 07:39:44 GMT"
                )
            }
    }

    @Test
    fun `create in-app message events`() {
        val context = Evaluators.context()
        val evaluation1 =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, listOf(), experiment(id = 1), 42, "B", null)
        context.add(evaluation1)

        val request = InAppMessages.request()
        val evaluation = InAppMessageEvaluation.of(
            request,
            context,
            DecisionReason.IN_APP_MESSAGE_TARGET,
            request.inAppMessage.messageContext.messages[0]
        )

        val events = sut.create(request, evaluation)

        expectThat(events).hasSize(1)
        expectThat(events[0])
            .isA<UserEvent.Exposure>().and {
                get { timestamp } isEqualTo 47
                get { user } isSameInstanceAs request.user
                get { experiment } isSameInstanceAs evaluation1.experiment
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
