package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.core.HackleContext
import io.hackle.sdk.core.evaluation.flow.isDecisionWith
import io.hackle.sdk.core.evaluation.flow.isEnd
import io.hackle.sdk.core.evaluation.service.experiment.flow.CompletedExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.ContainerExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.DefaultRuleExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.DraftExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.ExperimentLocalEvaluationFlowFactory
import io.hackle.sdk.core.evaluation.service.experiment.flow.TargetExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.IdentifierExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.OverrideExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.PausedExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.TargetRuleExperimentLocalFlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.flow.TrafficAllocateExperimentLocalFlowEvaluator
import io.hackle.sdk.core.model.Experiment
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat

internal class ExperimentLocalEvaluationFlowFactoryTest {

    val sut = ExperimentLocalEvaluationFlowFactory(HackleContext().also {
        it.initialize(mockk(), mockk(), mockk())
    })

    @Test
    fun `AB_TEST`() {
        val flow = sut.flow(Experiment.Type.AB_TEST)
        expectThat(flow)
            .isDecisionWith<OverrideExperimentLocalFlowEvaluator>()
            .isDecisionWith<IdentifierExperimentLocalFlowEvaluator>()
            .isDecisionWith<ContainerExperimentLocalFlowEvaluator>()
            .isDecisionWith<TargetExperimentLocalFlowEvaluator>()
            .isDecisionWith<DraftExperimentLocalFlowEvaluator>()
            .isDecisionWith<PausedExperimentLocalFlowEvaluator>()
            .isDecisionWith<CompletedExperimentLocalFlowEvaluator>()
            .isDecisionWith<TrafficAllocateExperimentLocalFlowEvaluator>()
            .isEnd()
    }

    @Test
    fun `FEATURE_FLAG`() {
        val flow = sut.flow(Experiment.Type.FEATURE_FLAG)
        expectThat(flow)
            .isDecisionWith<DraftExperimentLocalFlowEvaluator>()
            .isDecisionWith<PausedExperimentLocalFlowEvaluator>()
            .isDecisionWith<CompletedExperimentLocalFlowEvaluator>()
            .isDecisionWith<OverrideExperimentLocalFlowEvaluator>()
            .isDecisionWith<IdentifierExperimentLocalFlowEvaluator>()
            .isDecisionWith<TargetRuleExperimentLocalFlowEvaluator>()
            .isDecisionWith<DefaultRuleExperimentLocalFlowEvaluator>()
            .isEnd()
    }
}
