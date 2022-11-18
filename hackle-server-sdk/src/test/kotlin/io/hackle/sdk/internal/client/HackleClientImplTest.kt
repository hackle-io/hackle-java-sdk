package io.hackle.sdk.internal.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.HackleRemoteConfig
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.client.HackleInternalClient
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.internal.user.HackleUserResolver
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue


/**
 * @author Yong
 */
internal class HackleClientImplTest {


    private lateinit var client: HackleInternalClient

    private lateinit var userResolver: HackleUserResolver

    private lateinit var remoteConfig: HackleRemoteConfig

    private lateinit var sut: HackleClientImpl

    @BeforeEach
    fun beforeEach() {
        client = mockk()
        userResolver = HackleUserResolver()
        remoteConfig = mockk()
        sut = HackleClientImpl(client, userResolver, remoteConfig)
    }

    @Nested
    inner class VariationTest {

        @Test
        fun `key, userId`() {
            // given
            every { client.experiment(any(), any(), any()) } returns Decision.of(Variation.G, TRAFFIC_ALLOCATED)

            // when
            val actual = sut.variation(42, "42")

            // then
            expectThat(actual) isEqualTo Variation.G
            verify(exactly = 1) {
                client.experiment(42, HackleUser.of("42"), Variation.A)
            }
        }

        @Test
        fun `key, user`() {
            // given
            every { client.experiment(any(), any(), any()) } returns Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            val user = User.of("42")

            // when
            val actual = sut.variation(42, user)

            // then
            expectThat(actual) isEqualTo Variation.G
            verify(exactly = 1) {
                client.experiment(42, HackleUser.of(user), Variation.A)
            }
        }

        @Test
        fun `key, user, defaultVariation`() {
            // given
            every { client.experiment(any(), any(), any()) } returns Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            val user = User.of("42")

            // when
            val actual = sut.variation(42, user, Variation.C)

            // then
            expectThat(actual) isEqualTo Variation.G
            verify(exactly = 1) {
                client.experiment(42, HackleUser.of(user), Variation.C)
            }
        }
    }

    @Nested
    inner class VariationDetailTest {

        @Test
        fun `key, userId`() {
            // given
            val decision = Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            every { client.experiment(any(), any(), any()) } returns decision

            // when
            val actual = sut.variationDetail(42, "42")

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                client.experiment(42, HackleUser.of("42"), Variation.A)
            }
        }

        @Test
        fun `key, user`() {
            // given
            val decision = Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            every { client.experiment(any(), any(), any()) } returns decision
            val user = User.of("42")

            // when
            val actual = sut.variationDetail(42, user)

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                client.experiment(42, HackleUser.of(user), Variation.A)
            }
        }

        @Test
        fun `internalClient 의 experiment 를 호출하고 리턴받은 값을 바로 리턴한다`() {
            // given
            val user = User.of("42")
            val decision = Decision.of(Variation.G, TRAFFIC_ALLOCATED)
            every { client.experiment(any(), any(), any()) } returns decision

            // when
            val actual = sut.variationDetail(42L, user, Variation.J)

            //then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                client.experiment(42L, HackleUser.of("42"), Variation.J)
            }
        }

        @Test
        fun `internalClient에서 예외가 발생하면 defaultVariation을 리턴한다`() {
            // given
            every { client.experiment(any(), any(), any()) } throws IllegalArgumentException()

            val defaultVariation = Variation.I

            // when
            val actual = sut.variationDetail(42, User.of("42"), defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo EXCEPTION
                get { variation } isSameInstanceAs defaultVariation
            }
        }
    }

    @Nested
    inner class IsFeatureOnTest {

        @Test
        fun `key, userId`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { client.featureFlag(any(), any()) } returns decision

            // when
            val actual = sut.isFeatureOn(42, "42")

            // then
            expectThat(actual).isTrue()
            verify(exactly = 1) {
                client.featureFlag(42, HackleUser.of("42"))
            }
        }

        @Test
        fun `key, user`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { client.featureFlag(any(), any()) } returns decision
            val user = User.of("42")

            // when
            val actual = sut.isFeatureOn(42, user)

            // then
            expectThat(actual).isTrue()
            verify(exactly = 1) {
                client.featureFlag(42, HackleUser.of(user))
            }
        }
    }

    @Nested
    inner class FeatureFlagDetailTest {

        @Test
        fun `key, userId`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { client.featureFlag(any(), any()) } returns decision

            // when
            val actual = sut.featureFlagDetail(42, "42")

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                client.featureFlag(42, HackleUser.of("42"))
            }
        }


        @Test
        fun `internalClient 의 featureFlag 를 호출하고 리턴받은 값을 그대로 리턴한다`() {
            // given
            val decision = FeatureFlagDecision.on(DEFAULT_RULE)
            every { client.featureFlag(any(), any()) } returns decision
            val user = User.of("42")

            // when
            val actual = sut.featureFlagDetail(42, user)

            // then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                client.featureFlag(42, HackleUser.of(user))
            }
        }

        @Test
        fun `internalClient 에서 예외가 발생하면 off 를 리턴한다`() {
            // given
            every { client.featureFlag(any(), any()) } throws IllegalArgumentException()

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
                client.track(Event.of("key"), HackleUser.of("42"))
            }
        }

        @Test
        fun `key, user`() {
            sut.track("key", User.of("42"))
            verify(exactly = 1) {
                client.track(Event.of("key"), HackleUser.of("42"))
            }
        }

        @Test
        fun `internalClient로 event와 user를 전달한다`() {
            // given
            val event = Event.of("key")
            val user = User.of("test")

            // when
            sut.track(event, user)

            //then
            verify(exactly = 1) {
                client.track(event, HackleUser.of(user))
            }
        }
    }

    @Nested
    inner class Close {

        @Test
        fun `internalClient를 종료한다`() {
            // given
            mockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")

            // when
            sut.close()

            //then
            verify(exactly = 1) {
                client.tryClose()
            }
            unmockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")
        }
    }
}
