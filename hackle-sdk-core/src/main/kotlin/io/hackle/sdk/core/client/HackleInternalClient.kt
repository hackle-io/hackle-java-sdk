package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.EXPERIMENT_NOT_FOUND
import io.hackle.sdk.common.decision.DecisionReason.SDK_NOT_READY
import io.hackle.sdk.core.allocation.Allocation.*
import io.hackle.sdk.core.allocation.Allocator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * @author Yong
 */
class HackleInternalClient internal constructor(
    private val allocator: Allocator,
    private val workspaceFetcher: WorkspaceFetcher,
    private val eventProcessor: EventProcessor
) : AutoCloseable {

    fun variation(experimentKey: Long, user: User, defaultVariation: Variation): Decision {
        val workspace = workspaceFetcher.fetch() ?: return Decision.of(defaultVariation, SDK_NOT_READY)
        val experiment = workspace.getExperimentOrNull(experimentKey) ?: return Decision.of(defaultVariation, EXPERIMENT_NOT_FOUND)
        return when (val allocation = allocator.allocate(experiment, user)) {
            is NotAllocated -> Decision.of(defaultVariation, allocation.decisionReason)
            is ForcedAllocated -> Decision.of(Variation.from(allocation.variationKey), allocation.decisionReason)
            is Allocated -> Decision.of(Variation.from(allocation.variation.key), allocation.decisionReason).also {
                eventProcessor.process(UserEvent.exposure(experiment, allocation.variation, user))
            }
        }
    }

    fun track(event: Event, user: User) {
        val workspace = workspaceFetcher.fetch() ?: return
        val eventType = workspace.getEventTypeOrNull(event.key) ?: EventType.Undefined(event.key)
        eventProcessor.process(UserEvent.track(eventType, event, user))
    }

    override fun close() {
        workspaceFetcher.tryClose()
        eventProcessor.tryClose()
    }
}
