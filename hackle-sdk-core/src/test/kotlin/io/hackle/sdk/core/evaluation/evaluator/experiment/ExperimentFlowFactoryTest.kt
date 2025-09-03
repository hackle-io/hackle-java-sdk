package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.flow.isDecisionWith
import io.hackle.sdk.core.evaluation.flow.isEnd
import io.hackle.sdk.core.model.Experiment
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

internal class ExperimentFlowFactoryTest {

    val sut = ExperimentFlowFactory(EvaluationContext().also {
        it.initialize(mockk(), mockk(), mockk())
    })

    @Test
    fun `AB_TEST`() {
        val flow = sut.experimentFlow(Experiment.Type.AB_TEST)
        expectThat(flow)
            .isDecisionWith<OverrideEvaluator>()
            .isDecisionWith<IdentifierEvaluator>()
            .isDecisionWith<ContainerEvaluator>()
            .isDecisionWith<ExperimentTargetEvaluator>()
            .isDecisionWith<DraftExperimentEvaluator>()
            .isDecisionWith<PausedExperimentEvaluator>()
            .isDecisionWith<CompletedExperimentEvaluator>()
            .isDecisionWith<TrafficAllocateEvaluator>()
            .isEnd()
    }

    @Test
    fun `FEATURE_FLAG`() {
        val flow = sut.experimentFlow(Experiment.Type.FEATURE_FLAG)
        expectThat(flow)
            .isDecisionWith<DraftExperimentEvaluator>()
            .isDecisionWith<PausedExperimentEvaluator>()
            .isDecisionWith<CompletedExperimentEvaluator>()
            .isDecisionWith<OverrideEvaluator>()
            .isDecisionWith<IdentifierEvaluator>()
            .isDecisionWith<TargetRuleEvaluator>()
            .isDecisionWith<DefaultRuleEvaluator>()
            .isEnd()
    }
}
