package io.hackle.sdk.core.model

/**
 * @author Yong
 */
data class Bucket(
    val id: Long,
    val seed: Int,
    val slotSize: Int,
    private val slots: List<Slot>
) {

    fun getSlotOrNull(slotNumber: Int): Slot? {
        return slots.find { it.contains(slotNumber) }
    }
}
