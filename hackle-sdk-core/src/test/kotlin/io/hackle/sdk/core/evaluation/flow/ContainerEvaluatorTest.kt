package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Container
import io.hackle.sdk.core.model.VariationKey
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class ContainerEvaluatorTest : FlowEvaluatorTest() {

    @MockK
    private lateinit var containerResolver: ContainerResolver

    @InjectMockKs
    private lateinit var sut: ContainerEvaluator

    private val user = HackleUser.of("test_id")
    private val defaultVariationKey = VariationKey.A.name

    @Test
    fun `실험이 상호배타에 속하지 않은 실험은 Next Flow로 진행한다`() {
        val workspace = workspace {}
        val experiment = experiment()
        val request = experimentRequest(workspace, user, experiment)

        val actual = sut.evaluate(request, context, nextFlow)

        expectThat(actual) isSameInstanceAs evaluation
        verify {
            nextFlow.evaluate(any(), any())
        }
    }

    @Test
    fun `실험이 상호배타에 속해있지만 container 정보를 찾을 수 없을때 Exception 발생`() {
        val workspace = workspace {}
        val experiment = experiment(containerId = 42L)
        val request = experimentRequest(workspace, user, experiment)

        val actual = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, nextFlow)
        }

        expectThat(actual.message) isEqualTo "Container[42]"
    }

    @Test
    fun `실험이 상호배타에 속해있고 상호배타 그룹에 해당하면 Next Flow 진행 `() {
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
        }
        val experiment = experiment(containerId = 1L)
        val request = experimentRequest(workspace, user, experiment)

        every { containerResolver.isUserInContainerGroup(any(), any()) } returns true

        val actual = sut.evaluate(request, context, nextFlow)

        expectThat(actual) isSameInstanceAs evaluation
        verify {
            nextFlow.evaluate(any(), any())
        }
    }

    @Test
    fun `실험이 상호배타에 속해있지만 상호배타 그룹에 해당하지 않으면 defaultVariation 결과를 리턴`() {
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
        }
        val experiment = experiment(containerId = 1L)
        val request = experimentRequest(workspace, user, experiment)

        every { containerResolver.isUserInContainerGroup(any(), any()) } returns false

        val actual = sut.evaluate(request, context, nextFlow)

        expectThat(actual.reason) isEqualTo DecisionReason.NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT
        expectThat(actual.variationKey) isEqualTo "A"
    }
}