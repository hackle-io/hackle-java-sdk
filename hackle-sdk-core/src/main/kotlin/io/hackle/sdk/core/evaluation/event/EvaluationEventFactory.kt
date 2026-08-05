package io.hackle.sdk.core.evaluation.event

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class EvaluationEventFactory(
    private val clock: Clock,
) {

    fun create(response: EvaluateResponse): List<UserEvent> {

        val timestamp = clock.currentMillis()
        val events = mutableListOf<UserEvent>()

        val rootEvent = create(response.user, response.workspace, response.evaluation, timestamp, PropertiesBuilder())
        if (rootEvent != null) {
            events.add(rootEvent)
        }

        for (reference in response.references) {
            val properties = PropertiesBuilder()
            properties.add(ROOT_TYPE, response.evaluation.entity.serviceType.name)
            properties.add(ROOT_ID, response.evaluation.entity.id)
            val targetEvent = create(response.user, response.workspace, reference, timestamp, properties)
            if (targetEvent != null) {
                events.add(targetEvent)
            }
        }
        return events
    }

    private fun create(
        user: HackleUser,
        workspace: Workspace,
        evaluation: Evaluation,
        timestamp: Long,
        properties: PropertiesBuilder,
    ): UserEvent? {
        return when (evaluation) {
            is ExperimentEvaluation -> {
                properties.add(CONFIG_ID_PROPERTY_KEY, evaluation.result.variation.parameterConfiguration?.id)
                properties.add(EXPERIMENT_VERSION_KEY, evaluation.entity.version)
                properties.add(EXECUTION_VERSION_KEY, evaluation.entity.executionVersion)
                UserEvent.exposure(timestamp, user, workspace, evaluation, properties.build())
            }

            is RemoteConfigEvaluation -> {
                UserEvent.remoteConfig(timestamp, user, workspace, evaluation, properties.build())
            }

            else -> null
        }
    }

    companion object {
        private const val ROOT_TYPE = "\$targetingRootType"
        private const val ROOT_ID = "\$targetingRootId"

        private const val CONFIG_ID_PROPERTY_KEY = "\$parameterConfigurationId"

        private const val EXPERIMENT_VERSION_KEY = "\$experiment_version"
        private const val EXECUTION_VERSION_KEY = "\$execution_version"
    }
}
