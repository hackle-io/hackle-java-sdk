package io.hackle.sdk.internal.client

import io.hackle.sdk.HackleClient
import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.EXCEPTION
import io.hackle.sdk.common.decision.DecisionReason.INVALID_INPUT
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.client.HackleInternalClient
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.internal.user.HackleUserResolver

/**
 * @author Yong
 */
internal class HackleClientImpl(
    private val client: HackleInternalClient,
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
        return try {
            val hackleUser = userResolver.resolveOrNull(user) ?: return Decision.of(defaultVariation, INVALID_INPUT)
            client.experiment(experimentKey, hackleUser, defaultVariation)
        } catch (e: Exception) {
            log.error { "Unexpected exception while deciding variation for experiment[$experimentKey]. Returning default variation[$defaultVariation]: $e" }
            Decision.of(defaultVariation, EXCEPTION)
        }
    }

    override fun allExperimentsDecision(user: User): Map<Long, Decision> {
        return try {
            val hackleUser = userResolver.resolveOrNull(user) ?: return hashMapOf()
            client.experiments(hackleUser)
        } catch (e: Exception) {
            log.error { "Unexpected exception while deciding variation for all experiments: $e" }
            emptyMap()
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
        return try {
            val hackleUser = userResolver.resolveOrNull(user) ?: return FeatureFlagDecision.off(INVALID_INPUT)
            client.featureFlag(featureKey, hackleUser)
        } catch (e: Exception) {
            log.error { "Unexpected exception while deciding feature flag[$featureKey]. Returning default flag[off]: $e" }
            return FeatureFlagDecision.off(EXCEPTION)
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
            client.track(event, hackleUser)
        } catch (e: Exception) {
            log.error { "Unexpected exception while tracking event[${event.key}]: $e" }
        }
    }

    override fun close() {
        client.tryClose()
    }

    companion object {
        private val log = Logger<HackleClientImpl>()
    }
}
