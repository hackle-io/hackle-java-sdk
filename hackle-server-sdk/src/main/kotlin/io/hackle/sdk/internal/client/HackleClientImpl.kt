package io.hackle.sdk.internal.client

import io.hackle.sdk.HackleClient
import io.hackle.sdk.common.*
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.EXCEPTION
import io.hackle.sdk.common.decision.DecisionReason.INVALID_INPUT
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.Metrics
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.toEvent
import io.hackle.sdk.internal.monitoring.metrics.DecisionMetrics
import io.hackle.sdk.internal.user.HackleUserResolver

/**
 * @author Yong
 */
internal class HackleClientImpl(
    private val core: HackleCore,
    private val userResolver: HackleUserResolver,
) : HackleClient {

    override fun variation(experimentKey: Long, userId: String): Variation {
        return variation(experimentKey, User.of(userId))
    }

    override fun variation(experimentKey: Long, user: User): Variation {
        return variation(experimentKey, user, Variation.CONTROL)
    }

    override fun variation(experimentKey: Long, user: User, defaultVariation: Variation): Variation {
        return variationDetail(experimentKey, user, defaultVariation).variation
    }

    override fun variationDetail(experimentKey: Long, userId: String): Decision {
        return variationDetail(experimentKey, User.of(userId), Variation.CONTROL)
    }

    override fun variationDetail(experimentKey: Long, user: User): Decision {
        return variationDetail(experimentKey, user, Variation.CONTROL)
    }

    override fun variationDetail(experimentKey: Long, user: User, defaultVariation: Variation): Decision {
        val sample = Timer.start()
        return try {
            val hackleUser = userResolver.resolveOrNull(user) ?: return Decision.of(defaultVariation, INVALID_INPUT)
            core.experiment(experimentKey, hackleUser, defaultVariation)
        } catch (e: Exception) {
            log.error { "Unexpected exception while deciding variation for experiment[$experimentKey]. Returning default variation[$defaultVariation]: $e" }
            Decision.of(defaultVariation, EXCEPTION)
        }.also {
            DecisionMetrics.experiment(sample, experimentKey, it)
        }
    }

    override fun isFeatureOn(featureKey: Long, userId: String): Boolean {
        return isFeatureOn(featureKey, User.of(userId))
    }

    override fun isFeatureOn(featureKey: Long, user: User): Boolean {
        return featureFlagDetail(featureKey, user).isOn
    }

    override fun featureFlagDetail(featureKey: Long, userId: String): FeatureFlagDecision {
        return featureFlagDetail(featureKey, User.of(userId))
    }

    override fun featureFlagDetail(featureKey: Long, user: User): FeatureFlagDecision {
        val sample = Timer.start()
        return try {
            val hackleUser = userResolver.resolveOrNull(user) ?: return FeatureFlagDecision.off(INVALID_INPUT)
            core.featureFlag(featureKey, hackleUser)
        } catch (e: Exception) {
            log.error { "Unexpected exception while deciding feature flag[$featureKey]. Returning default flag[off]: $e" }
            return FeatureFlagDecision.off(EXCEPTION)
        }.also {
            DecisionMetrics.featureFlag(sample, featureKey, it)
        }
    }

    override fun track(eventKey: String, userId: String) {
        track(eventKey, User.of(userId))
    }

    override fun track(eventKey: String, user: User) {
        track(Event.of(eventKey), user)
    }

    override fun track(event: Event, user: User) {
        try {
            val hackleUser = userResolver.resolveOrNull(user) ?: return
            core.track(event, hackleUser, System.currentTimeMillis())
        } catch (e: Exception) {
            log.error { "Unexpected exception while tracking event[${event.key}]: $e" }
        }
    }

    override fun remoteConfig(user: User): HackleRemoteConfig {
        return HackleRemoteConfigImpl(user, core, userResolver)
    }

    override fun updateUserProperties(user: User, operations: PropertyOperations) {
        try {
            val event = operations.toEvent()
            track(event, user)
        } catch (e: Exception) {
            log.error { "Unexpected exception while update user properties: $e" }
        }
    }

    override fun close() {
        core.tryClose()
        Metrics.globalRegistry.tryClose()
    }

    companion object {
        private val log = Logger<HackleClientImpl>()
    }
}
