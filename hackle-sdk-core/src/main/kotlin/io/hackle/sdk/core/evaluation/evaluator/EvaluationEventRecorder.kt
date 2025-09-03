package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEventFactory
import io.hackle.sdk.core.event.process

class EvaluationEventRecorder(
    private val eventFactory: UserEventFactory,
    private val eventProcessor: EventProcessor,
) {

    fun record(request: Evaluator.Request, evaluation: Evaluator.Evaluation) {
        val events = eventFactory.create(request, evaluation)
        eventProcessor.process(events)
    }
}
