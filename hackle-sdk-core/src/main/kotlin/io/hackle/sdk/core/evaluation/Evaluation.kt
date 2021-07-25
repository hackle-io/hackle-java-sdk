package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.Variation

internal data class Evaluation(
    val variationId: Long?,
    val variationKey: String,
    val reason: DecisionReason,
) {

    companion object {
        fun of(reason: DecisionReason, variation: Variation) = Evaluation(variation.id, variation.key, reason)
        fun of(reason: DecisionReason, variationKey: String) = Evaluation(null, variationKey, reason)
    }
}