package io.hackle.sdk.core.decision

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.EvaluateProcessor
import io.hackle.sdk.core.evaluation.service.experiment.mode.remote.ExperimentRemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote.RemoteConfigRemoteEvaluateRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluationFetcher

class RemoteDecisionProcessor(
    private val workspaceFetcher: WorkspaceEvaluationFetcher,
    private val evaluateProcessor: EvaluateProcessor,
) : DecisionProcessor {
    override fun experiment(experimentKey: Long, user: HackleUser): Decision {
        val workspace = workspaceFetcher.workspace(user)
            ?: return Decision.of(Variation.CONTROL, DecisionReason.SDK_NOT_READY)

        val experiment = workspace.getExperimentOrNull(experimentKey)
            ?: return Decision.of(Variation.CONTROL, DecisionReason.EXPERIMENT_NOT_FOUND)

        val request = ExperimentRemoteEvaluateRequest.of(workspace, experiment, user)
        val response = evaluateProcessor.experiment(request)

        return response.evaluation.toDecision()
    }

    override fun experiments(user: HackleUser): Map<Experiment, Decision> {
        val decisions = hashMapOf<Experiment, Decision>()
        val workspace = workspaceFetcher.workspace(user) ?: return decisions
        for (experiment in workspace.experiments) {
            val request = ExperimentRemoteEvaluateRequest.of(workspace, experiment, user, record = false)
            val response = evaluateProcessor.experiment(request)
            decisions[experiment] = response.evaluation.toDecision()
        }
        return decisions
    }

    override fun featureFlag(featureKey: Long, user: HackleUser): FeatureFlagDecision {
        val workspace = workspaceFetcher.workspace(user)
            ?: return FeatureFlagDecision.off(DecisionReason.SDK_NOT_READY)
        val featureFlag = workspace.getFeatureFlagOrNull(featureKey)
            ?: return FeatureFlagDecision.off(DecisionReason.FEATURE_FLAG_NOT_FOUND)

        val request = ExperimentRemoteEvaluateRequest.of(workspace, featureFlag, user)
        val response = evaluateProcessor.experiment(request)

        return response.evaluation.toFeatureFlagDecision()
    }

    override fun featureFlags(user: HackleUser): Map<Experiment, FeatureFlagDecision> {
        val decisions = hashMapOf<Experiment, FeatureFlagDecision>()
        val workspace = workspaceFetcher.workspace(user) ?: return decisions
        for (featureFlag in workspace.featureFlags) {
            val request = ExperimentRemoteEvaluateRequest.of(workspace, featureFlag, user, record = false)
            val response = evaluateProcessor.experiment(request)
            decisions[featureFlag] = response.evaluation.toFeatureFlagDecision()
        }
        return decisions
    }

    override fun <T : Any> remoteConfig(
        parameterKey: String,
        user: HackleUser,
        requiredType: ValueType,
        defaultValue: T,
    ): RemoteConfigDecision<T> {
        val workspace = workspaceFetcher.workspace(user)
            ?: return RemoteConfigDecision.of(defaultValue, DecisionReason.SDK_NOT_READY)
        val parameter = workspace.getRemoteConfigParameterOrNull(parameterKey)
            ?: return RemoteConfigDecision.of(defaultValue, DecisionReason.REMOTE_CONFIG_PARAMETER_NOT_FOUND)

        val request = RemoteConfigRemoteEvaluateRequest.of(workspace, parameter, user, requiredType)
        val response = evaluateProcessor.remoteConfig(request)

        return response.evaluation.toDecision(requiredType, defaultValue)
    }
}
