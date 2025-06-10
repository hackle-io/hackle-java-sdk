package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.HackleCommonEvent
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.user.HackleUser
import java.util.*

/**
 * @author Yong
 */
sealed class UserEvent {

    abstract val insertId: String
    abstract val timestamp: Long
    abstract val user: HackleUser

    abstract fun with(user: HackleUser): UserEvent

    data class Exposure(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        val experiment: Experiment,
        val variationId: Long?,
        val variationKey: String,
        val decisionReason: DecisionReason,
        val properties: Map<String, Any>,
        val internalProperties: Map<String, Any>
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    data class Track(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        val eventType: EventType,
        val event: HackleCommonEvent
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    data class RemoteConfig(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        val parameter: RemoteConfigParameter,
        val valueId: Long?,
        val decisionReason: DecisionReason,
        val properties: Map<String, Any>,
        val internalProperties: Map<String, Any>
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    companion object {

        internal fun exposure(
            user: HackleUser,
            evaluation: ExperimentEvaluation,
            properties: Map<String, Any>,
            internalProperties: Map<String, Any>,
            timestamp: Long
        ): UserEvent {
            return Exposure(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                experiment = evaluation.experiment,
                variationId = evaluation.variationId,
                variationKey = evaluation.variationKey,
                decisionReason = evaluation.reason,
                properties = properties,
                internalProperties = internalProperties
            )
        }

        internal fun track(eventType: EventType, event: HackleCommonEvent, timestamp: Long, user: HackleUser): UserEvent {
            return Track(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                eventType = eventType,
                event = event
            )
        }

        internal fun remoteConfig(
            user: HackleUser,
            evaluation: RemoteConfigEvaluation<*>,
            properties: Map<String, Any>,
            internalProperties: Map<String, Any>,
            timestamp: Long
        ): UserEvent {
            return RemoteConfig(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                parameter = evaluation.parameter,
                valueId = evaluation.valueId,
                decisionReason = evaluation.reason,
                properties = properties,
                internalProperties = internalProperties
            )
        }
    }
}

val UserEvent.properties
    get() = when (this) {
        is UserEvent.Exposure -> properties
        is UserEvent.Track -> event.properties
        is UserEvent.RemoteConfig -> properties
    }
