package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResult
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.model.AbstractExperiment
import io.hackle.sdk.core.model.Entity
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation

class ExperimentRemoteEvaluateResult(
    override val id: Long,
    override val key: Long,
    override val version: Int,
    override val order: Long,
    override val type: Experiment.Type,
    override val executionVersion: Int,
    override val variation: Variation,
    override val reason: DecisionReason,
    override val references: List<Entity>,
) : AbstractExperiment(),
    ExperimentEvaluateResult,
    RemoteEvaluateResult {
    override fun toEvaluation(): Evaluation {
        return ExperimentEvaluation(this, this)
    }

    override fun toString(): String {
        return "ExperimentRemoteEvaluateResult(id=$id, key=$key, type=$type, version=$version, variation=$variation, reason=$reason)"
    }
}
