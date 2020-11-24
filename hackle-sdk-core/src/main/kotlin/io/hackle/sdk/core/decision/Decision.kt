package io.hackle.sdk.core.decision

import io.hackle.sdk.core.model.Variation

/**
 * @author Yong
 */
internal sealed class Decision {
    object NotAllocated : Decision()
    class ForcedAllocated(val variationKey: String) : Decision()
    class NaturalAllocated(val variation: Variation) : Decision()
}
