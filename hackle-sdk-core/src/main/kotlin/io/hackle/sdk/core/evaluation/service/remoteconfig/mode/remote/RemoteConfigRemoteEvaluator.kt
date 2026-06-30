package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResult
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.model.RemoteConfigParameter

class RemoteConfigRemoteEvaluator<T : Any>(
    private val eventRecorder: EvaluationEventRecorder,
) : RemoteEvaluator<RemoteConfigRemoteEvaluateRequest<T>, RemoteConfigEvaluateResponse<T>>(),
    RemoteConfigEvaluator<T, RemoteConfigRemoteEvaluateRequest<T>> {

    override fun supports(request: EvaluateRequest): Boolean {
        return request is RemoteConfigRemoteEvaluateRequest<*>
    }

    override fun remoteEvaluate(
        request: RemoteConfigRemoteEvaluateRequest<T>,
        context: Evaluator.Context,
    ): RemoteConfigEvaluateResponse<T> {
        val typedValue = RemoteConfigParameter.cast<T>(request.requiredType, request.entity.value)
        val result = if (typedValue != null) {
            RemoteConfigEvaluateResult.of(request.entity.reason, typedValue, request.entity.valueId)
        } else {
            RemoteConfigEvaluateResult.of(DecisionReason.TYPE_MISMATCH, request.defaultValue, null)
        }
        return RemoteConfigEvaluateResponse.of(request, context, result)
    }

    override fun record(
        request: RemoteConfigRemoteEvaluateRequest<T>,
        response: RemoteConfigEvaluateResponse<T>,
    ) {
        eventRecorder.record(response)
    }
}
