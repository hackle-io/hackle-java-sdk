package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation

class InAppMessageExperimentEvaluator(
    override val evaluator: Evaluator,
) : ExperimentContextualEvaluator() {

    override fun resolve(
        request: Evaluator.Request,
        context: Evaluator.Context,
        evaluation: ExperimentEvaluation,
    ): ExperimentEvaluation {
        context.setProperty("experiment_id", evaluation.experiment.id)
        context.setProperty("experiment_key", evaluation.experiment.key)
        context.setProperty("variation_id", evaluation.variationId)
        context.setProperty("variation_key", evaluation.variationKey)
        context.setProperty("experiment_decision_reason", evaluation.reason.name)

        return evaluation
    }
}
