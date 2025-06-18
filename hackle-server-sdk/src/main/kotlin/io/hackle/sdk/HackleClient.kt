package io.hackle.sdk

import io.hackle.sdk.common.*
import io.hackle.sdk.common.channel.HackleSubscriptionOperations
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.FeatureFlagDecision

/**
 * The entry point of Hackle SDKs.
 *
 * @author Yong
 */
interface HackleClient : AutoCloseable {

    /**
     * Decide the variation to expose to the user for experiment.
     *
     * This method return the [Variation.CONTROL] if:
     * - The experiment key is invalid
     * - The experiment has not started yet
     * - The user is not allocated to the experiment
     * - The decided variation has been dropped
     *
     * This method does not block the calling thread.
     *
     * @param experimentKey the unique key of the experiment.
     * @param userId        the identifier of user to participate in the experiment. MUST NOT be null.
     *
     * @return the decided variation for the user, or [Variation.CONTROL]
     */
    fun variation(experimentKey: Long, userId: String): Variation

    /**
     * Decide the variation to expose to the user for experiment.
     *
     * This method does not block the calling thread.
     *
     * @param experimentKey the unique key of the experiment.
     * @param user          the user to participate in the experiment. MUST NOT be null.
     *
     * @return the decided variation for the user, or [Variation.CONTROL]
     */
    fun variation(experimentKey: Long, user: User): Variation

    /**
     * Decide the variation to expose to the user for experiment.
     * Returns the default variation if the variation cannot be decided.
     *
     * This method does not block the calling thread.
     *
     * @param experimentKey    the unique key for the experiment.
     * @param user             the user to participate in the experiment. MUST NOT be null.
     * @param defaultVariation the default variation of the experiment. MUST NOT be null.
     *
     * @return the decided variation for the user, or the default variation.
     */
    fun variation(experimentKey: Long, user: User, defaultVariation: Variation): Variation

    /**
     * Decide the variation to expose to the user for experiment, and returns an object that
     * describes the way the variation was decided.
     *
     * @param experimentKey the unique key of the experiment.
     * @param userId        the identifier of user to participate in the experiment. MUST NOT be null.
     *
     * @return a [Decision] object
     */
    fun variationDetail(experimentKey: Long, userId: String): Decision

    /**
     * Decide the variation to expose to the user for experiment, and returns an object that
     * describes the way the variation was decided.
     *
     * @param experimentKey the unique key of the experiment.
     * @param user          the user to participate in the experiment. MUST NOT be null.
     *
     * @return a [Decision] object
     */
    fun variationDetail(experimentKey: Long, user: User): Decision

    /**
     * Decide the variation to expose to the user for experiment, and returns an object that
     * describes the way the variation was decided.
     *
     * @param experimentKey    the unique key for the experiment.
     * @param user             the user to participate in the experiment. MUST NOT be null.
     * @param defaultVariation the default variation of the experiment. MUST NOT be null.
     *
     * @return a [Decision] object
     */
    fun variationDetail(experimentKey: Long, user: User, defaultVariation: Variation): Decision

    /**
     * Decide whether the feature is turned on to the user.
     *
     * @param featureKey the unique key for the feature.
     * @param userId     the identifier of user.
     *
     * @return True if the feature is on.
     *         False if the feature is off.
     *
     * @since 2.0.0
     */
    fun isFeatureOn(featureKey: Long, userId: String): Boolean

    /**
     * Decide whether the feature is turned on to the user.
     *
     * @param featureKey the unique key for the feature.
     * @param user       the user requesting the feature.
     *
     * @return True if the feature is on.
     *         False if the feature is off.
     *
     * @since 2.0.0
     */
    fun isFeatureOn(featureKey: Long, user: User): Boolean

    /**
     * Decide whether the feature is turned on to the user, and returns an object that
     * describes the way the flag was decided.
     *
     * @param featureKey the unique key for the feature.
     * @param userId     the identifier of user.
     *
     * @return a [FeatureFlagDecision] object
     *
     * @since 2.0.0
     */
    fun featureFlagDetail(featureKey: Long, userId: String): FeatureFlagDecision

    /**
     * Decide whether the feature is turned on to the user, and returns an object that
     * describes the way the flag was decided.
     *
     * @param featureKey the unique key for the feature.
     * @param user       the user requesting the feature.
     *
     * @return a [FeatureFlagDecision] object
     *
     * @since 2.0.0
     */
    fun featureFlagDetail(featureKey: Long, user: User): FeatureFlagDecision

    /**
     * Records the event that occurred by the user.
     *
     * This method does not block the calling thread.
     *
     * @param eventKey the unique key of the event that occurred. MUST NOT be null.
     * @param userId   the identifier of user that occurred the event. MUST NOT be null.
     */
    fun track(eventKey: String, userId: String)

    /**
     * Records the event that occurred by the user.
     *
     * This method does not block the calling thread.
     *
     * @param eventKey the unique key of the event that occurred. MUST NOT be null.
     * @param user     the user that occurred the event. MUST NOT be null.
     */
    fun track(eventKey: String, user: User)

    /**
     * Records the event that occurred by the user.
     *
     * This method does not block the calling thread.
     *
     * @param event the event that occurred. MUST NOT be null.
     * @param user  the user that occurred the event. MUST NOT be null.
     */
    fun track(event: Event, user: User)

    /**
     * Returns an instance of Hackle Remote Config.
     */
    fun remoteConfig(user: User): HackleRemoteConfig

    /**
     * Updates the user's properties.
     *
     * @param operations Property operations to update user properties.
     * @param user the user whose properties will be updated
     */
    fun updateUserProperties(operations: PropertyOperations, user: User)

    /**
     * Updates the user's push subscription status.
     *
     * @param operations The subscription operations.
     * @param user The user whose subscription status will be updated.
     */
    fun updatePushSubscriptions(
        operations: HackleSubscriptionOperations,
        user: User
    )

    /**
     * Updates the user's sms subscription status.
     *
     * @param operations The subscription operations.
     * @param user The user whose subscription status will be updated.
     */
    fun updateSmsSubscriptions(
        operations: HackleSubscriptionOperations,
        user: User
    )

    /**
     * Updates the user's kakao talk subscription status.
     *
     * @param operations The subscription operations.
     * @param user The user whose subscription status will be updated.
     */
    fun updateKakaoSubscriptions(
        operations: HackleSubscriptionOperations,
        user: User
    )

    /**
     * Shutdown the background task and release the resources used for the background task.
     * This should only be called when the application shutdown.
     */
    @Throws(Exception::class)
    override fun close()
}
