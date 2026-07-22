package io.hackle.sdk.internal.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.PropertyOperations
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.subscription.HackleSubscriptionOperations
import io.hackle.sdk.common.subscription.HackleSubscriptionStatus
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.toEvent
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.internal.user.HackleUserResolver
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue


/**
 * @author Yong
 */
internal class HackleClientImplTest {

    private lateinit var core: HackleCore

    private lateinit var userResolver: HackleUserResolver

    private lateinit var sut: HackleClientImpl

    @BeforeEach
    fun beforeEach() {
        core = mockk()
        userResolver = HackleUserResolver()
        sut = HackleClientImpl(core, userResolver)
    }

    @Nested
    inner class VariationTest {

        @Test
        fun `key, userId`() {
            // given
            every { core.experiment(any(), any()) } returns Decision.of(Variation.G, TRAFFIC_ALLOCATED)

            // when
            val actual = sut.variation(42, "42")

            // then
            expectThat(actual) isEqualTo Variation.G
            verify(exactly = 1) {
                core.experiment(42, HackleUser.of("42"))
            }
        }

        @Test
        fun `key, user`() {
            // given
            every { core.experiment(any(), any()) } returns Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            val user = User.of("42")

            // when
            val actual = sut.variation(42, user)

            // then
            expectThat(actual) isEqualTo Variation.G
            verify(exactly = 1) {
                core.experiment(42, HackleUser.of(user))
            }
        }
    }

    @Nested
    inner class VariationDetailTest {

        @Test
        fun `key, userId`() {
            // given
            val decision = Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            every { core.experiment(any(), any()) } returns decision

            // when
            val actual = sut.variationDetail(42, "42")

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                core.experiment(42, HackleUser.of("42"))
            }
        }

