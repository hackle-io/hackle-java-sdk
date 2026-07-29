package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.model.Entity

interface Evaluation {
    val entity: Entity
    val result: EvaluateResult
}
