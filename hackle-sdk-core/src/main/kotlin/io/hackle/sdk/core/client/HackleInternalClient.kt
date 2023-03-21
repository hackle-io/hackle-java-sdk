package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.Evaluator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * @author Yong
 */
class HackleInternalClient internal constructor(
    private val evaluator: Evaluator,
    private val workspaceFetcher: WorkspaceFetcher,
    private val eventProcessor: EventProcessor,
) : AutoCloseable {

    fun experiment(experimentKey: Long, user: HackleUser, defaultVariation: Variation): Decision {

        val workspace = workspaceFetcher.fetch() ?: return Decision.of(defaultVariation, SDK_NOT_READY)
        val experiment =
            workspace.getExperimentOrNull(experimentKey) ?: return Decision.of(defaultVariation, EXPERIMENT_NOT_FOUND)

        val evaluation = evaluator.evaluate(workspace, experiment, user, defaultVariation.name)
        eventProcessor.process(UserEvent.exposure(experiment, user, evaluation))

        val variation = Variation.from(evaluation.variationKey)
        val config = evaluation.config ?: ParameterConfig.empty()
        return Decision.of(variation, evaluation.reason, config)
    }

    fun experiments(user: HackleUser): Map<Experiment, Decision> {
        val decisions = hashMapOf<Experiment, Decision>()
        val workspace = workspaceFetcher.fetch() ?: return decisions
        for (experiment in workspace.experiments) {
            val evaluation = evaluator.evaluate(workspace, experiment, user, Variation.CONTROL.name)
            val config = evaluation.config ?: ParameterConfig.empty()
            val decision = Decision.of(Variation.from(evaluation.variationKey), evaluation.reason, config)
            decisions[experiment] = decision
        }
        return decisions
    }

    fun featureFlag(featureKey: Long, user: HackleUser): FeatureFlagDecision {

        val workspace = workspaceFetcher.fetch() ?: return FeatureFlagDecision.off(SDK_NOT_READY)
        val featureFlag =
            workspace.getFeatureFlagOrNull(featureKey) ?: return FeatureFlagDecision.off(FEATURE_FLAG_NOT_FOUND)

        val evaluation = evaluator.evaluate(workspace, featureFlag, user, Variation.CONTROL.name)
        eventProcessor.process(UserEvent.exposure(featureFlag, user, evaluation))

        val variation = Variation.from(evaluation.variationKey)
        val config = evaluation.config ?: ParameterConfig.empty()
        return if (variation.isControl) {
            FeatureFlagDecision.off(evaluation.reason, config)
        } else {
            FeatureFlagDecision.on(evaluation.reason, config)
        }
    }

    fun featureFlags(user: HackleUser): Map<Experiment, FeatureFlagDecision> {
        val decisions = hashMapOf<Experiment, FeatureFlagDecision>()
        val workspace = workspaceFetcher.fetch() ?: return decisions
        for (featureFlag in workspace.featureFlags) {
            val evaluation = evaluator.evaluate(workspace, featureFlag, user, Variation.CONTROL.name)
            val variation = Variation.from(evaluation.variationKey)
            val config = evaluation.config ?: ParameterConfig.empty()
            val decision = if (variation.isControl) {
                FeatureFlagDecision.off(evaluation.reason, config)
            } else {
                FeatureFlagDecision.on(evaluation.reason, config)
            }
            decisions[featureFlag] = decision
        }
        return decisions
    }

    fun track(event: Event, user: HackleUser, timestamp: Long) {
        val eventType = workspaceFetcher.fetch()?.getEventTypeOrNull(event.key) ?: EventType.Undefined(event.key)
        eventProcessor.process(UserEvent.track(eventType, event, timestamp, user))
    }

    fun <T : Any> remoteConfig(
        parameterKey: String,
        user: HackleUser,
        requiredType: ValueType,
        defaultValue: T,
    ): RemoteConfigDecision<T> {
        val workspace = workspaceFetcher.fetch() ?: return RemoteConfigDecision.of(defaultValue, SDK_NOT_READY)
        val parameter = workspace.getRemoteConfigParameterOrNull(parameterKey)
            ?: return RemoteConfigDecision.of(defaultValue, REMOTE_CONFIG_PARAMETER_NOT_FOUND)
        val evaluation = evaluator.evaluate(workspace, parameter, user, requiredType, defaultValue)
        eventProcessor.process(UserEvent.remoteConfig(parameter, user, evaluation))
        return RemoteConfigDecision.of(evaluation.value, evaluation.reason)
    }

    override fun close() {
        workspaceFetcher.tryClose()
        eventProcessor.tryClose()
    }
}
