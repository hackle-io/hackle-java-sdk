package io.hackle.sdk.core.evaluation.service.experiment.mode.local

import io.hackle.sdk.core.evaluation.evaluator.DelegatingEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.mode.local.ReferenceLocalEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig

abstract class ExperimentReferenceLocalEvaluator : ReferenceLocalEvaluator<ExperimentConfig, ExperimentEvaluation>() {

    protected abstract val evaluator: DelegatingEvaluator

    override fun doEvaluate(
        sourceRequest: LocalEvaluateRequest,
        context: Evaluator.Context,
        reference: ExperimentConfig,
    ): ExperimentEvaluation {
        val experimentRequest = ExperimentLocalEvaluateRequest.of(sourceRequest, reference)
        val experimentResponse = evaluator.evaluate(experimentRequest, context)
        require(experimentResponse is ExperimentEvaluateResponse) { "Unexpected EvaluateResponse (expected=ExperimentEvaluateResponse, actual=${experimentResponse::class.java.simpleName})" }
        return resolveEvaluation(sourceRequest, experimentResponse)
    }

    protected abstract fun resolveEvaluation(
        sourceRequest: LocalEvaluateRequest,
        experimentResponse: ExperimentEvaluateResponse,
    ): ExperimentEvaluation
}
