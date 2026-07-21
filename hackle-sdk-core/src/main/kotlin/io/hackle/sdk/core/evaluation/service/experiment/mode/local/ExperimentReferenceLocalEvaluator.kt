package io.hackle.sdk.core.evaluation.service.experiment.mode.local

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.EvaluatorFactory
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.mode.local.ReferenceLocalEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig

class ExperimentReferenceLocalEvaluator(
    private val evaluatorFactory: EvaluatorFactory,
) : ReferenceLocalEvaluator<ExperimentConfig, ExperimentEvaluation>() {
    override fun doEvaluate(
        parentRequest: LocalEvaluateRequest,
        context: Evaluator.Context,
        reference: ExperimentConfig,
    ): ExperimentEvaluation {
        val experimentRequest = ExperimentLocalEvaluateRequest.of(parentRequest, reference)
        val experimentEvaluator = evaluatorFactory.experiment(experimentRequest)
        val experimentResponse = experimentEvaluator.evaluate(experimentRequest, context)
        return experimentResponse.evaluation
    }
}
