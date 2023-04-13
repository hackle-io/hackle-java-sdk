package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.internal.time.Clock
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

    data class Exposure internal constructor(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        val experiment: Experiment,
        val variationId: Long?,
        val variationKey: String,
        val decisionReason: DecisionReason,
        val properties: Map<String, Any>
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    data class Track internal constructor(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        val eventType: EventType,
        val event: Event
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    data class RemoteConfig internal constructor(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        val parameter: RemoteConfigParameter,
        val valueId: Long?,
        val decisionReason: DecisionReason,
        val properties: Map<String, Any>,
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    companion object {

        internal fun exposure(experiment: Experiment, user: HackleUser, evaluation: ExperimentEvaluation): UserEvent {
            return Exposure(
                insertId = UUID.randomUUID().toString(),
                timestamp = Clock.SYSTEM.currentMillis(),
                user = user,
                experiment = experiment,
                variationId = evaluation.variationId,
                variationKey = evaluation.variationKey,
                decisionReason = evaluation.reason,
                properties = exposureProperties(evaluation)
            )
        }

        private const val CONFIG_ID_PROPERTY_KEY = "\$parameterConfigurationId"

        private fun exposureProperties(evaluation: ExperimentEvaluation): Map<String, Any> {
            val properties = hashMapOf<String, Any>()
            if (evaluation.config != null) {
                properties[CONFIG_ID_PROPERTY_KEY] = evaluation.config.id
            }
            return properties
        }

        internal fun track(eventType: EventType, event: Event, timestamp: Long, user: HackleUser): UserEvent {
            return Track(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                eventType = eventType,
                event = event
            )
        }

        internal fun remoteConfig(
            parameter: RemoteConfigParameter,
            user: HackleUser,
            evaluation: RemoteConfigEvaluation<Any>,
        ): UserEvent {
            return RemoteConfig(
                insertId = UUID.randomUUID().toString(),
                timestamp = Clock.SYSTEM.currentMillis(),
                user = user,
                parameter = parameter,
                valueId = evaluation.valueId,
                decisionReason = evaluation.reason,
                properties = evaluation.properties
            )
        }
    }
}
