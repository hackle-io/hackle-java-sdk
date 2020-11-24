package io.hackle.sdk.core.model

/**
 * @author Yong
 */
class Slot(
    private val startInclusive: Int,
    private val endExclusive: Int,
    val variationId: Long
) {

    fun contains(slotNumber: Int): Boolean {
        return (startInclusive <= slotNumber) and (slotNumber < endExclusive)
    }
}
