package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.mutualexclusion.ContainerResolver
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.target.OverrideResolver
import io.hackle.sdk.core.evaluation.target.TargetRuleDeterminer
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

/**
 * @author Yong
 */
internal class EvaluationFlowFactory {

    /**
     * [EvaluationFlow] for [AB_TEST]
     */
    private val abTestFlow: EvaluationFlow

    /**
     * [EvaluationFlow] for [FEATURE_FLAG]
     */
    private val featureFlagFlow: EvaluationFlow

    init {

        val targetMatcher = TargetMatcher(ConditionMatcherFactory())
        val actionResolver = ActionResolver(Bucketer())
        val overrideResolver = OverrideResolver(targetMatcher, actionResolver)
        val containerResolver = ContainerResolver(Bucketer())

        val abTestFlow = EvaluationFlow.of(
            OverrideEvaluator(overrideResolver),
            IdentifierEvaluator(),
            ContainerEvaluator(containerResolver),
            ExperimentTargetEvaluator(ExperimentTargetDeterminer(targetMatcher)),
            DraftExperimentEvaluator(),
            PausedExperimentEvaluator(),
            CompletedExperimentEvaluator(),
            TrafficAllocateEvaluator(actionResolver)
        )

        val featureFlagFlow = EvaluationFlow.of(
            DraftExperimentEvaluator(),
            PausedExperimentEvaluator(),
            CompletedExperimentEvaluator(),
            OverrideEvaluator(overrideResolver),
            IdentifierEvaluator(),
            TargetRuleEvaluator(TargetRuleDeterminer(targetMatcher), actionResolver),
            DefaultRuleEvaluator(actionResolver)
        )

        this.abTestFlow = abTestFlow
        this.featureFlagFlow = featureFlagFlow
    }

    fun getFlow(experimentType: Experiment.Type): EvaluationFlow {
        return when (experimentType) {
            AB_TEST -> abTestFlow
            FEATURE_FLAG -> featureFlagFlow
        }
    }
}
