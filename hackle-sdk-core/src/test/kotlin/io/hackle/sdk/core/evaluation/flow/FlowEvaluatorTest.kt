package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach

internal abstract class FlowEvaluatorTest {

    protected lateinit var nextFlow: EvaluationFlow
    protected lateinit var evaluation: ExperimentEvaluation
    protected lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        evaluation = mockk()
        nextFlow = mockk {
            every { evaluate(any(), any()) } returns evaluation
        }
        context = Evaluators.context()
    }
}