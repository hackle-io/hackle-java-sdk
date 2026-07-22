package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.RemoteConfigParameter

interface Workspace {

    val metadata: Metadata

    val experiments: List<Experiment>
    val featureFlags: List<Experiment>
    val remoteConfigParameters: List<RemoteConfigParameter>
    val inAppMessages: List<InAppMessage>

    fun getExperimentOrNull(experimentKey: Long): Experiment?
    fun getFeatureFlagOrNull(featureKey: Long): Experiment?
    fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameter?
    fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessage?

    fun toProperties(): Map<String, Any>

    interface Metadata {
        val id: Long
        val environmentId: Long
    }
}
