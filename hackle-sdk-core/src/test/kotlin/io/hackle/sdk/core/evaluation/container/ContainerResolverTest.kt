package io.hackle.sdk.core.evaluation.container

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
internal class ContainerResolverTest{

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: ContainerResolver

    private val user = HackleUser.of("test_id")

    @Test
    fun `bucketing 결과 slot 정보를 가져오지 못한경우 Next Flow 진행시키지 않는다`() {
        val bucket = mockk<Bucket>()
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(any()) } returns bucket
        }
        val experiment = mockk<Experiment> {
            every { id } returns 1
            every { containerId } returns 1
            every { identifierType } returns IdentifierType.ID.key
        }
        every { bucketer.bucketing(bucket, any()) } returns null

        val actual = sut.isUserInContainerGroup(container, bucket, experiment, user)

        expectThat(actual).isFalse()
    }

    @Test
    fun `bucketing 결과에 해당하는 container group 정보를 못찾는 경우 Next Flow 진행시키지 않는다`() {
        val slot = Slot(0, 100, 320)
        val bucket = mockk<Bucket>()
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
            every { getGroupOrNull(any()) } returns null
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(any()) } returns bucket
        }
        val experiment = mockk<Experiment> {
            every { id } returns 1
            every { containerId } returns 1
            every { identifierType } returns IdentifierType.ID.key
        }
        every { bucketer.bucketing(bucket, any()) } returns slot

        val actual = sut.isUserInContainerGroup(container, bucket, experiment, user)

        expectThat(actual).isFalse()
    }

    @Test
    fun `실험이 bucketing 결과에 해당하지 않으면 Next Flow를 진행시키지 않는다`() {
        val experimentId = 22L
        val slot = Slot(0, 100, 22)
        val bucket = mockk<Bucket>()
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
            every { getGroupOrNull(any()) } returns ContainerGroup(22, listOf(23L))
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(any()) } returns bucket
        }
        val experiment = mockk<Experiment> {
            every { id } returns 1
            every { containerId } returns 1
            every { identifierType } returns IdentifierType.ID.key
        }
        every { bucketer.bucketing(bucket, any()) } returns slot

        val actual = sut.isUserInContainerGroup(container, bucket, experiment, user)

        expectThat(actual).isFalse()
    }

    @Test
    fun `실험이 bucketing 결과에 해당하면 Next Flow를 진행한다 `() {
        val experimentId = 22L
        val slot = Slot(0, 100, 22)
        val bucket = mockk<Bucket>()
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
            every { getGroupOrNull(slot.variationId) } returns ContainerGroup(22, listOf(experimentId))
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(any()) } returns bucket
        }
        val experiment = mockk<Experiment> {
            every { id } returns experimentId
            every { containerId } returns 1
            every { identifierType } returns IdentifierType.ID.key
        }
        every { bucketer.bucketing(bucket, any()) } returns slot

        val actual = sut.isUserInContainerGroup(container, bucket, experiment, user)

        expectThat(actual).isTrue()
    }
}