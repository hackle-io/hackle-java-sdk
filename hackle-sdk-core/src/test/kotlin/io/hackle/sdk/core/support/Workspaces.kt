package io.hackle.sdk.core.support

import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig
import io.hackle.sdk.core.workspace.config.entity.InAppMessageConfig
import io.hackle.sdk.core.workspace.config.entity.RemoteConfigParameterConfig
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.ExperimentRemoteEvaluateResult
import io.hackle.sdk.core.workspace.evaluation.entity.InAppMessageEligibilityRemoteEvaluateResult
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteConfigParameterRemoteEvaluateResult
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteEvaluateResult

internal object Workspaces {

    fun configMetadata(
        id: Long = 1,
        environmentId: Long = 1,
        modifiedAt: String? = null,
    ): WorkspaceConfig.Metadata {
        return TestConfigMetadata(id, environmentId, modifiedAt)
    }

    fun config(
        metadata: WorkspaceConfig.Metadata = configMetadata(),
        experiments: List<ExperimentConfig> = emptyList(),
        featureFlags: List<ExperimentConfig> = emptyList(),
        remoteConfigParameters: List<RemoteConfigParameterConfig> = emptyList(),
        inAppMessages: List<InAppMessageConfig> = emptyList(),
        buckets: List<Bucket> = emptyList(),
        segments: List<Segment> = emptyList(),
        containers: List<Container> = emptyList(),
    ): WorkspaceConfig {
        return TestWorkspaceConfig(
            metadata = metadata,
            experiments = experiments,
            featureFlags = featureFlags,
            remoteConfigParameters = remoteConfigParameters,
            inAppMessages = inAppMessages,
            buckets = buckets,
            segments = segments,
            containers = containers
        )
    }

    fun evaluationMetadata(
        id: Long = 1,
        environmentId: Long = 1,
        modifiedAt: String? = null,
        evaluatedAt: Long = 42,
    ): WorkspaceEvaluation.Metadata {
        return TestEvaluationMetadata(id, environmentId, modifiedAt, evaluatedAt)
    }

    fun evaluation(
        metadata: WorkspaceEvaluation.Metadata = evaluationMetadata(),
        experiments: List<ExperimentRemoteEvaluateResult> = emptyList(),
        featureFlags: List<ExperimentRemoteEvaluateResult> = emptyList(),
        remoteConfigParameters: List<RemoteConfigParameterRemoteEvaluateResult> = emptyList(),
        inAppMessages: List<InAppMessageEligibilityRemoteEvaluateResult> = emptyList(),
    ): WorkspaceEvaluation {
        return TestWorkspaceEvaluation(
            metadata = metadata,
            experiments = experiments,
            featureFlags = featureFlags,
            remoteConfigParameters = remoteConfigParameters,
            inAppMessages = inAppMessages
        )
    }

    private class TestConfigMetadata(
        override val id: Long,
        override val environmentId: Long,
        override val modifiedAt: String?,
    ) : WorkspaceConfig.Metadata

    private class TestEvaluationMetadata(
        override val id: Long,
        override val environmentId: Long,
        override val modifiedAt: String?,
        override val evaluatedAt: Long,
    ) : WorkspaceEvaluation.Metadata

    private class TestWorkspaceConfig(
        override val metadata: WorkspaceConfig.Metadata,
        override val experiments: List<ExperimentConfig>,
        override val featureFlags: List<ExperimentConfig>,
        override val remoteConfigParameters: List<RemoteConfigParameterConfig>,
        override val inAppMessages: List<InAppMessageConfig>,
        private val buckets: List<Bucket>,
        private val segments: List<Segment>,
        private val containers: List<Container>,
    ) : WorkspaceConfig {

        override fun getExperimentOrNull(experimentKey: Long): ExperimentConfig? {
            return experiments.find { it.key == experimentKey }
        }

        override fun getFeatureFlagOrNull(featureKey: Long): ExperimentConfig? {
            return featureFlags.find { it.key == featureKey }
        }

        override fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameterConfig? {
            return remoteConfigParameters.find { it.key == parameterKey }
        }

        override fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessageConfig? {
            return inAppMessages.find { it.key == inAppMessageKey }
        }

        override fun getBucketOrNull(bucketId: Long): Bucket? {
            return buckets.find { it.id == bucketId }
        }

        override fun getSegmentOrNull(segmentKey: String): Segment? {
            return segments.find { it.key == segmentKey }
        }

        override fun getContainerOrNull(containerId: Long): Container? {
            return containers.find { it.id == containerId }
        }

        override fun toProperties(): Map<String, Any> {
            val modifiedAt = metadata.modifiedAt ?: return emptyMap()
            return mapOf("config_modified_at" to modifiedAt)
        }
    }

    private class TestWorkspaceEvaluation(
        override val metadata: WorkspaceEvaluation.Metadata,
        override val experiments: List<ExperimentRemoteEvaluateResult>,
        override val featureFlags: List<ExperimentRemoteEvaluateResult>,
        override val remoteConfigParameters: List<RemoteConfigParameterRemoteEvaluateResult>,
        override val inAppMessages: List<InAppMessageEligibilityRemoteEvaluateResult>,
    ) : WorkspaceEvaluation {

        private val results: List<RemoteEvaluateResult> =
            experiments + featureFlags + remoteConfigParameters + inAppMessages

        override fun getExperimentOrNull(experimentKey: Long): ExperimentRemoteEvaluateResult? {
            return experiments.find { it.key == experimentKey }
        }

        override fun getFeatureFlagOrNull(featureKey: Long): ExperimentRemoteEvaluateResult? {
            return featureFlags.find { it.key == featureKey }
        }

        override fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameterRemoteEvaluateResult? {
            return remoteConfigParameters.find { it.key == parameterKey }
        }

        override fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessageEligibilityRemoteEvaluateResult? {
            return inAppMessages.find { it.key == inAppMessageKey }
        }

        override fun result(entity: Entity): RemoteEvaluateResult? {
            return results.find { it == entity }
        }

        override fun toProperties(): Map<String, Any> {
            val modifiedAt = metadata.modifiedAt ?: return emptyMap()
            return mapOf("config_modified_at" to modifiedAt)
        }
    }
}
