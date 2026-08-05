package io.hackle.sdk.core.evaluation.event

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.process

class EvaluationEventRecorder(
    private val eventFactory: EvaluationEventFactory,
    private val eventProcessor: EventProcessor,
) {

    fun record(response: EvaluateResponse) {
        val events = eventFactory.create(response)
        eventProcessor.process(events)
    }
}
