package io.hackle.sdk.core.evaluation.service.experiment.flow

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.experiment.match.*
import io.hackle.sdk.core.model.Experiment

class ExperimentLocalEvaluationFlowFactory(
    targetMatcher: TargetMatcher,
    bucketer: Bucketer,
    overrideStorage: ExperimentManualOverrideStorage,
) {
    private val abTestFlow: ExperimentLocalEvaluationFlow
    private val featureFlagFlow: ExperimentLocalEvaluationFlow

    init {
        val actionResolver = ExperimentActionResolver(bucketer)
        val overrideResolver = ExperimentOverrideResolver(overrideStorage, targetMatcher, actionResolver)

        abTestFlow = ExperimentLocalEvaluationFlow.of(
            OverrideExperimentLocalFlowEvaluator(overrideResolver),
            IdentifierExperimentLocalFlowEvaluator(),
            ContainerExperimentLocalFlowEvaluator(ExperimentContainerResolver(bucketer)),
            TargetExperimentLocalFlowEvaluator(ExperimentTargetDeterminer(targetMatcher)),
            DraftExperimentLocalFlowEvaluator(),
            PausedExperimentLocalFlowEvaluator(),
            CompletedExperimentLocalFlowEvaluator(),
            TrafficAllocateExperimentLocalFlowEvaluator(actionResolver)
        )

        featureFlagFlow = ExperimentLocalEvaluationFlow.of(
            DraftExperimentLocalFlowEvaluator(),
            PausedExperimentLocalFlowEvaluator(),
            CompletedExperimentLocalFlowEvaluator(),
            OverrideExperimentLocalFlowEvaluator(overrideResolver),
            IdentifierExperimentLocalFlowEvaluator(),
            TargetRuleExperimentLocalFlowEvaluator(ExperimentTargetRuleDeterminer(targetMatcher), actionResolver),
            DefaultRuleExperimentLocalFlowEvaluator(actionResolver)
        )
    }

    fun flow(experimentType: Experiment.Type): ExperimentLocalEvaluationFlow {
        return when (experimentType) {
            Experiment.Type.AB_TEST -> abTestFlow
            Experiment.Type.FEATURE_FLAG -> featureFlagFlow
        }
    }
}
