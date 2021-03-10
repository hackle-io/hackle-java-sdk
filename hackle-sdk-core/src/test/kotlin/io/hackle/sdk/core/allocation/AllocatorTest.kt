package io.hackle.sdk.core.allocation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.*
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
internal class AllocatorTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: Allocator


    @Test
    fun `완료된 실험인 경우 Winning Variation으로 강제 할당한다`() {
        // given
        val experiment = mockk<Experiment.Completed> {
            every { winnerVariationKey } returns "H"
        }

        // when
        val actual = sut.allocate(experiment, User.of("test_user_id"))

        // then
        expectThat(actual)
            .isA<Allocation.ForcedAllocated>()
            .and {
                get { decisionReason } isEqualTo EXPERIMENT_COMPLETED
                get { variationKey } isEqualTo "H"
            }
    }

    @Test
    fun `Override된 사용자인 경우 override된 variation으로 강제 할당한다`() {
        // given
        val overriddenVariation = mockk<Variation> { every { key } returns "H" }
        val experiment = mockk<Experiment.Running> {
            every { getOverriddenVariationOrNull(any()) } returns overriddenVariation
        }

        // when
        val actual = sut.allocate(experiment, User.of("test_user_id"))

        // then
        expectThat(actual)
            .isA<Allocation.ForcedAllocated>()
            .and {
                get { decisionReason } isEqualTo OVERRIDDEN
                get { variationKey } isEqualTo "H"
            }
    }

    @Test
    fun `버켓팅 결과 트래픽에 포함되지 않은 사용자는 할당되지 않는다`() {
        // given
        val user = User.of("test_user_id")
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(user) } returns null
        }
        every { bucketer.bucketing(any(), user) } returns null

        // when
        val actual = sut.allocate(experiment, user)

        // then
        expectThat(actual)
            .isA<Allocation.NotAllocated>()
            .get { decisionReason } isEqualTo TRAFFIC_NOT_ALLOCATED
    }

    @Test
    fun `버케팅 결과 다른 실험의 슬롯에 포함된 경우 할당되지 않는다`() {
        // given
        val user = User.of("test_user_id")
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(user) } returns null
        }

        val slot = Slot(0, 100, 320)
        every { bucketer.bucketing(any(), user) } returns slot
        every { experiment.getVariationOrNull(slot.variationId) } returns null

        // when
        val actual = sut.allocate(experiment, user)

        // then
        expectThat(actual)
            .isA<Allocation.NotAllocated>()
            .get { decisionReason } isEqualTo TRAFFIC_NOT_ALLOCATED
    }

    @Test
    fun `드랍된 Variation인 걍우 힐당되지 않는다`() {
        // given
        val user = User.of("test_user_id")
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(user) } returns null
        }

        val slot = Slot(0, 100, 320)
        every { bucketer.bucketing(any(), user) } returns slot

        val variation = Variation(42, "C", true)
        every { experiment.getVariationOrNull(slot.variationId) } returns variation

        // when
        val actual = sut.allocate(experiment, user)

        // then
        expectThat(actual)
            .isA<Allocation.NotAllocated>()
            .get { decisionReason } isEqualTo VARIATION_DROPPED
    }

    @Test
    fun `버켓팅결과 특정 Variation에 포함되면 해당 variation으로 할당된다`() {
        // given
        val user = User.of("test_user_id")
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { getOverriddenVariationOrNull(user) } returns null
        }

        val slot = Slot(0, 100, 320)
        every { bucketer.bucketing(any(), user) } returns slot

        val variation = Variation(42, "C", false)
        every { experiment.getVariationOrNull(slot.variationId) } returns variation

        // when
        val actual = sut.allocate(experiment, user)

        // then
        expectThat(actual)
            .isA<Allocation.Allocated>()
            .and {
                get { decisionReason } isEqualTo TRAFFIC_ALLOCATED
                get { this.variation } isSameInstanceAs variation
            }
    }
}
