package io.hackle.sdk.core.model

/**
 * @author Yong
 */
data class Slot(
    private val startInclusive: Int,
    private val endExclusive: Int,
    val variationId: Long
) {

    internal fun contains(slotNumber: Int): Boolean {
        return (startInclusive <= slotNumber) and (slotNumber < endExclusive)
    }
}
