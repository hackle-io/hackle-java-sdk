package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.FeatureFlag

/**
 * @author Yong
 */
sealed class UserEvent {

    abstract val timestamp: Long
    abstract val user: User

    data class Exposure internal constructor(
        override val timestamp: Long,
        override val user: User,
        val experimentId: Long,
        val experimentKey: Long,
        val experimentType: String,
        val variationId: Long?,
        val variationKey: String?,
        val decisionReason: DecisionReason,
    ) : UserEvent()

    class Track internal constructor(
        override val timestamp: Long,
        override val user: User,
        val eventType: EventType,
        val event: Event
    ) : UserEvent()

    companion object {

        private fun generateTimestamp() = System.currentTimeMillis()

        internal fun exposure(experiment: Experiment, user: User, evaluation: Evaluation): UserEvent {
            return Exposure(
                timestamp = generateTimestamp(),
                user = user,
                experimentId = experiment.id,
                experimentKey = experiment.key,
                experimentType = "AB_TEST",
                variationId = evaluation.variationId,
                variationKey = evaluation.variationKey,
                decisionReason = evaluation.reason
            )
        }

        internal fun exposure(featureFlag: FeatureFlag, user: User, evaluation: Evaluation): UserEvent {
            return Exposure(
                timestamp = generateTimestamp(),
                user = user,
                experimentId = featureFlag.id,
                experimentKey = featureFlag.key,
                experimentType = "FEATURE_FLAG",
                variationId = evaluation.variationId,
                variationKey = evaluation.variationKey,
                decisionReason = evaluation.reason
            )
        }

        fun track(eventType: EventType, event: Event, user: User): UserEvent {
            return Track(
                timestamp = generateTimestamp(),
                user = user,
                eventType = eventType,
                event = event
            )
        }
    }
}
