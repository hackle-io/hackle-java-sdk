package io.hackle.sdk.core

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.decision.DecisionProcessor
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * DO NOT use this module directly.
 * This module is only used internally by Hackle.
 * Backward compatibility is not supported.
 * Please use server-sdk or client-sdk instead.
 *
 * @author Yong
 */
class HackleCore(
    private val workspaceFetcher: WorkspaceFetcher,
    private val decisionProcessor: DecisionProcessor,
    private val eventProcessor: EventProcessor,
) : AutoCloseable {

    fun experiment(experimentKey: Long, user: HackleUser): Decision {
        return decisionProcessor.experiment(experimentKey, user)
    }

    fun experiments(user: HackleUser): Map<Experiment, Decision> {
        return decisionProcessor.experiments(user)
    }

    fun featureFlag(featureKey: Long, user: HackleUser): FeatureFlagDecision {
        return decisionProcessor.featureFlag(featureKey, user)
    }

    fun featureFlags(user: HackleUser): Map<Experiment, FeatureFlagDecision> {
        return decisionProcessor.featureFlags(user)
    }

    fun track(event: Event, user: HackleUser, timestamp: Long) {
        val workspace = workspaceFetcher.workspace(user)
        val trackEvent = UserEvent.track(timestamp, user, workspace, event)
        eventProcessor.process(trackEvent)
    }

    fun <T : Any> remoteConfig(
        parameterKey: String,
        user: HackleUser,
        requiredType: ValueType,
        defaultValue: T,
    ): RemoteConfigDecision<T> {
        return decisionProcessor.remoteConfig(parameterKey, user, requiredType, defaultValue)
    }

    fun flush() {
        eventProcessor.flush()
    }

    override fun close() {
        workspaceFetcher.tryClose()
        eventProcessor.tryClose()
    }
}
