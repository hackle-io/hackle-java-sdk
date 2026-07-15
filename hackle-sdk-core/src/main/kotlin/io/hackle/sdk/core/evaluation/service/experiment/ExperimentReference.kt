package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.model.Experiment

object ExperimentReference {
    fun resolve(sourceRequest: EvaluateRequest, evaluation: ExperimentEvaluation): ExperimentEvaluation {
        if (sourceRequest is ExperimentEvaluateRequest &&
            evaluation.entity.type == Experiment.Type.AB_TEST &&
            evaluation.result.reason == DecisionReason.TRAFFIC_ALLOCATED
        ) {
            return ExperimentEvaluation(
                evaluation.entity,
                evaluation.result.with(DecisionReason.TRAFFIC_ALLOCATED_BY_TARGETING)
            )
        }
        return evaluation
    }
}
