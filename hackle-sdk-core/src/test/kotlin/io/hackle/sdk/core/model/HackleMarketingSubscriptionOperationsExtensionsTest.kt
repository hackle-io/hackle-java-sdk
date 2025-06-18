package io.hackle.sdk.core.model

import io.hackle.sdk.common.channel.HackleSubscriptionOperations
import io.hackle.sdk.common.channel.HackleSubscriptionStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleMarketingSubscriptionOperationsExtensionsTest {

    @Test
    fun toPushEvent() {
        val operations = HackleSubscriptionOperations.builder()
            .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
            .information(HackleSubscriptionStatus.SUBSCRIBED)
            .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
            .build()

        val event = operations.toEvent("\$push_subscriptions")

        expectThat(event) {
            get { key } isEqualTo "\$push_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$marketing" to "UNSUBSCRIBED",
                "\$information" to "SUBSCRIBED",
                "custom_key" to "UNKNOWN"
            )
        }
    }

    @Test
    fun toSmsEvent() {
        val operations = HackleSubscriptionOperations.builder()
            .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
            .information(HackleSubscriptionStatus.SUBSCRIBED)
            .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
            .build()

        val event = operations.toEvent("\$sms_subscriptions")

        expectThat(event) {
            get { key } isEqualTo "\$sms_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$marketing" to "UNSUBSCRIBED",
                "\$information" to "SUBSCRIBED",
                "custom_key" to "UNKNOWN"
            )
        }
    }

    @Test
    fun toKakaoEvent() {
        val operations = HackleSubscriptionOperations.builder()
            .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
            .information(HackleSubscriptionStatus.SUBSCRIBED)
            .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
            .build()

        val event = operations.toEvent("\$kakao_subscriptions")

        expectThat(event) {
            get { key } isEqualTo "\$kakao_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$marketing" to "UNSUBSCRIBED",
                "\$information" to "SUBSCRIBED",
                "custom_key" to "UNKNOWN"
            )
        }
    }
}
