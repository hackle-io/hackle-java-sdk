package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation

/**
 * @author Yong
 */
sealed class UserEvent {

    abstract val timestamp: Long
    abstract val user: User

    class Exposure internal constructor(
        override val timestamp: Long,
        override val user: User,
        val experiment: Experiment,
        val variation: Variation
    ) : UserEvent()

    class Track internal constructor(
        override val timestamp: Long,
        override val user: User,
        val eventType: EventType,
        val event: Event
    ) : UserEvent()

    companion object {

        private fun generateTimestamp() = System.currentTimeMillis()

        fun exposure(experiment: Experiment, variation: Variation, user: User): UserEvent {
            return Exposure(
                timestamp = generateTimestamp(),
                experiment = experiment,
                user = user,
                variation = variation
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
