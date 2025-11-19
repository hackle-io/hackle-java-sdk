package io.hackle.sdk.core.evaluation.container

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
internal class ContainerResolverTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: ContainerResolver

    private val user = HackleUser.of("test_id")

    @Test
    fun `식별자가 없으면 false`() {
        val experiment = mockk<Experiment> {
            every { id } returns 1
            every { containerId } returns 1
            every { identifierType } returns "customId"
        }

        val request = experimentRequest(experiment = experiment, user = user)

        val actual = sut.isUserInContainerGroup(request, mockk())

        expectThat(actual).isFalse()
    }

    @Test
    fun `Bucket 없으면 에러`() {
        // given
        val workspace = mockk<Workspace> {
            every { getBucketOrNull(any()) } returns null
        }
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val request = experimentRequest(workspace = workspace, user = user)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.isUserInContainerGroup(request, container)
        }

        // then
        expectThat(exception.message) isEqualTo "Bucket[1]"
    }

    @Test
    fun `bucketing 결과 slot 정보를 가져오지 못한경우 Next Flow 진행시키지 않는다`() {
        val bucket = mockk<Bucket>()
        val workspace = mockk<Workspace> {
            every { getBucketOrNull(any()) } returns bucket
        }
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        every { bucketer.bucketing(bucket, any()) } returns null
        val experiment = experiment(containerId = 1)
        val request = experimentRequest(workspace, user, experiment)

        val actual = sut.isUserInContainerGroup(request, container)

        expectThat(actual).isFalse()
    }

    @Test
    fun `bucketing 결과에 해당하는 container group 정보를 못찾는 경우 Next Flow 진행시키지 않는다`() {
        val slot = Slot(0, 100, 320)
        val bucket = mockk<Bucket>()
        val workspace = mockk<Workspace> {
            every { getBucketOrNull(any()) } returns bucket
        }
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
            every { getGroupOrNull(any()) } returns null
        }
        val experiment = experiment(containerId = 1L)
        every { bucketer.bucketing(bucket, any()) } returns slot

        val request = experimentRequest(workspace, user, experiment)

        val actual = assertThrows<IllegalArgumentException> {
            sut.isUserInContainerGroup(request, container)
        }

        expectThat(actual.message).isNotNull().startsWith("ContainerGroup[320]")
    }

    @Test
    fun `실험이 bucketing 결과에 해당하지 않으면 Next Flow를 진행시키지 않는다`() {
        val slot = Slot(0, 100, 22)
        val bucket = mockk<Bucket>()
        val workspace = mockk<Workspace> {
            every { getBucketOrNull(any()) } returns bucket
        }
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
            every { getGroupOrNull(any()) } returns ContainerGroup(22, listOf(23L))
        }
        val experiment = experiment(id = 99, containerId = 1)
        every { bucketer.bucketing(bucket, any()) } returns slot

        val request = experimentRequest(workspace, user, experiment)

        val actual = sut.isUserInContainerGroup(request, container)

        expectThat(actual).isFalse()
    }

    @Test
    fun `실험이 bucketing 결과에 해당하면 Next Flow를 진행한다`() {
        val experimentId = 22L
        val slot = Slot(0, 100, 22)
        val bucket = mockk<Bucket>()
        val workspace = mockk<Workspace> {
            every { getBucketOrNull(any()) } returns bucket
        }
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
            every { getGroupOrNull(slot.variationId) } returns ContainerGroup(22, listOf(experimentId))
        }
        val experiment = experiment(id = experimentId, containerId = 1)
        every { bucketer.bucketing(bucket, any()) } returns slot

        val request = experimentRequest(workspace, user, experiment)

        val actual = sut.isUserInContainerGroup(request, container)

        expectThat(actual).isTrue()
    }
}