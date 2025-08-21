package io.hackle.sdk.core.event

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.internal.time.Clock

internal class UserEventFactory(
    private val clock: Clock,
) {

    fun create(request: Evaluator.Request, evaluation: Evaluator.Evaluation): List<UserEvent> {
        val timestamp = clock.currentMillis()
        val events = mutableListOf<UserEvent>()

        val rootEvent = create(request, evaluation, timestamp, PropertiesBuilder())
        if (rootEvent != null) {
            events.add(rootEvent)
        }

        for (targetEvaluation in evaluation.targetEvaluations) {
            val properties = PropertiesBuilder()
            properties.add(ROOT_TYPE, request.key.type.name)
            properties.add(ROOT_ID, request.key.id)
            val targetEvent = create(request, targetEvaluation, timestamp, properties)
            if (targetEvent != null) {
                events.add(targetEvent)
            }
        }

        return events
    }

    private fun create(
        request: Evaluator.Request,
        evaluation: Evaluator.Evaluation,
        timestamp: Long,
        properties: PropertiesBuilder,
    ): UserEvent? {
        return when (evaluation) {
            is ExperimentEvaluation -> {
                properties.add(CONFIG_ID_PROPERTY_KEY, evaluation.config?.id)
                properties.add(EXPERIMENT_VERSION_KEY, evaluation.experiment.version)
                properties.add(EXECUTION_VERSION_KEY, evaluation.experiment.executionVersion)
                UserEvent.exposure(
                    user = request.user,
                    evaluation = evaluation,
                    properties = properties.build(),
                    timestamp = timestamp
                )
            }

            is RemoteConfigEvaluation<*> -> {
                properties.add(evaluation.properties)
                UserEvent.remoteConfig(
                    user = request.user,
                    evaluation = evaluation,
                    properties = properties.build(),
                    timestamp = timestamp
                )
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
