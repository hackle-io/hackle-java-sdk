package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.target.*
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

/**
 * @author Yong
 */
internal class EvaluationFlowFactory(manualOverrideStorage: ManualOverrideStorage) {

    /**
     * [EvaluationFlow] for [AB_TEST]
     */
    private val abTestFlow: EvaluationFlow

    /**
     * [EvaluationFlow] for [FEATURE_FLAG]
     */
    private val featureFlagFlow: EvaluationFlow


    val remoteConfigParameterTargetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer

    init {

        val bucketer = Bucketer()
        val targetMatcher = TargetMatcher(ConditionMatcherFactory())
        val actionResolver = ActionResolver(bucketer)
        val overrideResolver = OverrideResolver(manualOverrideStorage, targetMatcher, actionResolver)
        val containerResolver = ContainerResolver(bucketer)

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
            TargetRuleEvaluator(ExperimentTargetRuleDeterminer(targetMatcher), actionResolver),
            DefaultRuleEvaluator(actionResolver)
        )

        this.abTestFlow = abTestFlow
        this.featureFlagFlow = featureFlagFlow
        this.remoteConfigParameterTargetRuleDeterminer =
            RemoteConfigParameterTargetRuleDeterminer(targetMatcher, bucketer)
    }

    fun getFlow(experimentType: Experiment.Type): EvaluationFlow {
        return when (experimentType) {
            AB_TEST -> abTestFlow
            FEATURE_FLAG -> featureFlagFlow
        }
    }
}
