package io.hackle.sdk.internal.client

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.User
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.EXCEPTION
import io.hackle.sdk.common.decision.DecisionReason.TRAFFIC_ALLOCATED
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.core.client.HackleInternalClient
import io.hackle.sdk.core.internal.utils.tryClose
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class HackleClientImplTest {


    @RelaxedMockK
    private lateinit var client: HackleInternalClient

    @InjectMockKs
    private lateinit var sut: HackleClientImpl

    @Nested
    inner class Variations {

        @Test
        fun `Control Variation을 전달한다`() {
            // given
            val spy = spyk(sut)

            val decision = mockk<Decision>()
            every { spy.variationDetail(any(), any(), any()) } returns decision

            val user = User.of("test_user_id")

            // when
            val actual = spy.variationDetail(320L, user)

            //then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                spy.variationDetail(320, user, Variation.CONTROL)
            }
        }

        @Test
        fun `internalClient 한테 입력을 그대로 전달하고 리턴받은 값을 바로 리턴한다`() {
            // given
            val experimentKey = 320L
            val user = mockk<User>()
            val defaultVariation = mockk<Variation>()
            val decision = mockk<Decision>()

            every { client.experiment(experimentKey, user, defaultVariation) } returns decision

            // when
            val actual = sut.variationDetail(experimentKey, user, defaultVariation)

            //then
            expectThat(actual) isSameInstanceAs decision
            verify(exactly = 1) {
                client.experiment(
                    experimentKey = withArg { expectThat(it) isEqualTo 320L },
                    user = withArg { expectThat(it) isSameInstanceAs user },
                    defaultVariation = withArg { expectThat(it) isSameInstanceAs defaultVariation }
                )
            }
        }

        @Test
        fun `internalClient에서 예외가 발생하면 defaultVariation을 리턴한다`() {
            // given
            every { client.experiment(any(), any(), any()) } throws IllegalArgumentException()

            val defaultVariation = Variation.I

            // when
            val actual = sut.variationDetail(320L, User.of("test_user_id"), defaultVariation)

            //then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.EXCEPTION
                get { variation } isSameInstanceAs defaultVariation
            }
        }
    }

    @Nested
    inner class FeatureFlag {

        @Test
        fun `feature flag on`() {
            // given
            every { client.featureFlag(42, User.of("test_id")) } returns FeatureFlagDecision.on(TRAFFIC_ALLOCATED)

            // when
            val actual = sut.isFeatureOn(42, "test_id")

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `예외가 발생하면 feature off`() {
            // given
            every { client.featureFlag(any(), any()) } answers { throw IllegalArgumentException("Fail") }

            // when
            val actual = sut.featureFlagDetail(42, "abc")

            // then
            expectThat(actual) {
                get { isOn }.isFalse()
                get { reason } isEqualTo EXCEPTION
            }
        }

    }

    @Nested
    inner class Track {

        @Test
        fun `eventKey로 Event를 생성해서 전달한다`() {
            // given
            val spy = spyk(sut)

            val user = User.of("test_user_id")

            // when
            spy.track("test_event_key", user)

            //then
            verify(exactly = 1) {
                spy.track(
                    event = withArg { expectThat(it).get { key } isEqualTo "test_event_key" },
                    user = user
                )
            }
        }

        @Test
        fun `internalClient로 event와 user를 전달한다`() {
            // given
            val event = mockk<Event>()
            val user = mockk<User>()

            // when
            sut.track(event, user)

            //then
            verify(exactly = 1) {
                client.track(
                    event = withArg { expectThat(it) isSameInstanceAs event },
                    user = withArg { expectThat(it) isSameInstanceAs user }
                )
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
