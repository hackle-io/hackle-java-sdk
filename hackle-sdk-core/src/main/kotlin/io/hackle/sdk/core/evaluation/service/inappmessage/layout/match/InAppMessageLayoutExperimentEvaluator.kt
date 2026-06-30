package io.hackle.sdk.core.evaluation.service.inappmessage.layout.match

import io.hackle.sdk.core.evaluation.evaluator.DelegatingEvaluator
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentReferenceLocalEvaluator

class InAppMessageLayoutExperimentEvaluator(
    override val evaluator: DelegatingEvaluator,
) : ExperimentReferenceLocalEvaluator() {
    override fun resolveEvaluation(
        sourceRequest: LocalEvaluateRequest,
        experimentResponse: ExperimentEvaluateResponse,
    ): ExperimentEvaluation {
        return experimentResponse.evaluation
    }
}
