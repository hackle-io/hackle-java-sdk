package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.bucket
import io.hackle.sdk.core.support.slot
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class ExperimentActionResolverTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: ExperimentActionResolver

    @Nested
    inner class VariationActionTest {

        @Test
        fun `VariationAction인 경우 variationId에 해당하는 Variation을 가져온다`() {
            // given
            val action = Action.Variation(420)

            val variation = Experiments.variation(id = 420, key = "B")
            val experiment = Experiments.config(variations = listOf(variation))
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val actual = sut.resolveOrNull(request, action)

            // then
            expectThat(actual)
                .isNotNull()
                .isSameInstanceAs(variation)
        }

        @Test
        fun `Variation을 찾을 수 없으면 예외 발생`() {
            // given
            val action = Action.Variation(420)
            val experiment = Experiments.config(
                variations = listOf(
                    Experiments.variation(id = 1, key = "A"),
                    Experiments.variation(id = 2, key = "B"),
                )
            )
            val request = Experiments.localRequest(experiment = experiment)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.resolveOrNull(request, action)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .isEqualTo("variation[420]")
        }
    }

    @Nested
    inner class BucketActionTest {

        @Test
        fun `bucketId에 해당하는 Bucket을 찾을 수 없으면 예외 발생`() {
            // given
            val action = Action.Bucket(42)
            val request = Experiments.localRequest()

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.resolveOrNull(request, action)
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .isEqualTo("bucket[42]")
        }

        @Test
        fun `Experiment identifierType에 해당하는 식별자가 없으면 null을 리턴한다`() {
            // given
            val action = Action.Bucket(42)
            val experiment = Experiments.config(identifierType = "custom_id")
            val request = Experiments.localRequest(
                workspace = Workspaces.config(buckets = listOf(bucket(id = 42))),
                experiment = experiment
            )

            // when
            val actual = sut.resolveOrNull(request, action)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `슬롯에 할당 안된 사용자는 null을 리턴한다`() {
            // given
            val action = Action.Bucket(42)
            val request = Experiments.localRequest(
                workspace = Workspaces.config(buckets = listOf(bucket(id = 42)))
            )
            every { bucketer.bucketing(any(), any()) } returns null

            // when
            val actual = sut.resolveOrNull(request, action)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `슬롯에 할당되었지만 슬롯의 variationId에 해당하는 Variation이 Experiment에 없으면 null리턴`() {
            // given
            val action = Action.Bucket(42)
            val experiment = Experiments.config(
                variations = listOf(
                    Experiments.variation(id = 1, key = "A"),
                    Experiments.variation(id = 2, key = "B"),
                )
            )
            val request = Experiments.localRequest(
                workspace = Workspaces.config(buckets = listOf(bucket(id = 42))),
                experiment = experiment
            )
            every { bucketer.bucketing(any(), any()) } returns slot(0, 100, 320)

            // when
            val actual = sut.resolveOrNull(request, action)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `버켓팅을 통해 할당된 Variation을 리턴한다`() {
            // given
            val action = Action.Bucket(42)
            val variation = Experiments.variation(id = 320, key = "C")
            val experiment = Experiments.config(variations = listOf(variation))
            val request = Experiments.localRequest(
                workspace = Workspaces.config(buckets = listOf(bucket(id = 42))),
                experiment = experiment
            )
            every { bucketer.bucketing(any(), any()) } returns slot(0, 100, 320)

            // when
            val actual = sut.resolveOrNull(request, action)

            // then
            expectThat(actual)
                .isNotNull()
                .isSameInstanceAs(variation)
        }
    }
}
