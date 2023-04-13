package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.Variation

internal class ExperimentEvaluation private constructor(
    override val reason: DecisionReason,
    override val context: Evaluator.Context,
    val variationId: Long?,
    val variationKey: String,
    val config: ParameterConfiguration?
) : Evaluator.Evaluation {

    companion object {
        fun ofDefault(
            request: ExperimentRequest,
            context: Evaluator.Context,
            reason: DecisionReason
        ): ExperimentEvaluation {
            val variation = request.experiment.getVariationOrNull(request.defaultVariationKey)
            return if (variation != null) {
                of(request, context, variation, reason)
            } else {
                ExperimentEvaluation(reason, context, null, request.defaultVariationKey, null)
            }
        }

        fun of(
            request: ExperimentRequest,
            context: Evaluator.Context,
            variation: Variation,
            reason: DecisionReason
        ): ExperimentEvaluation {
            val parameterConfigurationId = variation.parameterConfigurationId
            val parameterConfiguration = parameterConfigurationId?.let {
                requireNotNull(request.workspace.getParameterConfigurationOrNull(it)) { "ParameterConfiguration[$it]" }
            }
            return ExperimentEvaluation(reason, context, variation.id, variation.key, parameterConfiguration)
        }
    }
}
