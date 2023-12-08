package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.*

/**
 * @author Yong
 */
interface Workspace {

    val id: Long

    val environmentId: Long

    val experiments: List<Experiment>

    val featureFlags: List<Experiment>

    val inAppMessages: List<InAppMessage>

    fun getExperimentOrNull(experimentKey: Long): Experiment?

    fun getFeatureFlagOrNull(featureKey: Long): Experiment?

    fun getEventTypeOrNull(eventTypeKey: String): EventType?

    fun getBucketOrNull(bucketId: Long): Bucket?

    fun getSegmentOrNull(segmentKey: String): Segment?

    fun getContainerOrNull(containerId: Long): Container?

    fun getParameterConfigurationOrNull(parameterConfigurationId: Long): ParameterConfiguration?

    fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameter?

    fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessage?
}
