package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.evaluation.Evaluation.*
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
    private val workspaceFetcher: WorkspaceFetcher,
    private val eventProcessor: EventProcessor,
    private val evaluator: Evaluator
) : AutoCloseable {

    fun experiment(experimentKey: Long, user: User, defaultVariation: Variation): Decision {

        val workspace = workspaceFetcher.fetch() ?: return Decision.of(defaultVariation, SDK_NOT_READY)
        val experiment =
            workspace.getExperimentOrNull(experimentKey) ?: return Decision.of(defaultVariation, EXPERIMENT_NOT_FOUND)

        val evaluation = evaluator.evaluate(experiment, user, defaultVariation.name)
        eventProcessor.process(UserEvent.exposure(experiment, user, evaluation))

        return when (evaluation) {
            is Identified -> Decision.of(Variation.from(evaluation.variationKey), evaluation.reason)
            is Forced -> Decision.of(Variation.from(evaluation.variationKey), evaluation.reason)
            is Default -> Decision.of(Variation.from(evaluation.variationKey), evaluation.reason)
            is None -> Decision.of(defaultVariation, evaluation.reason)
        }
    }

    fun featureFlag(featureFlagKey: Long, user: User): FeatureFlagDecision {

        val workspace = workspaceFetcher.fetch() ?: return FeatureFlagDecision.off(SDK_NOT_READY)
        val featureFlag =
            workspace.getFeatureFlagOrNull(featureFlagKey) ?: return FeatureFlagDecision.off(FEATURE_FLAG_NOT_FOUND)

        val evaluation = evaluator.evaluate(featureFlag, user)
        eventProcessor.process(UserEvent.exposure(featureFlag, user, evaluation))

        return when (evaluation) {
            is Identified, is Forced -> FeatureFlagDecision.on(evaluation.reason)
            is Default, is None -> FeatureFlagDecision.off(evaluation.reason)
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
