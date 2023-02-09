package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
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

        fun of(workspace: Workspace, experiment: Experiment, variationKey: String, reason: DecisionReason): Evaluation {
            val variation = experiment.getVariationOrNull(variationKey)
            return if (variation != null) {
                of(workspace, variation, reason)
            } else {
                Evaluation(null, variationKey, reason, null)
            }
        }

        fun of(workspace: Workspace, variation: Variation, reason: DecisionReason): Evaluation {
            val parameterConfigurationId = variation.parameterConfigurationId
            val parameterConfiguration = parameterConfigurationId?.let {
                requireNotNull(workspace.getParameterConfigurationOrNull(it)) { "ParameterConfiguration[$it]" }
            }
            return Evaluation(variation.id, variation.key, reason, parameterConfiguration)
        }
    }
}


internal data class RemoteConfigEvaluation<out T : Any>(
    val valueId: Long?,
    val value: T,
    val reason: DecisionReason,
    val properties: Map<String, Any?> = emptyMap()
) {

    companion object {
        fun <T : Any> of(
            valueId: Long?,
            value: T,
            reason: DecisionReason,
            propertiesBuilder: PropertiesBuilder
        ): RemoteConfigEvaluation<T> {
            propertiesBuilder.add("returnValue", value)
            return RemoteConfigEvaluation(valueId, value, reason, propertiesBuilder.build())
        }
    }
}