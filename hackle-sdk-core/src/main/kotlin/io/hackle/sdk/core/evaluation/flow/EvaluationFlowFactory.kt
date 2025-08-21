package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.evaluator.experiment.*
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility.*
import io.hackle.sdk.core.evaluation.get
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

/**
 * @author Yong
 */
class EvaluationFlowFactory(context: EvaluationContext) {

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
    private val featureFlagFlow: ExperimentFlow = EvaluationFlow.of(
        DraftExperimentEvaluator(),
        PausedExperimentEvaluator(),
        CompletedExperimentEvaluator(),
        OverrideEvaluator(context.get()),
        IdentifierEvaluator(),
        TargetRuleEvaluator(context.get(), context.get()),
        DefaultRuleEvaluator(context.get())
    )
    private val inAppMessageEligibilityFlow: InAppMessageEligibilityFlow = InAppMessageEligibilityFlow.of(
        PlatformInAppMessageEligibilityFlowEvaluator(),
        OverrideInAppMessageEligibilityFlowEvaluator(context.get()),
        DraftInAppMessageEligibilityFlowEvaluator(),
        PauseInAppMessageEligibilityFlowEvaluator(),
        PeriodInAppMessageEligibilityFlowEvaluator(),
        TargetInAppMessageEligibilityFlowEvaluator(context.get()),
        FrequencyCapInAppMessageEligibilityFlowEvaluator(context.get()),
        HiddenInAppMessageEligibilityFlowEvaluator(context.get()),
        EligibleInAppMessageEligibilityFlowEvaluator()
    )

    internal fun experimentFlow(experimentType: Experiment.Type): ExperimentFlow {
        return when (experimentType) {
            AB_TEST -> abTestFlow
            FEATURE_FLAG -> featureFlagFlow
        }
    }

    internal fun inAppMessageFlow(): InAppMessageEligibilityFlow {
        return inAppMessageEligibilityFlow
    }
}
