package io.hackle.sdk.common

import io.hackle.sdk.common.marketing.HackleMarketingSubscriptionOperations
import io.hackle.sdk.common.marketing.HackleMarketingSubscriptionStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleMarketingSubscriptionOperationsTest {
    @Test
    fun build() {
        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.SUBSCRIBED)
            .set("custom_key", HackleMarketingSubscriptionStatus.UNSUBSCRIBED)
            .build()

        expectThat(operations.size).isEqualTo(2)
        expectThat(operations.asMap()).isEqualTo(
            mapOf(
                "\$global" to HackleMarketingSubscriptionStatus.SUBSCRIBED,
                "custom_key" to HackleMarketingSubscriptionStatus.UNSUBSCRIBED
            )
        )
    }
}
