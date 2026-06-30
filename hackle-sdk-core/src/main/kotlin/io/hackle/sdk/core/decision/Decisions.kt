package io.hackle.sdk.core.decision

import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluation

internal fun ExperimentEvaluation.toDecision(): Decision {
    val config = result.parameterConfiguration ?: ParameterConfig.empty()
    return Decision.of(Variation.from(result.variationKey), result.reason, config, entity)
}

internal fun ExperimentEvaluation.toFeatureFlagDecision(): FeatureFlagDecision {
    val config = result.parameterConfiguration ?: ParameterConfig.empty()
    return if (Variation.from(result.variationKey).isControl) {
        FeatureFlagDecision.off(result.reason, config, entity)
    } else {
        FeatureFlagDecision.on(result.reason, config, entity)
    }
}

internal fun <T : Any> RemoteConfigEvaluation<T>.toDecision(): RemoteConfigDecision<T> {
    return RemoteConfigDecision.of(result.value, result.reason)
}
