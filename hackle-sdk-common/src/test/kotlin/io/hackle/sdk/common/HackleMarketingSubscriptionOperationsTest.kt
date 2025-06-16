package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleMarketingSubscriptionOperationsTest {
    @Test
    fun `build`() {
        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.SUBSCRIBED)
            .build()

        expectThat(operations.size).isEqualTo(1)
        expectThat(operations.asMap()).isEqualTo(mapOf("\$global" to HackleMarketingSubscriptionStatus.SUBSCRIBED))
    }
}
