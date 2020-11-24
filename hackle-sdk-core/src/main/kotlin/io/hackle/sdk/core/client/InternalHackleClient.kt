package io.hackle.sdk.core.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.core.decision.Decider
import io.hackle.sdk.core.decision.Decision.*
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * @author Yong
 */
internal class InternalHackleClient(
    private val decider: Decider,
    private val workspaceFetcher: WorkspaceFetcher,
    private val eventProcessor: EventProcessor
) : HackleClient {

    override fun variation(experimentKey: Long, user: User, defaultVariation: Variation): Variation {
        val workspace = workspaceFetcher.fetch() ?: return defaultVariation
        val experiment = workspace.getExperimentOrNull(experimentKey) ?: return defaultVariation
        return when (val decision = decider.decide(experiment, user)) {
            NotAllocated -> defaultVariation
            is ForcedAllocated -> Variation.from(decision.variationKey)
            is NaturalAllocated -> Variation.from(decision.variation.key).also {
                eventProcessor.process(UserEvent.exposure(experiment, decision.variation, user))
            }
        }
    }

    override fun track(event: Event, user: User) {
        val workspace = workspaceFetcher.fetch() ?: return
        val eventType = workspace.getEventTypeOrNull(event.key) ?: EventType.Undefined(event.key)
        eventProcessor.process(UserEvent.track(eventType, event, user))
    }

    override fun close() {
        workspaceFetcher.tryClose()
        eventProcessor.tryClose()
    }
}
