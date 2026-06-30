package io.hackle.sdk.core.evaluation.service.experiment.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResult
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.ExperimentLocalEvaluationFlowFactory

class ExperimentLocalEvaluator(
    private val evaluationFlowFactory: ExperimentLocalEvaluationFlowFactory,
    private val eventRecorder: EvaluationEventRecorder,
) : LocalEvaluator<ExperimentLocalEvaluateRequest, ExperimentEvaluateResponse>(),
    ExperimentEvaluator<ExperimentLocalEvaluateRequest> {
    override fun supports(request: EvaluateRequest): Boolean {
        return request is ExperimentLocalEvaluateRequest
    }

    override fun doEvaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Evaluator.Context,
    ): ExperimentEvaluateResponse {
        val flow = evaluationFlowFactory.flow(request.entity.type)
        val result = flow.evaluate(request, context)
            ?: ExperimentEvaluateResult.ofDefault(DecisionReason.TRAFFIC_NOT_ALLOCATED, request)
        return ExperimentEvaluateResponse.of(request, context, result)
    }

    override fun record(request: ExperimentLocalEvaluateRequest, response: ExperimentEvaluateResponse) {
        eventRecorder.record(response)
    }
}
