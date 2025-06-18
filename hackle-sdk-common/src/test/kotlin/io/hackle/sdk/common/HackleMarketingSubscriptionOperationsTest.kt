package io.hackle.sdk.common

import io.hackle.sdk.common.channel.HackleSubscriptionOperations
import io.hackle.sdk.common.channel.HackleSubscriptionStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleMarketingSubscriptionOperationsTest {
    @Test
    fun build() {
        val operations = HackleSubscriptionOperations.builder()
            .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
            .information(HackleSubscriptionStatus.SUBSCRIBED)
            .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
            .build()

        expectThat(operations.size).isEqualTo(3)
        expectThat(operations.asMap()).isEqualTo(
            mapOf(
                "\$marketing" to HackleSubscriptionStatus.UNSUBSCRIBED,
                "\$information" to HackleSubscriptionStatus.SUBSCRIBED,
                "custom_key" to HackleSubscriptionStatus.UNKNOWN,
            )
        )
    }
}
