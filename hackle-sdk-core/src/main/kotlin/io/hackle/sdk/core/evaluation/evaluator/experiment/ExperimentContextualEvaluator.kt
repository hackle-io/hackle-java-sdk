package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Experiment

internal abstract class ExperimentContextualEvaluator internal constructor(
    val evaluator: Evaluator
) {
    abstract fun decorate(
        request: Evaluator.Request,
        context: Evaluator.Context,
        evaluation: Evaluator.Evaluation
    ): ExperimentEvaluation

    fun evaluate(
        request: Evaluator.Request,
        context: Evaluator.Context,
        experiment: Experiment
    ): ExperimentEvaluation {
        val evaluation = context[experiment] ?: evaluateInternal(request, context, experiment)
        return evaluation as ExperimentEvaluation
    }

    private fun evaluateInternal(
        request: Evaluator.Request,
        context: Evaluator.Context,
        experiment: Experiment
    ): ExperimentEvaluation {
        val experimentRequest = ExperimentRequest.of(request, experiment)
        val evaluation = evaluator.evaluate(experimentRequest, context)
        val decoratedEvaluation = decorate(request, context, evaluation)

        context.add(decoratedEvaluation)
        return decoratedEvaluation
    }
}
