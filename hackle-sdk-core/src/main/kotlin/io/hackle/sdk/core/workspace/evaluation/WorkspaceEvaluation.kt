package io.hackle.sdk.core.workspace.evaluation

import io.hackle.sdk.core.model.Entity
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.evaluation.entity.ExperimentRemoteEvaluateResult
import io.hackle.sdk.core.workspace.evaluation.entity.InAppMessageEligibilityRemoteEvaluateResult
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteConfigParameterRemoteEvaluateResult
import io.hackle.sdk.core.workspace.evaluation.entity.RemoteEvaluateResult

interface WorkspaceEvaluation : Workspace {
    override val metadata: Metadata

    override val experiments: List<ExperimentRemoteEvaluateResult>
    override val featureFlags: List<ExperimentRemoteEvaluateResult>
    override val remoteConfigParameters: List<RemoteConfigParameterRemoteEvaluateResult>
    override val inAppMessages: List<InAppMessageEligibilityRemoteEvaluateResult>

    override fun getExperimentOrNull(experimentKey: Long): ExperimentRemoteEvaluateResult?
    override fun getFeatureFlagOrNull(featureKey: Long): ExperimentRemoteEvaluateResult?
    override fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameterRemoteEvaluateResult?
    override fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessageEligibilityRemoteEvaluateResult?

    fun result(entity: Entity): RemoteEvaluateResult?

    interface Metadata : WorkspaceConfig.Metadata {
        val evaluatedAt: Long
    }
}
