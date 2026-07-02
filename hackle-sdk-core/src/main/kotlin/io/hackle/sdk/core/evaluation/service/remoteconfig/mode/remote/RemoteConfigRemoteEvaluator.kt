package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator

class RemoteConfigRemoteEvaluator(
    private val eventRecorder: EvaluationEventRecorder,
) : RemoteEvaluator<RemoteConfigRemoteEvaluateRequest, RemoteConfigEvaluateResponse>(),
    RemoteConfigEvaluator<RemoteConfigRemoteEvaluateRequest> {

    override fun supports(request: EvaluateRequest): Boolean {
        return request is RemoteConfigRemoteEvaluateRequest
    }

    override fun remoteEvaluate(
        request: RemoteConfigRemoteEvaluateRequest,
        context: Evaluator.Context,
    ): RemoteConfigEvaluateResponse {
        return RemoteConfigEvaluateResponse.of(request, context, request.entity)
    }

    override fun record(
        request: RemoteConfigRemoteEvaluateRequest,
        response: RemoteConfigEvaluateResponse,
    ) {
        eventRecorder.record(response)
    }
}
