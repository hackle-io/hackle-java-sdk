package io.hackle.sdk.core.allocation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.Variation

/**
 * @author Yong
 */
internal sealed class Allocation {
    abstract val decisionReason: DecisionReason

    data class NotAllocated(override val decisionReason: DecisionReason) : Allocation()
    data class ForcedAllocated(override val decisionReason: DecisionReason, val variationKey: String) : Allocation()
    data class Allocated(override val decisionReason: DecisionReason, val variation: Variation) : Allocation()
}
