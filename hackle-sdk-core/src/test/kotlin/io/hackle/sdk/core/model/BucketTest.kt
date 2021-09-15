package io.hackle.sdk.core.model

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class BucketTest {

    @Test
    fun `getSlotOrNull`() {
        val s1 = Slot(0, 100, 1)
        val s2 = Slot(100, 200, 2)
        val bucket = Bucket(1, 10000, listOf(s1, s2))

        expectThat(bucket.getSlotOrNull(0)) isSameInstanceAs s1
        expectThat(bucket.getSlotOrNull(99)) isSameInstanceAs s1
        expectThat(bucket.getSlotOrNull(100)) isSameInstanceAs s2
        expectThat(bucket.getSlotOrNull(199)) isSameInstanceAs s2
        expectThat(bucket.getSlotOrNull(200)).isNull()
    }
}