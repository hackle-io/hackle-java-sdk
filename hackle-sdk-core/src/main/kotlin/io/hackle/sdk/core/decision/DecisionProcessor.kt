package io.hackle.sdk.core.decision

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser

interface DecisionProcessor {
    fun experiment(experimentKey: Long, user: HackleUser, defaultVariation: Variation): Decision
    fun experiments(user: HackleUser): Map<Experiment, Decision>
    fun featureFlag(featureKey: Long, user: HackleUser): FeatureFlagDecision
    fun featureFlags(user: HackleUser): Map<Experiment, FeatureFlagDecision>
    fun <T : Any> remoteConfig(
        parameterKey: String,
        user: HackleUser,
        requiredType: ValueType,
        defaultValue: T,
    ): RemoteConfigDecision<T>
}
