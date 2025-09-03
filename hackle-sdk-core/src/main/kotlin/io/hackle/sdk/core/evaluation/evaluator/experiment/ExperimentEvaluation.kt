package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.Variation

class ExperimentEvaluation internal constructor(
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val experiment: Experiment,
    val variationId: Long?,
    val variationKey: String,
    val config: ParameterConfiguration?,
) : Evaluator.Evaluation {

    fun with(reason: DecisionReason): ExperimentEvaluation {
        return ExperimentEvaluation(reason, targetEvaluations, experiment, variationId, variationKey, config)
    }

    companion object {

        fun of(
            request: ExperimentRequest,
            context: Evaluator.Context,
            variation: Variation,
            reason: DecisionReason,
        ): ExperimentEvaluation {
            val parameterConfigurationId = variation.parameterConfigurationId
            val parameterConfiguration = parameterConfigurationId?.let {
                requireNotNull(request.workspace.getParameterConfigurationOrNull(it)) { "ParameterConfiguration[$it]" }
            }

            return ExperimentEvaluation(
                reason = reason,
                targetEvaluations = context.targetEvaluations,
                experiment = request.experiment,
                variationId = variation.id,
                variationKey = variation.key,
                config = parameterConfiguration
            )
        }

        fun ofDefault(
            request: ExperimentRequest,
            context: Evaluator.Context,
            reason: DecisionReason,
        ): ExperimentEvaluation {
            val variation = request.experiment.getVariationOrNull(request.defaultVariationKey)
            return if (variation != null) {
                of(request, context, variation, reason)
            } else {
                ExperimentEvaluation(
                    reason = reason,
                    targetEvaluations = context.targetEvaluations,
                    experiment = request.experiment,
                    variationId = null,
                    variationKey = request.defaultVariationKey,
                    config = null
                )
            }
        }
    }
}
