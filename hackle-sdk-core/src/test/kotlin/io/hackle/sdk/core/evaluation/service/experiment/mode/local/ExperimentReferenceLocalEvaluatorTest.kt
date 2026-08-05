package io.hackle.sdk.core.evaluation.service.experiment.mode.local

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.EvaluatorFactory
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluator
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class ExperimentReferenceLocalEvaluatorTest {

    @MockK
    private lateinit var evaluatorFactory: EvaluatorFactory

    @InjectMockKs
    private lateinit var sut: ExperimentReferenceLocalEvaluator

    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        context = Evaluators.context()
    }

    @Test
    fun `이미 평가된 경우 다시 평가하지 않고 평가된 결과를 리턴한다`() {
        // given
        val experiment = Experiments.config()
        val evaluation = Experiments.evaluation(entity = experiment)
        context.add(evaluation)

        // when
        val actual = sut.evaluate(InAppMessages.layoutLocalRequest(), context, experiment)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify { evaluatorFactory wasNot Called }
    }

    @Test
    fun `reference 를 평가하고 평가 결과를 context 에 추가한다`() {
        // given
        val experiment = Experiments.config()
        val evaluation = Experiments.evaluation(entity = experiment)
        val response = Experiments.response(evaluation = evaluation)

        val requestSlot = slot<ExperimentEvaluateRequest>()
        val experimentEvaluator = mockk<ExperimentEvaluator<ExperimentEvaluateRequest>> {
            every { evaluate(any(), any()) } returns response
        }
        every { evaluatorFactory.experiment(capture(requestSlot)) } returns experimentEvaluator

        val parentRequest = InAppMessages.layoutLocalRequest()

        // when
        val actual = sut.evaluate(parentRequest, context, experiment)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        expectThat(context[experiment]) isSameInstanceAs evaluation
        expectThat(requestSlot.captured) {
            get { entity } isSameInstanceAs experiment
            get { workspace } isSameInstanceAs parentRequest.workspace
            get { user } isSameInstanceAs parentRequest.user
        }
    }
}
