package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.evaluator.experiment.*
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.*
import io.hackle.sdk.core.model.Experiment
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

internal class EvaluationFlowFactoryTest {

    val sut = EvaluationFlowFactory(EvaluationContext().also {
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

    @Test
    fun `IN_APP_MESSAGE`() {
        val flow = sut.inAppMessageFlow()
        expectThat(flow)
            .isDecisionWith<PlatformInAppMessageFlowEvaluator>()
            .isDecisionWith<OverrideInAppMessageFlowEvaluator>()
            .isDecisionWith<DraftInAppMessageFlowEvaluator>()
            .isDecisionWith<PauseInAppMessageFlowEvaluator>()
            .isDecisionWith<PeriodInAppMessageFlowEvaluator>()
            .isDecisionWith<HiddenInAppMessageFlowEvaluator>()
            .isDecisionWith<TargetInAppMessageFlowEvaluator>()
            .isEnd()
    }
}
