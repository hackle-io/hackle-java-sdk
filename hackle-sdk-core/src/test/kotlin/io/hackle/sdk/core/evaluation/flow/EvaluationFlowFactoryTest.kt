package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.model.Experiment
import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.isA

internal class EvaluationFlowFactoryTest {

    @Test
    fun `AB_TEST evaluationFlow`() {
        val actual = EvaluationFlowFactory().getFlow(Experiment.Type.AB_TEST)
        expectThat(actual)
            .isDecisionWith<OverrideEvaluator>()
            .isDecisionWith<DraftExperimentEvaluator>()
            .isDecisionWith<PausedExperimentEvaluator>()
            .isDecisionWith<CompletedExperimentEvaluator>()
            .isDecisionWith<AudienceEvaluator>()
            .isDecisionWith<TrafficAllocateEvaluator>()
            .isA<EvaluationFlow.End>()
    }

    @Test
    fun `FEATURE_FLAG evaluationFlow`() {
        val actual = EvaluationFlowFactory().getFlow(Experiment.Type.FEATURE_FLAG)
        expectThat(actual)
            .isDecisionWith<DraftExperimentEvaluator>()
            .isDecisionWith<PausedExperimentEvaluator>()
            .isDecisionWith<CompletedExperimentEvaluator>()
            .isDecisionWith<OverrideEvaluator>()
            .isDecisionWith<TargetRuleEvaluator>()
            .isDecisionWith<DefaultRuleEvaluator>()
            .isA<EvaluationFlow.End>()
    }

    private inline fun <reified T : FlowEvaluator> Assertion.Builder<EvaluationFlow>.isDecisionWith(): Assertion.Builder<EvaluationFlow> {
        return isA<EvaluationFlow.Decision>()
            .and { get { flowEvaluator }.isA<T>() }
            .get { nextFlow }
    }
}