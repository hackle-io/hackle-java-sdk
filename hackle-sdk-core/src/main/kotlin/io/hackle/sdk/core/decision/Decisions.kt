package io.hackle.sdk.core.decision

import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.model.cast

internal fun ExperimentEvaluation.toDecision(): Decision {
    val config = result.variation.parameterConfiguration ?: ParameterConfig.empty()
    return Decision.of(Variation.from(result.variation.key), result.reason, config, entity)
}

internal fun ExperimentEvaluation.toFeatureFlagDecision(): FeatureFlagDecision {
    val config = result.variation.parameterConfiguration ?: ParameterConfig.empty()
    return if (Variation.from(result.variation.key).isControl) {
        FeatureFlagDecision.off(result.reason, config, entity)
    } else {
        FeatureFlagDecision.on(result.reason, config, entity)
    }
}

internal fun <T : Any> RemoteConfigEvaluation.toDecision(type: ValueType, defaultValue: T): RemoteConfigDecision<T> {
    val value = result.value ?: return RemoteConfigDecision.of(defaultValue, result.reason)
    val typedValue = type.cast<T>(value) ?: return RemoteConfigDecision.of(defaultValue, result.reason)
    return RemoteConfigDecision.of(typedValue, result.reason)
}
