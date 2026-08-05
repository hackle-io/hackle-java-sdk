package io.hackle.sdk.core.evaluation.service.experiment.flow

import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.support.isEnd
import io.hackle.sdk.core.support.isStepWith
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

internal class ExperimentLocalEvaluationFlowFactoryTest {

    private val sut = ExperimentLocalEvaluationFlowFactory(
        targetMatcher = mockk(),
        bucketer = mockk(),
        overrideStorage = mockk(),
    )

    @Test
    fun `AB_TEST`() {
        val flow = sut.flow(Experiment.Type.AB_TEST)
        expectThat(flow)
            .isStepWith<OverrideExperimentLocalFlowEvaluator>()
            .isStepWith<IdentifierExperimentLocalFlowEvaluator>()
            .isStepWith<ContainerExperimentLocalFlowEvaluator>()
            .isStepWith<TargetExperimentLocalFlowEvaluator>()
            .isStepWith<DraftExperimentLocalFlowEvaluator>()
            .isStepWith<PausedExperimentLocalFlowEvaluator>()
            .isStepWith<CompletedExperimentLocalFlowEvaluator>()
            .isStepWith<TrafficAllocateExperimentLocalFlowEvaluator>()
            .isEnd()
    }

    @Test
    fun `FEATURE_FLAG`() {
        val flow = sut.flow(Experiment.Type.FEATURE_FLAG)
        expectThat(flow)
            .isStepWith<DraftExperimentLocalFlowEvaluator>()
            .isStepWith<PausedExperimentLocalFlowEvaluator>()
            .isStepWith<CompletedExperimentLocalFlowEvaluator>()
            .isStepWith<OverrideExperimentLocalFlowEvaluator>()
            .isStepWith<IdentifierExperimentLocalFlowEvaluator>()
            .isStepWith<TargetRuleExperimentLocalFlowEvaluator>()
            .isStepWith<DefaultRuleExperimentLocalFlowEvaluator>()
            .isEnd()
    }
}
