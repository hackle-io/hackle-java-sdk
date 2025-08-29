package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.get
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

/**
 * @author Yong
 */
class ExperimentFlowFactory(context: EvaluationContext) {

    private val abTestFlow: ExperimentFlow = ExperimentFlow.of(
        OverrideEvaluator(context.get()),
        IdentifierEvaluator(),
        ContainerEvaluator(context.get()),
        ExperimentTargetEvaluator(context.get()),
        DraftExperimentEvaluator(),
        PausedExperimentEvaluator(),
        CompletedExperimentEvaluator(),
        TrafficAllocateEvaluator(context.get())
    )
    private val featureFlagFlow: ExperimentFlow = ExperimentFlow.of(
        DraftExperimentEvaluator(),
        PausedExperimentEvaluator(),
        CompletedExperimentEvaluator(),
        OverrideEvaluator(context.get()),
        IdentifierEvaluator(),
        TargetRuleEvaluator(context.get(), context.get()),
        DefaultRuleEvaluator(context.get())
    )

    internal fun experimentFlow(experimentType: Experiment.Type): ExperimentFlow {
        return when (experimentType) {
            AB_TEST -> abTestFlow
            FEATURE_FLAG -> featureFlagFlow
        }
    }
}
