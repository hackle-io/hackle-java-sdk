package io.hackle.sdk.core.decision

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.EvaluateProcessor
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local.RemoteConfigLocalEvaluateRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfigFetcher

class LocalDecisionProcessor(
    private val workspaceFetcher: WorkspaceConfigFetcher,
    private val evaluateProcessor: EvaluateProcessor,
) : DecisionProcessor {
    override fun experiment(experimentKey: Long, user: HackleUser): Decision {
        val workspace = workspaceFetcher.workspace(user)
            ?: return Decision.of(Variation.CONTROL, SDK_NOT_READY)
        val experiment = workspace.getExperimentOrNull(experimentKey)
            ?: return Decision.of(Variation.CONTROL, EXPERIMENT_NOT_FOUND)

        val request = ExperimentLocalEvaluateRequest.of(workspace, experiment, user)
        val response = evaluateProcessor.experiment(request)

        return response.evaluation.toDecision()
    }

    override fun experiments(user: HackleUser): Map<Experiment, Decision> {
        val decisions = hashMapOf<Experiment, Decision>()
        val workspace = workspaceFetcher.workspace(user) ?: return decisions
        for (experiment in workspace.experiments) {
            val request =
                ExperimentLocalEvaluateRequest.of(workspace, experiment, user, record = false)
            val response = evaluateProcessor.experiment(request)
            decisions[experiment] = response.evaluation.toDecision()
        }
        return decisions
    }

    override fun featureFlag(featureKey: Long, user: HackleUser): FeatureFlagDecision {
        val workspace = workspaceFetcher.workspace(user)
            ?: return FeatureFlagDecision.off(SDK_NOT_READY)
        val featureFlag = workspace.getFeatureFlagOrNull(featureKey)
            ?: return FeatureFlagDecision.off(FEATURE_FLAG_NOT_FOUND)

        val request = ExperimentLocalEvaluateRequest.of(workspace, featureFlag, user)
        val response = evaluateProcessor.experiment(request)

        return response.evaluation.toFeatureFlagDecision()
    }

    override fun featureFlags(user: HackleUser): Map<Experiment, FeatureFlagDecision> {
        val decisions = hashMapOf<Experiment, FeatureFlagDecision>()
        val workspace = workspaceFetcher.workspace(user) ?: return decisions
        for (featureFlag in workspace.featureFlags) {
            val request =
                ExperimentLocalEvaluateRequest.of(workspace, featureFlag, user, record = false)
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
            ?: return RemoteConfigDecision.of(defaultValue, SDK_NOT_READY)
        val parameter = workspace.getRemoteConfigParameterOrNull(parameterKey)
            ?: return RemoteConfigDecision.of(defaultValue, REMOTE_CONFIG_PARAMETER_NOT_FOUND)

        val request = RemoteConfigLocalEvaluateRequest.of(workspace, parameter, user, requiredType)
        val response = evaluateProcessor.remoteConfig(request)

        return response.evaluation.toDecision(requiredType, defaultValue)
    }
}
