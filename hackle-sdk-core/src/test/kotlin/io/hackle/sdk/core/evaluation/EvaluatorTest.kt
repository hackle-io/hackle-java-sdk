package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.Bucket
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
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
internal class EvaluatorTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: Evaluator

    @Test
    fun `Override된 사용자인 경우 Override된 Variation으로 결정한다`() {
        // given
        val variation = mockk<Variation> { every { key } returns "H" }
        val experiment = mockk<Experiment> {
            every { getOverriddenVariationOrNull(any()) } returns variation
        }

        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "J")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.OVERRIDDEN, "H")
    }


    @Test
    fun `DRAFT상태인 경우 defaultVariationKey로 결정한다`() {
        // given
        val experiment = mockk<Experiment.Draft> {
            every { getOverriddenVariationOrNull(any()) } returns null
        }

        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "J")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.EXPERIMENT_DRAFT, "J")
    }

    @Test
    fun `실행중이지만 트래픽 할당이 안되어 있으면 defaultVariation으로 결정한다`() {
        // given
        val user = User.of("tid")
        val bucket = mockk<Bucket>()
        val experiment = mockk<Experiment.Running> {
            every { getOverriddenVariationOrNull(any()) } returns null
            every { this@mockk.bucket } returns bucket
        }
        every { bucketer.bucketing(bucket, user) } returns null

        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "I")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.TRAFFIC_NOT_ALLOCATED, "I")
    }

    @Test
    fun `실행중이고 슬롯에 할당되었지만 입력한 Experiment에 해당하는 슬롯이 아니면 defaultVariation으로 결정한다`() {
        // given
        val user = User.of("tid")
        val bucket = mockk<Bucket>()
        val experiment = mockk<Experiment.Running> {
            every { getOverriddenVariationOrNull(any()) } returns null
            every { this@mockk.bucket } returns bucket
        }

        val slot = mockk<Slot> { every { variationId } returns 42 }
        every { bucketer.bucketing(bucket, user) } returns slot

        every { experiment.getVariationOrNull(42) } returns null


        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "D")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.TRAFFIC_NOT_ALLOCATED, "D")
    }

    @Test
    fun `할당된 Variation이 제외된 상태면 defaultVariation으로 결정한다`() {
        // given
        val user = User.of("tid")
        val bucket = mockk<Bucket>()
        val experiment = mockk<Experiment.Running> {
            every { getOverriddenVariationOrNull(any()) } returns null
            every { this@mockk.bucket } returns bucket
        }

        val slot = mockk<Slot> { every { variationId } returns 42 }
        every { bucketer.bucketing(bucket, user) } returns slot

        val variation = mockk<Variation> { every { isDropped } returns true }
        every { experiment.getVariationOrNull(42) } returns variation


        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "E")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.VARIATION_DROPPED, "E")
    }

    @Test
    fun `트래픽 할당되었으면 해당 Variation으로 결정한다`() {
        // given
        val user = User.of("tid")
        val bucket = mockk<Bucket>()
        val experiment = mockk<Experiment.Running> {
            every { getOverriddenVariationOrNull(any()) } returns null
            every { this@mockk.bucket } returns bucket
        }

        val slot = mockk<Slot> { every { variationId } returns 42 }
        every { bucketer.bucketing(bucket, user) } returns slot

        val variation = Variation(42, "G", false)
        every { experiment.getVariationOrNull(42) } returns variation


        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "E")

        // then
        expectThat(actual) isEqualTo Evaluation(42, "G", DecisionReason.TRAFFIC_ALLOCATED)
    }

    @Test
    fun `Experiment가 일시정지 상태면 defautlVariation으로 결정한다`() {
        // given
        val experiment = mockk<Experiment.Paused> {
            every { getOverriddenVariationOrNull(any()) } returns null
        }

        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "J")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.EXPERIMENT_PAUSED, "J")
    }

    @Test
    fun `왼료된 Experiment는 winnerVariation으로 결정한다`() {
        // given
        val experiment = mockk<Experiment.Completed> {
            every { getOverriddenVariationOrNull(any()) } returns null
            every { winnerVariation } returns Variation(42, "H", false)
        }

        // when
        val actual = sut.evaluate(experiment, User.of("tid"), "J")

        // then
        expectThat(actual) isEqualTo Evaluation.of(DecisionReason.EXPERIMENT_COMPLETED, "H")
    }
}
