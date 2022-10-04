package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser

/**
 * @author Yong
 */
sealed class UserEvent {

    abstract val timestamp: Long
    abstract val user: HackleUser

    data class Exposure internal constructor(
        override val timestamp: Long,
        override val user: HackleUser,
        val experiment: Experiment,
        val variationId: Long?,
        val variationKey: String,
        val decisionReason: DecisionReason,
        val properties: Map<String, Any>
    ) : UserEvent()

    data class Track internal constructor(
        override val timestamp: Long,
        override val user: HackleUser,
        val eventType: EventType,
        val event: Event
    ) : UserEvent()

    companion object {

        private fun generateTimestamp() = System.currentTimeMillis()

        internal fun exposure(experiment: Experiment, user: HackleUser, evaluation: Evaluation): UserEvent {
            return Exposure(
                timestamp = generateTimestamp(),
                user = user,
                experiment = experiment,
                variationId = evaluation.variationId,
                variationKey = evaluation.variationKey,
                decisionReason = evaluation.reason,
                properties = exposureProperties(evaluation)
            )
        }

        private const val CONFIG_ID_PROPERTY_KEY = "\$configId"
        
        private fun exposureProperties(evaluation: Evaluation): Map<String, Any> {
            val properties = hashMapOf<String, Any>()
            if (evaluation.config != null) {
                properties[CONFIG_ID_PROPERTY_KEY] = evaluation.config.id
            }
            return properties
        }

        internal fun track(eventType: EventType, event: Event, user: HackleUser): UserEvent {
            return Track(
                timestamp = generateTimestamp(),
                user = user,
                eventType = eventType,
                event = event
            )
        }
    }
}
