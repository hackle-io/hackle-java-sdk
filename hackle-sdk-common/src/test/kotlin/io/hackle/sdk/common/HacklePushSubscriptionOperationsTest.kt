package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HacklePushSubscriptionOperationsTest {

    @Test
    fun `build`() {
        val operations = HacklePushSubscriptionOperations.builder()
                .global(HacklePushSubscriptionStatus.UNSUBSCRIBED)
                .build()

        expectThat(operations.size).isEqualTo(1)
        expectThat(operations.asMap()).isEqualTo(mapOf("\$global" to HacklePushSubscriptionStatus.UNSUBSCRIBED))
    }
}
