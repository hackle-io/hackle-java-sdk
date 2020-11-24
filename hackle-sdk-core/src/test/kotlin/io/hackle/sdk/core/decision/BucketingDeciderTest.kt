package io.hackle.sdk.core.decision

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Slot
import io.hackle.sdk.core.model.Variation
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class BucketingDeciderTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: BucketingDecider

    @Test
    fun `완료된 실험인 경우 Winning Variation으로 결정한다`() {
        // given
        val experiment = mockk<Experiment.Completed> {
            every { winnerVariationKey } returns "H"
        }

        // when
        val actual = sut.decide(experiment, User.of("test_user_id"))

        //then
        expectThat(actual)
            .isA<Decision.ForcedAllocated>()
            .get { variationKey } isEqualTo "H"
    }

    @Test
    fun `Override된 사용자인 경우 override된 variation으로 결정한다`() {
        // given
        val experiment = mockk<Experiment.Running> {
            every { getOverriddenVariationOrNull(any()) } returns mockk {
                every { key } returns "H"
            }
        }

        // when
        val actual = sut.decide(experiment, User.of("test_user_id"))

        //then
        expectThat(actual)
            .isA<Decision.ForcedAllocated>()
            .get { variationKey } isEqualTo "H"
    }

    @Test
    fun `실험에 할당되지 않은 사용자는 NotAllocated를 리턴한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(any()) } returns null
        }
        every { bucketer.bucketing(any(), any()) } returns null

        // when
        val actual = sut.decide(experiment, User.of("test_user_id"))

        //then
        expectThat(actual)
            .isA<Decision.NotAllocated>()
    }

    @Test
    fun `다른 실험의 슬롯에 할당된 경우 NotAllocated를 리턴한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(any()) } returns null
        }

        val slot = mockk<Slot>(relaxed = true)
        every { bucketer.bucketing(any(), any()) } returns slot
        every { experiment.getVariationOrNull(slot.variationId) } returns null

        // when
        val actual = sut.decide(experiment, User.of("test_user_id"))

        //then
        expectThat(actual)
            .isA<Decision.NotAllocated>()
    }

    @Test
    fun `드랍된 Variation인 경우 NotAllocated를 리턴한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(any()) } returns null
        }

        val slot = mockk<Slot>(relaxed = true)
        every { bucketer.bucketing(any(), any()) } returns slot

        val variation = mockk<Variation> { every { isDropped } returns true }
        every { experiment.getVariationOrNull(slot.variationId) } returns variation

        // when
        val actual = sut.decide(experiment, User.of("test_user_id"))

        //then
        expectThat(actual)
            .isA<Decision.NotAllocated>()
    }

    @Test
    fun `Bucketing으로 할당된 사용자는 NatualAllocated를 리턴한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(any()) } returns null
        }

        val slot = mockk<Slot>(relaxed = true)
        every { bucketer.bucketing(any(), any()) } returns slot

        val variation = mockk<Variation> {
            every { isDropped } returns false
            every { key } returns "I"
        }
        every { experiment.getVariationOrNull(slot.variationId) } returns variation

        // when
        val actual = sut.decide(experiment, User.of("test_user_id"))

        //then
        expectThat(actual)
            .isA<Decision.NaturalAllocated>()
            .get { variation } isSameInstanceAs variation
    }
}
