package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
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

    val bucketer: Bucketer
    val targetMatcher: TargetMatcher
    val actionResolver: ActionResolver
    val overrideResolver: OverrideResolver
    val containerResolver: ContainerResolver
    val experimentTargetDeterminer: ExperimentTargetDeterminer
    val targetRuleDeterminer: TargetRuleDeterminer

    /**
     * [EvaluationFlow] for [AB_TEST]
     */
    private val abTestFlow: EvaluationFlow

    /**
     * [EvaluationFlow] for [FEATURE_FLAG]
     */
    private val featureFlagFlow: EvaluationFlow

    init {

        this.bucketer = Bucketer()
        this.targetMatcher = TargetMatcher(ConditionMatcherFactory())
        this.actionResolver = ActionResolver(bucketer)
        this.overrideResolver = OverrideResolver(targetMatcher, actionResolver)
        this.containerResolver = ContainerResolver(bucketer)
        this.experimentTargetDeterminer = ExperimentTargetDeterminer(targetMatcher)
        this.targetRuleDeterminer = TargetRuleDeterminer(targetMatcher, bucketer)

        val abTestFlow = EvaluationFlow.of(
            OverrideEvaluator(overrideResolver),
            IdentifierEvaluator(),
            ContainerEvaluator(containerResolver),
            ExperimentTargetEvaluator(experimentTargetDeterminer),
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
            TargetRuleEvaluator(targetRuleDeterminer, actionResolver),
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
