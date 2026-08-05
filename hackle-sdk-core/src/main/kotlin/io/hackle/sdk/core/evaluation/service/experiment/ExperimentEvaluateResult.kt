package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.model.Variation

interface ExperimentEvaluateResult : EvaluateResult {
    val variation: Variation

    fun with(reason: DecisionReason): ExperimentEvaluateResult {
        return DefaultExperimentEvaluateResult(reason, variation)
    }

    companion object {
        fun of(reason: DecisionReason, variation: Variation): ExperimentEvaluateResult {
            return DefaultExperimentEvaluateResult(
                reason = reason,
                variation = variation
            )
        }

        fun ofControl(reason: DecisionReason, request: ExperimentLocalEvaluateRequest): ExperimentEvaluateResult {
            return of(reason, request.entity.controlVariation)
        }
    }
}

private class DefaultExperimentEvaluateResult(
    override val reason: DecisionReason,
    override val variation: Variation,
) : ExperimentEvaluateResult {
    override fun toString(): String {
        return "ExperimentEvaluateResult(reason=$reason, variation=$variation)"
    }
}
