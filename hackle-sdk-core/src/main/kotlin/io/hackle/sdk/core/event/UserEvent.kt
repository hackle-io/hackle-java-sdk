package io.hackle.sdk.core.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import java.util.*

/**
 * @author Yong
 */
sealed class UserEvent {

    abstract val insertId: String
    abstract val timestamp: Long
    abstract val user: HackleUser
    abstract val properties: Map<String, Any>
    abstract val internalProperties: Map<String, Any>

    abstract fun with(user: HackleUser): UserEvent

    data class Exposure(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        override val properties: Map<String, Any>,
        override val internalProperties: Map<String, Any>,
        val experiment: Experiment,
        val variationId: Long?,
        val variationKey: String,
        val decisionReason: DecisionReason,
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    data class Track(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        override val internalProperties: Map<String, Any>,
        val event: Event,
    ) : UserEvent() {
        override val properties: Map<String, Any> get() = event.properties
        override fun with(user: HackleUser) = copy(user = user)
    }

    data class RemoteConfig(
        override val insertId: String,
        override val timestamp: Long,
        override val user: HackleUser,
        override val properties: Map<String, Any>,
        override val internalProperties: Map<String, Any>,
        val parameter: RemoteConfigParameter,
        val valueId: Long?,
        val decisionReason: DecisionReason,
    ) : UserEvent() {
        override fun with(user: HackleUser) = copy(user = user)
    }

    companion object {

        internal fun exposure(
            timestamp: Long,
            user: HackleUser,
            workspace: Workspace,
            evaluation: ExperimentEvaluation,
            properties: Map<String, Any>,
        ): UserEvent {
            return Exposure(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                properties = properties,
                internalProperties = workspace.toProperties(),
                experiment = evaluation.entity,
                variationId = evaluation.result.variation.id,
                variationKey = evaluation.result.variation.key,
                decisionReason = evaluation.result.reason
            )
        }

        internal fun track(
            timestamp: Long,
            user: HackleUser,
            workspace: Workspace?,
            event: Event,
        ): UserEvent {
            return Track(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                internalProperties = workspace?.toProperties() ?: emptyMap(),
                event = event
            )
        }

        internal fun remoteConfig(
            timestamp: Long,
            user: HackleUser,
            workspace: Workspace,
            evaluation: RemoteConfigEvaluation,
            properties: Map<String, Any>,
        ): UserEvent {
            return RemoteConfig(
                insertId = UUID.randomUUID().toString(),
                timestamp = timestamp,
                user = user,
                properties = properties,
                internalProperties = workspace.toProperties(),
                parameter = evaluation.entity,
                valueId = evaluation.result.value?.id,
                decisionReason = evaluation.result.reason,
            )
        }
    }
}
