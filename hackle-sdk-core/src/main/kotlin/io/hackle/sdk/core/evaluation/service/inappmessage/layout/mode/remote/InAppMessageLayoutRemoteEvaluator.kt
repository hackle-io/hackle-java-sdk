package io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluator

class InAppMessageLayoutRemoteEvaluator(
    private val eventRecorder: EvaluationEventRecorder,
) : RemoteEvaluator<InAppMessageLayoutRemoteEvaluateRequest, InAppMessageLayoutEvaluateResponse>(),
    InAppMessageLayoutEvaluator<InAppMessageLayoutRemoteEvaluateRequest> {

    override fun supports(request: EvaluateRequest): Boolean {
        return request is InAppMessageLayoutRemoteEvaluateRequest
    }

    override fun remoteEvaluate(
        request: InAppMessageLayoutRemoteEvaluateRequest,
        context: Evaluator.Context,
    ): InAppMessageLayoutEvaluateResponse {
        return InAppMessageLayoutEvaluateResponse.of(request, context, request.entity)
    }

    override fun record(
        request: InAppMessageLayoutRemoteEvaluateRequest,
        response: InAppMessageLayoutEvaluateResponse,
    ) {
        eventRecorder.record(response)
    }
}
