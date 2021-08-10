package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.EXPERIMENT_NOT_FOUND
import io.hackle.sdk.common.decision.DecisionReason.SDK_NOT_READY
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.evaluation.Evaluator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * @author Yong
 */
class HackleInternalClient internal constructor(
    private val evaluator: Evaluator,
    private val workspaceFetcher: WorkspaceFetcher,
    private val eventProcessor: EventProcessor,
) : AutoCloseable {

    fun experiment(experimentKey: Long, user: User, defaultVariation: Variation): Decision {

        val workspace = workspaceFetcher.fetch() ?: return Decision.of(defaultVariation, SDK_NOT_READY)
        val experiment =
            workspace.getExperimentOrNull(experimentKey) ?: return Decision.of(defaultVariation, EXPERIMENT_NOT_FOUND)

        val evaluation = evaluator.evaluate(workspace, experiment, user, defaultVariation.name)
        eventProcessor.process(UserEvent.exposure(experiment, user, evaluation))

        return Decision.of(Variation.from(evaluation.variationKey), evaluation.reason)
    }

    fun featureFlag(featureKey: Long, user: User): FeatureFlagDecision {

        val workspace = workspaceFetcher.fetch() ?: return FeatureFlagDecision.off(SDK_NOT_READY)
        val featureFlag =
            workspace.getFeatureFlagOrNull(featureKey) ?: return FeatureFlagDecision.off(EXPERIMENT_NOT_FOUND)

        val evaluation = evaluator.evaluate(workspace, featureFlag, user, Variation.CONTROL.name)
        eventProcessor.process(UserEvent.exposure(featureFlag, user, evaluation))

        val variation = Variation.from(evaluation.variationKey)
        return if (variation.isControl) {
            FeatureFlagDecision.off(evaluation.reason)
        } else {
            FeatureFlagDecision.on(evaluation.reason)
        }
    }

    fun track(event: Event, user: User) {
        val workspace = workspaceFetcher.fetch() ?: return
        val eventType = workspace.getEventTypeOrNull(event.key) ?: EventType.Undefined(event.key)
        eventProcessor.process(UserEvent.track(eventType, event, user))
    }

    override fun close() {
        workspaceFetcher.tryClose()
        eventProcessor.tryClose()
    }
}
