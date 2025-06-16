package io.hackle.sdk.core.model

import io.hackle.sdk.common.HackleMarketingSubscriptionOperations
import io.hackle.sdk.common.HackleMarketingSubscriptionStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleMarketingSubscriptionOperationsExtensionsTest {

    @Test
    fun toPushEvent() {

        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.UNSUBSCRIBED)
            .build()

        val event = operations.toPushSubscriptionEvent()

        expectThat(event) {
            get { key } isEqualTo "\$push_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "UNSUBSCRIBED"
            )
        }
    }

    @Test
    fun toSmsEvent() {

        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.SUBSCRIBED)
            .build()

        val event = operations.toSmsSubscriptionEvent()

        expectThat(event) {
            get { key } isEqualTo "\$sms_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "SUBSCRIBED"
            )
        }
    }

    @Test
    fun toKakaoEvent() {

        val operations = HackleMarketingSubscriptionOperations.builder()
            .global(HackleMarketingSubscriptionStatus.UNKNOWN)
            .build()

        val event = operations.toKakaoSubscriptionEvent()

        expectThat(event) {
            get { key } isEqualTo "\$kakao_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "UNKNOWN"
            )
        }
    }
}
