package io.hackle.sdk.core.evaluation.bucket

import io.hackle.sdk.core.model.Bucket
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Slot

/**
 * @author Yong
 */
internal class Bucketer {

    fun bucketing(bucket: Bucket, user: HackleUser): Slot? {
        val slotNumber = calculateSlotNumber(seed = bucket.seed, slotSize = bucket.slotSize, value = user.id)
        return bucket.getSlotOrNull(slotNumber)
    }

    fun calculateSlotNumber(seed: Int, slotSize: Int, value: String): Int {
        val hashValue = Murmur3.murmurhash3_x86_32(value, seed)
        return Math.abs(hashValue) % slotSize
    }
}
