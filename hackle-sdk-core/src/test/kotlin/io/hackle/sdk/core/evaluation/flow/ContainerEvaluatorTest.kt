package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.model.Bucket
import io.hackle.sdk.core.model.Container
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.VariationKey
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
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class ContainerEvaluatorTest {

    @MockK
    private lateinit var containerResolver: ContainerResolver
    @InjectMockKs
    private lateinit var sut: ContainerEvaluator

    private val user = HackleUser.of("test_id")
    private val defaultVariationKey = VariationKey.A.name

    @Test
    fun `실험이 상호배타에 속하지 않은 실험은 Next Flow로 진행한다`() {
        val workspace = workspace {}
        val experiment = mockk<Experiment> {
            every { containerId } returns null
        }
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        val actual = sut.evaluate(workspace, experiment, user, defaultVariationKey, nextFlow)

        expectThat(actual) isSameInstanceAs evaluation
        verify {
            nextFlow.evaluate(any(), experiment, user, defaultVariationKey)
        }
    }

    @Test
    fun `실험이 상호배타에 속해있지만 container 정보를 찾을 수 없을때 Exception 발생`() {
        val workspace = workspace {}
        val experiment = mockk<Experiment> {
            every { containerId } returns 1
        }
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        val actual = assertThrows<IllegalArgumentException> {
            sut.evaluate(workspace, experiment, user, defaultVariationKey, nextFlow)
        }

        expectThat(actual.message).isNotNull().startsWith("container group not exist.")
    }

    @Test
    fun `실험이 상호배타에 속해있지만 container Bucket 정보를 찾을 수 없을때 Exception 발생`() {
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(1) } answers { null }
        }
        val experiment = mockk<Experiment> {
            every { containerId } returns 1
        }
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        val actual = assertThrows<IllegalArgumentException> {
            sut.evaluate(workspace, experiment, user, defaultVariationKey, nextFlow)
        }

        expectThat(actual.message).isNotNull().startsWith("container group bucket not exist.")
    }

    @Test
    fun `실험이 상호배타에 속해있고 상호배타 그룹에 해당하면 Next Flow 진행 `() {
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val bucket = mockk<Bucket>()
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(1) } answers { bucket }
        }
        val experiment = mockk<Experiment> {
            every { containerId } returns 1
        }
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        every { containerResolver.isUserInContainerGroup(container, bucket, experiment, user) }.returns(true)

        val actual = sut.evaluate(workspace, experiment, user, defaultVariationKey, nextFlow)

        expectThat(actual) isSameInstanceAs evaluation
        verify {
            nextFlow.evaluate(any(), experiment, user, defaultVariationKey)
        }
    }

    @Test
    fun `실험이 상호배타에 속해있지만 상호배타 그룹에 해당하지 않으면 defaultVariation 결과를 리턴`() {
        val container = mockk<Container> {
            every { id } returns 1
            every { bucketId } returns 1
        }
        val bucket = mockk<Bucket>()
        val workspace = mockk<Workspace> {
            every { getContainerOrNull(1) } returns container
            every { getBucketOrNull(1) } answers { bucket }
        }
        val experiment = mockk<Experiment> {
            every { containerId } returns 1
        }
        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        every { experiment.getVariationOrNull(defaultVariationKey) } answers  { null }
        every { containerResolver.isUserInContainerGroup(container, bucket, experiment, user) }.returns(false)

        val actual = sut.evaluate(workspace, experiment, user, defaultVariationKey, nextFlow)

        expectThat(actual.variationKey) isEqualTo defaultVariationKey
        expectThat(actual.reason) isEqualTo DecisionReason.NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT
    }

}