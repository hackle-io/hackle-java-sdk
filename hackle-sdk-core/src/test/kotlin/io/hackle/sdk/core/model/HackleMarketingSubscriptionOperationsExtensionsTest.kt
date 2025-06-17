package io.hackle.sdk.core.model

import io.hackle.sdk.common.marketing.HackleMarketingChannel
import io.hackle.sdk.common.marketing.HackleMarketingSubscriptionOperations
import io.hackle.sdk.common.marketing.HackleMarketingSubscriptionStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleMarketingSubscriptionOperationsExtensionsTest {

    @Test
    fun toPushEvent() {

        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.UNSUBSCRIBED)
            .set("custom_key", HackleMarketingSubscriptionStatus.SUBSCRIBED)
            .build()

        val event = operations.toSubscriptionEvent(HackleMarketingChannel.PUSH)

        expectThat(event) {
            get { key } isEqualTo "\$push_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "UNSUBSCRIBED",
                "custom_key" to "SUBSCRIBED"
            )
        }
    }

    @Test
    fun toSmsEvent() {

        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.SUBSCRIBED)
            .set("custom_key", HackleMarketingSubscriptionStatus.UNSUBSCRIBED)
            .build()

        val event = operations.toSubscriptionEvent(HackleMarketingChannel.SMS)

        expectThat(event) {
            get { key } isEqualTo "\$sms_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "SUBSCRIBED",
                "custom_key" to "UNSUBSCRIBED"
            )
        }
    }

    @Test
    fun toKakaoEvent() {

        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.UNKNOWN)
            .set("custom_key", HackleMarketingSubscriptionStatus.UNKNOWN)
            .build()

        val event = operations.toSubscriptionEvent(HackleMarketingChannel.KAKAO)

        expectThat(event) {
            get { key } isEqualTo "\$kakao_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "UNKNOWN",
                "custom_key" to "UNKNOWN"
            )
        }
    }
}
