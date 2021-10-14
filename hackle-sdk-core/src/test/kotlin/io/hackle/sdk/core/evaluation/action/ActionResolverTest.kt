package io.hackle.sdk.core.evaluation.action

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
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
internal class ActionResolverTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: ActionResolver

    @Nested
    inner class VariationActionTest {
        @Test
        fun `VariationAction인 경우 variationId에 해당하는 Variation을 가져온다`() {
            // given
            val action = Action.Variation(420)

            val variation = mockk<Variation>()
            val experiment = mockk<Experiment> {
                every { getVariationOrNull(420) } returns variation
            }

            // when
            val actual = sut.resolveOrNull(action, mockk(), experiment, mockk())

            // then
            expectThat(actual)
                .isNotNull()
                .isSameInstanceAs(variation)
        }

        @Test
        fun `Variation을 찾을 수 없으면 예외 발생`() {
            // given
            val action = Action.Variation(420)

            val variation = mockk<Variation>()
            val experiment = mockk<Experiment> {
                every { getVariationOrNull(420) } returns null
            }

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.resolveOrNull(action, mockk(), experiment, mockk())
            }

            // then
            expectThat(exception) {
                get { message }
                    .isNotNull()
                    .isEqualTo("variation[420]")
            }
        }
    }

    @Nested
    inner class BucketActionTest {

        @Test
        fun `bucketId에 해당하는 Bucket을 찾을 수 없으면 예외 발생`() {
            // given
            val action = Action.Bucket(42)
            val workspace = mockk<Workspace> {
                every { getBucketOrNull(any()) } returns null
            }

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.resolveOrNull(action, workspace, mockk(), HackleUser.of("test_id"))
            }

            // then
            expectThat(exception.message)
                .isNotNull()
                .isEqualTo("bucket[42]")
        }

        @Test
        fun `슬롯에 할당 안된 사용자는 null을 리턴한다`() {
            // given
            val user = HackleUser.of("test_id")
            val action = Action.Bucket(42)
            val bucket = mockk<Bucket>()
            val workspace = mockk<Workspace> {
                every { getBucketOrNull(any()) } returns bucket
            }
            every { bucketer.bucketing(bucket, user) } returns null

            // when
            val actual = sut.resolveOrNull(action, workspace, mockk(), user)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `슬롯에 할당되었지만 슬롯의 variationId에 해당하는 Variation이 Experiment에 없으면 null리턴`() {
            // given
            val user = HackleUser.of("test_id")
            val action = Action.Bucket(42)
            val bucket = mockk<Bucket>()
            val workspace = mockk<Workspace> {
                every { getBucketOrNull(any()) } returns bucket
            }
            val slot = Slot(0, 100, 320)
            every { bucketer.bucketing(bucket, user) } returns slot
            val experiment = mockk<Experiment> {
                every { getVariationOrNull(320) } returns null
            }

            // when
            val actual = sut.resolveOrNull(action, workspace, experiment, user)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `버켓팅을 통해 할당된 Variaiton리턴`() {
            // given
            val user = HackleUser.of("test_id")
            val action = Action.Bucket(42)
            val bucket = mockk<Bucket>()
            val workspace = mockk<Workspace> {
                every { getBucketOrNull(any()) } returns bucket
            }
            val slot = Slot(0, 100, 320)
            every { bucketer.bucketing(bucket, user) } returns slot
            val experiment = mockk<Experiment> {
                every { getVariationOrNull(320) } returns Variation(320, "C", false)
            }

            // when
            val actual = sut.resolveOrNull(action, workspace, experiment, user)

            // then
            expectThat(actual)
                .isNotNull()
                .isEqualTo(Variation(320, "C", false))
        }
    }
}
