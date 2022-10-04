package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.*

/**
 * @author Yong
 */
interface Workspace {

    fun getExperimentOrNull(experimentKey: Long): Experiment?

    fun getFeatureFlagOrNull(featureKey: Long): Experiment?

    fun getEventTypeOrNull(eventTypeKey: String): EventType?

    fun getBucketOrNull(bucketId: Long): Bucket?

    fun getSegmentOrNull(segmentKey: String): Segment?

    fun getContainerOrNull(containerId: Long): Container?

    fun getParameterConfigurationOrNull(configId: Long): ParameterConfiguration?
}
