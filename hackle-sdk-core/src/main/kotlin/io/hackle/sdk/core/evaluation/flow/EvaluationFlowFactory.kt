package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.evaluator.experiment.*
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.*
import io.hackle.sdk.core.evaluation.get
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

/**
 * @author Yong
 */
internal class EvaluationFlowFactory(context: EvaluationContext) {

    private val abTestFlow: ExperimentFlow
    private val featureFlagFlow: ExperimentFlow
    private val inAppMessageFlow: InAppMessageFlow


    init {
        abTestFlow = ExperimentFlow.of(
            OverrideEvaluator(context.get()),
            IdentifierEvaluator(),
            ContainerEvaluator(context.get()),
            ExperimentTargetEvaluator(context.get()),
            DraftExperimentEvaluator(),
            PausedExperimentEvaluator(),
            CompletedExperimentEvaluator(),
            TrafficAllocateEvaluator(context.get())
        )
        featureFlagFlow = EvaluationFlow.of(
            DraftExperimentEvaluator(),
            PausedExperimentEvaluator(),
            CompletedExperimentEvaluator(),
            OverrideEvaluator(context.get()),
            IdentifierEvaluator(),
            TargetRuleEvaluator(context.get(), context.get()),
            DefaultRuleEvaluator(context.get())
        )

        inAppMessageFlow = InAppMessageFlow.of(
            PlatformInAppMessageFlowEvaluator(),
            OverrideInAppMessageFlowEvaluator(context.get(), context.get()),
            DraftInAppMessageFlowEvaluator(),
            PauseInAppMessageFlowEvaluator(),
            PeriodInAppMessageFlowEvaluator(),
            HiddenInAppMessageFlowEvaluator(context.get()),
            TargetInAppMessageFlowEvaluator(context.get(), context.get())
        )
    }

    fun experimentFlow(experimentType: Experiment.Type): ExperimentFlow {
        return when (experimentType) {
            AB_TEST -> abTestFlow
            FEATURE_FLAG -> featureFlagFlow
        }
    }

    fun inAppMessageFlow(): InAppMessageFlow {
        return inAppMessageFlow
    }
}
