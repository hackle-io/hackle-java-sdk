package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.bucket
import io.hackle.sdk.core.support.container
import io.hackle.sdk.core.support.containerGroup
import io.hackle.sdk.core.support.slot
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class ExperimentContainerResolverTest {

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: ExperimentContainerResolver

    @Test
    fun `식별자가 없으면 false`() {
        // given
        val experiment = Experiments.config(identifierType = "customId", containerId = 1)
        val request = Experiments.localRequest(experiment = experiment)

        // when
        val actual = sut.isUserInContainerGroup(request, container(id = 1, bucketId = 1))

        // then
        expectThat(actual).isFalse()
    }

    @Test
    fun `Bucket 없으면 에러`() {
        // given
        val request = Experiments.localRequest()

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.isUserInContainerGroup(request, container(id = 1, bucketId = 1))
        }

        // then
        expectThat(exception.message) isEqualTo "Bucket[1]"
    }

    @Test
    fun `bucketing 결과 slot 정보를 가져오지 못한경우 false`() {
        // given
        val experiment = Experiments.config(containerId = 1)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(buckets = listOf(bucket(id = 1))),
            experiment = experiment
        )
        every { bucketer.bucketing(any(), any()) } returns null

        // when
        val actual = sut.isUserInContainerGroup(request, container(id = 1, bucketId = 1))

        // then
        expectThat(actual).isFalse()
    }

    @Test
    fun `bucketing 결과에 해당하는 container group 정보를 못찾는 경우 예외 발생`() {
        // given
        val experiment = Experiments.config(containerId = 1)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(buckets = listOf(bucket(id = 1))),
            experiment = experiment
        )
        every { bucketer.bucketing(any(), any()) } returns slot(0, 100, 320)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.isUserInContainerGroup(request, container(id = 1, bucketId = 1, groups = emptyList()))
        }

        // then
        expectThat(exception.message).isNotNull().startsWith("ContainerGroup[320]")
    }

    @Test
    fun `실험이 container group 에 속해있지 않으면 false`() {
        // given
        val experiment = Experiments.config(id = 99, containerId = 1)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(buckets = listOf(bucket(id = 1))),
            experiment = experiment
        )
        val container = container(
            id = 1,
            bucketId = 1,
            groups = listOf(containerGroup(id = 22, experiments = listOf(23)))
        )
        every { bucketer.bucketing(any(), any()) } returns slot(0, 100, 22)

        // when
        val actual = sut.isUserInContainerGroup(request, container)

        // then
        expectThat(actual).isFalse()
    }

    @Test
    fun `실험이 container group 에 속해있으면 true`() {
        // given
        val experiment = Experiments.config(id = 22, containerId = 1)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(buckets = listOf(bucket(id = 1))),
            experiment = experiment
        )
        val container = container(
            id = 1,
            bucketId = 1,
            groups = listOf(containerGroup(id = 22, experiments = listOf(22)))
        )
        every { bucketer.bucketing(any(), any()) } returns slot(0, 100, 22)

        // when
        val actual = sut.isUserInContainerGroup(request, container)

        // then
        expectThat(actual).isTrue()
    }
}
