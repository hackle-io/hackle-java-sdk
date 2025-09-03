package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Experiment

abstract class ExperimentContextualEvaluator {

    protected abstract val evaluator: Evaluator

    fun evaluate(
        request: Evaluator.Request,
        context: Evaluator.Context,
        experiment: Experiment,
    ): ExperimentEvaluation {
        val evaluation = context[experiment] ?: evaluateInternal(request, context, experiment)
        return evaluation as ExperimentEvaluation
    }

    private fun evaluateInternal(
        request: Evaluator.Request,
        context: Evaluator.Context,
        experiment: Experiment,
    ): ExperimentEvaluation {
        val experimentRequest = ExperimentRequest.of(request, experiment)
        val evaluation = evaluator.evaluate(experimentRequest, context)
        require(evaluation is ExperimentEvaluation) { "Unexpected evaluation [expected=ExperimentEvaluation, actual=${evaluation::class.java.simpleName}]" }
        return resolve(request, context, evaluation)
            .also { context.add(it) }
    }

    protected abstract fun resolve(
        request: Evaluator.Request,
        context: Evaluator.Context,
        evaluation: ExperimentEvaluation,
    ): ExperimentEvaluation
}
