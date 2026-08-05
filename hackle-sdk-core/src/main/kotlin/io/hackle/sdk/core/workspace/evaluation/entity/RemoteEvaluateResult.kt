package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Entity

interface RemoteEvaluateResult : EvaluateResult, Entity {
    val references: List<Entity>
    fun toEvaluation(): Evaluation

    data class Key(val type: String, val id: Long)
}
