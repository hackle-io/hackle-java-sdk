package io.hackle.sdk.core.event

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.internal.time.Clock

internal class UserEventFactory(
    private val clock: Clock
) {

    fun create(request: Evaluator.Request, evaluation: Evaluator.Evaluation): List<UserEvent> {
        val timestamp = clock.currentMillis()
        val events = mutableListOf<UserEvent>()

        val rootEvent = create(request, evaluation, timestamp, PropertiesBuilder())
        events.add(rootEvent)

        for (targetEvaluation in evaluation.targetEvaluations) {
            val properties = PropertiesBuilder()
            properties.add(ROOT_TYPE, request.key.type.name)
            properties.add(ROOT_ID, request.key.id)
            val targetEvent = create(request, targetEvaluation, timestamp, properties)
            events.add(targetEvent)
        }

        return events
    }

    private fun create(
        request: Evaluator.Request,
        evaluation: Evaluator.Evaluation,
        timestamp: Long,
        properties: PropertiesBuilder
    ): UserEvent {
        @Suppress("UNCHECKED_CAST")
        return when (evaluation) {
            is ExperimentEvaluation -> {
                properties.add(CONFIG_ID_PROPERTY_KEY, evaluation.config?.id)
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
                    evaluation = evaluation as RemoteConfigEvaluation<Any>,
                    properties = properties.build(),
                    timestamp = timestamp
                )
            }

            else -> throw IllegalArgumentException("Unsupported Evaluation [${evaluation::class.java.simpleName}")
        }
    }

    companion object {
        private const val ROOT_TYPE = "\$evaluatedType"
        private const val ROOT_ID = "\$evaluatedId"

        private const val CONFIG_ID_PROPERTY_KEY = "\$parameterConfigurationId"
    }
}