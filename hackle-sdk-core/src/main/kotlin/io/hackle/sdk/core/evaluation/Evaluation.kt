package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace

internal data class Evaluation(
    val variationId: Long?,
    val variationKey: String,
    val reason: DecisionReason,
    val config: ParameterConfiguration?
) {

    companion object {

        private val log = Logger<Evaluation>()

        fun of(workspace: Workspace, variation: Variation, reason: DecisionReason): Evaluation {
            val parameterConfiguration = variation.parameterConfigurationId?.let {
                requireNotNull(workspace.getParameterConfigurationOrNull(it)) { "ParameterConfiguration[$it]" }
            }
            return Evaluation(variation.id, variation.key, reason, parameterConfiguration)
        }

        fun of(workspace: Workspace, experiment: Experiment, variationKey: String, reason: DecisionReason): Evaluation {
            val variation = experiment.getVariationOrNull(variationKey)
            return if (variation != null) {
                of(workspace, variation, reason)
            } else {
                log.debug { "Variation not founded in experiment [${experiment.id} / $variationKey]" }
                Evaluation(null, variationKey, reason, null)
            }
        }
    }
}
