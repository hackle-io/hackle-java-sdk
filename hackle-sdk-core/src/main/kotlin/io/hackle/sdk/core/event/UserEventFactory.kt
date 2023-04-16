package io.hackle.sdk.core.event

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
        events.add(create(request, evaluation, timestamp))
        for (e in evaluation.context.evaluations) {
            events.add(create(request, e, timestamp))
        }
        return events
    }

    private fun create(request: Evaluator.Request, evaluation: Evaluator.Evaluation, timestamp: Long): UserEvent {
        @Suppress("UNCHECKED_CAST")
        return when (evaluation) {
            is ExperimentEvaluation -> UserEvent.exposure(
                evaluation.experiment,
                request.user,
                evaluation,
                timestamp
            )

            is RemoteConfigEvaluation<*> -> UserEvent.remoteConfig(
                evaluation.parameter,
                request.user,
                evaluation as RemoteConfigEvaluation<Any>,
                timestamp
            )

            else -> throw IllegalArgumentException()
        }
    }
}