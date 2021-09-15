package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation

internal data class Evaluation(
    val variationId: Long?,
    val variationKey: String,
    val reason: DecisionReason,
) {

    companion object {

        private val log = Logger<Evaluation>()

        fun of(variation: Variation, reason: DecisionReason): Evaluation {
            return Evaluation(variation.id, variation.key, reason)
        }

        fun of(experiment: Experiment, variationKey: String, reason: DecisionReason): Evaluation {
            val variation = experiment.getVariationOrNull(variationKey)
            return if (variation != null) {
                of(variation, reason)
            } else {
                log.debug { "Variation not founded in experiment [${experiment.id} / $variationKey]" }
                Evaluation(null, variationKey, reason)
            }
        }
    }
}
