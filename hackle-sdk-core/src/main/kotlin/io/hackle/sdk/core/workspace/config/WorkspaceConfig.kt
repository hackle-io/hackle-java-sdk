package io.hackle.sdk.core.workspace.config

import io.hackle.sdk.core.model.Bucket
import io.hackle.sdk.core.model.Container
import io.hackle.sdk.core.model.Segment
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig
import io.hackle.sdk.core.workspace.config.entity.InAppMessageConfig
import io.hackle.sdk.core.workspace.config.entity.RemoteConfigParameterConfig

interface WorkspaceConfig : Workspace {

    override val metadata: Metadata

    override val experiments: List<ExperimentConfig>
    override val featureFlags: List<ExperimentConfig>
    override val remoteConfigParameters: List<RemoteConfigParameterConfig>
    override val inAppMessages: List<InAppMessageConfig>

    override fun getExperimentOrNull(experimentKey: Long): ExperimentConfig?
    override fun getFeatureFlagOrNull(featureKey: Long): ExperimentConfig?
    override fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameterConfig?
    override fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessageConfig?

    fun getBucketOrNull(bucketId: Long): Bucket?
    fun getSegmentOrNull(segmentKey: String): Segment?
    fun getContainerOrNull(containerId: Long): Container?

    interface Metadata : Workspace.Metadata {
        val modifiedAt: String?
    }
}
