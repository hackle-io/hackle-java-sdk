package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.target.TargetAudienceDeterminer
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

        val abTestFlow = EvaluationFlow.of(
            OverrideEvaluator(),
            DraftExperimentEvaluator(),
            PausedExperimentEvaluator(),
            CompletedExperimentEvaluator(),
            AudienceEvaluator(TargetAudienceDeterminer(targetMatcher)),
            TrafficAllocateEvaluator(actionResolver)
        )

        val featureFlagFlow = EvaluationFlow.of(
            DraftExperimentEvaluator(),
            PausedExperimentEvaluator(),
            CompletedExperimentEvaluator(),
            OverrideEvaluator(),
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