        @Test
        fun `core 의 experiment 를 호출하고 리턴받은 값을 바로 리턴한다`() {
            // given
            val decision = Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            every { core.experiment(any(), any()) } returns decision
            val user = User.of("42")

            // when
            val actual = sut.variationDetail(42, user)

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                core.experiment(42, HackleUser.of(user))
            }
        }

        @Test
        fun `유효하지 않은 user 면 Control Variation 으로 결정하고 평가하지 않는다`() {
            // when
            val actual = sut.variationDetail(42, User.builder().build())

            // then
            expectThat(actual) isEqualTo Decision.of(Variation.CONTROL, INVALID_INPUT)
            verify { core wasNot Called }
        }

        @Test
        fun `core에서 예외가 발생하면 Control Variation을 리턴한다`() {
            // given
            every { core.experiment(any(), any()) } throws IllegalArgumentException()

            // when
            val actual = sut.variationDetail(42, User.of("42"))

            //then
            expectThat(actual) {
                get { reason } isEqualTo EXCEPTION
                get { variation } isEqualTo Variation.CONTROL
            }
        }
    }

    @Nested
    inner class IsFeatureOnTest {

        @Test
        fun `key, userId`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { core.featureFlag(any(), any()) } returns decision

            // when
            val actual = sut.isFeatureOn(42, "42")

            // then
            expectThat(actual).isTrue()
            verify(exactly = 1) {
                core.featureFlag(42, HackleUser.of("42"))
            }
        }

        @Test
        fun `key, user`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { core.featureFlag(any(), any()) } returns decision
            val user = User.of("42")

            // when
            val actual = sut.isFeatureOn(42, user)

            // then
            expectThat(actual).isTrue()
            verify(exactly = 1) {
                core.featureFlag(42, HackleUser.of(user))
            }
        }
    }

    @Nested
    inner class FeatureFlagDetailTest {

        @Test
        fun `key, userId`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { core.featureFlag(any(), any()) } returns decision

            // when
            val actual = sut.featureFlagDetail(42, "42")

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                core.featureFlag(42, HackleUser.of("42"))
            }
        }


        @Test
        fun `core 의 featureFlag 를 호출하고 리턴받은 값을 그대로 리턴한다`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { core.featureFlag(any(), any()) } returns decision
            val user = User.of("42")

            // when
            val actual = sut.featureFlagDetail(42, user)

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                core.featureFlag(42, HackleUser.of(user))
            }
        }

        @Test
        fun `유효하지 않은 user 면 off 로 결정하고 평가하지 않는다`() {
            // when
            val actual = sut.featureFlagDetail(42, User.builder().build())

            // then
            expectThat(actual) isEqualTo FeatureFlagDecision.off(INVALID_INPUT)
            verify { core wasNot Called }
        }

        @Test
        fun `core 에서 예외가 발생하면 off 를 리턴한다`() {
            // given
            every { core.featureFlag(any(), any()) } throws IllegalArgumentException()

            // when
            val actual = sut.featureFlagDetail(42, User.of("42"))

            // then
            expectThat(actual) isEqualTo FeatureFlagDecision.off(EXCEPTION)
        }
    }

    @Nested
    inner class Track {

        @Test
        fun `key, userId`() {
            sut.track("key", "42")
            verify(exactly = 1) {
                core.track(Event.of("key"), HackleUser.of("42"), any())
            }
        }

        @Test
        fun `key, user`() {
            sut.track("key", User.of("42"))
            verify(exactly = 1) {
                core.track(Event.of("key"), HackleUser.of("42"), any())
            }
        }

        @Test
        fun `core로 event와 user를 전달한다`() {
            // given
            val event = Event.of("key")
            val user = User.of("test")

            // when
            sut.track(event, user)

            //then
            verify(exactly = 1) {
                core.track(event, HackleUser.of(user), any())
            }
        }
    }

    @Nested
    inner class UpdateUserProperties {

        @Test
        fun `track 이벤트로 전송한다`() {
            val operations = PropertyOperations.builder()
                .set("age", 42)
                .build()
            val user = User.of("42")

            sut.updateUserProperties(operations, user)

            verify(exactly = 1) {
                core.track(withArg { expectThat(it.key) isEqualTo "\$properties" }, any(), any())
            }
            verify(exactly = 1) {
                core.flush()
            }
        }


        @Test
        fun `예외 발생해도 무시한다`() {
            val operations = spyk(
                PropertyOperations.builder()
                    .set("age", 42)
                    .build()
            )
            every { operations.toEvent() } throws IllegalArgumentException("fail")
            val user = User.of("42")

            try {
                sut.updateUserProperties(operations, user)
            } catch (e: Throwable) {
                fail("fail")
            }
        }
    }

    @Nested
    inner class UpdateMarketingStatus {
        @Test
        fun `update push subscription status`() {
            val user = User.of("42")

            sut.updatePushSubscriptions(
                HackleSubscriptionOperations
                    .builder()
                    .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
                    .information(HackleSubscriptionStatus.SUBSCRIBED)
                    .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
                    .build(), user
            )

            verify(exactly = 1) {
                core.track(
                    withArg {
                        expectThat(it.key) isEqualTo "\$push_subscriptions"
                        expectThat(it.properties.size) isEqualTo 3
                        expectThat(it.properties["\$marketing"] as String) isEqualTo "UNSUBSCRIBED"
                        expectThat(it.properties["\$information"] as String) isEqualTo "SUBSCRIBED"
                        expectThat(it.properties["custom_key"] as String) isEqualTo "UNKNOWN"
                    },
                    HackleUser.of("42"),
                    any()
                )
            }
            verify(exactly = 1) {
                core.flush()
            }
        }

        @Test
        fun `update sms subscription status`() {
            val user = User.of("42")

            sut.updateSmsSubscriptions(
                HackleSubscriptionOperations
                    .builder()
                    .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
                    .information(HackleSubscriptionStatus.SUBSCRIBED)
                    .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
                    .build(), user
            )

            verify(exactly = 1) {
                core.track(
                    withArg {
                        expectThat(it.key) isEqualTo "\$sms_subscriptions"
                        expectThat(it.properties.size) isEqualTo 3
                        expectThat(it.properties["\$marketing"] as String) isEqualTo "UNSUBSCRIBED"
                        expectThat(it.properties["\$information"] as String) isEqualTo "SUBSCRIBED"
                        expectThat(it.properties["custom_key"] as String) isEqualTo "UNKNOWN"
                    },
                    HackleUser.of("42"),
                    any()
                )
            }
            verify(exactly = 1) {
                core.flush()
            }
        }

        @Test
        fun `update kakao subscription status`() {
            val user = User.of("42")

            sut.updateKakaoSubscriptions(
                HackleSubscriptionOperations
                    .builder()
                    .marketing(HackleSubscriptionStatus.UNSUBSCRIBED)
                    .information(HackleSubscriptionStatus.SUBSCRIBED)
                    .custom("custom_key", HackleSubscriptionStatus.UNKNOWN)
                    .build(), user
            )

            verify(exactly = 1) {
                core.track(
                    withArg {
                        expectThat(it.key) isEqualTo "\$kakao_subscriptions"
                        expectThat(it.properties.size) isEqualTo 3
                        expectThat(it.properties["\$marketing"] as String) isEqualTo "UNSUBSCRIBED"
                        expectThat(it.properties["\$information"] as String) isEqualTo "SUBSCRIBED"
                        expectThat(it.properties["custom_key"] as String) isEqualTo "UNKNOWN"
                    },
                    HackleUser.of("42"),
                    any()
                )
            }
            verify(exactly = 1) {
                core.flush()
            }
        }
    }

    @Nested
    inner class Close {

        @Test
        fun `core를 종료한다`() {
            // given
            mockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")

            // when
            sut.close()

            //then
            verify(exactly = 1) {
                core.tryClose()
            }
            unmockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")
        }
    }
}
