package io.hackle.sdk.core.evaluation.service.experiment.mode.remote

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluator

class ExperimentRemoteEvaluator(
    private val eventRecorder: EvaluationEventRecorder,
) : RemoteEvaluator<ExperimentRemoteEvaluateRequest, ExperimentEvaluateResponse>(),
    ExperimentEvaluator<ExperimentRemoteEvaluateRequest> {

    override fun supports(request: EvaluateRequest): Boolean {
        return request is ExperimentRemoteEvaluateRequest
    }

    override fun remoteEvaluate(
        request: ExperimentRemoteEvaluateRequest,
        context: Evaluator.Context,
    ): ExperimentEvaluateResponse {
        return ExperimentEvaluateResponse.of(request, context, request.entity)
    }

    override fun record(request: ExperimentRemoteEvaluateRequest, response: ExperimentEvaluateResponse) {
        eventRecorder.record(response)
    }
}
