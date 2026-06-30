package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.Variation

interface ExperimentEvaluateResult : EvaluateResult {
    val variationId: Long?
    val variationKey: String
    val parameterConfiguration: ParameterConfiguration?

    fun with(reason: DecisionReason): ExperimentEvaluateResult {
        return DefaultExperimentEvaluateResult(reason, variationId, variationKey, parameterConfiguration)
    }

    companion object {
        fun of(reason: DecisionReason, variation: Variation): ExperimentEvaluateResult {
            return DefaultExperimentEvaluateResult(
                reason = reason,
                variationId = variation.id,
                variationKey = variation.key,
                parameterConfiguration = variation.parameterConfiguration
            )
        }

        fun ofDefault(reason: DecisionReason, request: ExperimentLocalEvaluateRequest): ExperimentEvaluateResult {
            val variation = request.entity.getVariationOrNull(request.defaultVariationKey)
            return if (variation != null) {
                of(reason, variation)
            } else {
                DefaultExperimentEvaluateResult(
                    reason = reason,
                    variationId = null,
                    variationKey = request.defaultVariationKey,
                    parameterConfiguration = null
                )
            }
        }
    }
}

private class DefaultExperimentEvaluateResult(
    override val reason: DecisionReason,
    override val variationId: Long?,
    override val variationKey: String,
    override val parameterConfiguration: ParameterConfiguration?,
) : ExperimentEvaluateResult
