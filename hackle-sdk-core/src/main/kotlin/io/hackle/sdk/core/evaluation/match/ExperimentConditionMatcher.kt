package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentReferenceLocalEvaluator
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.AB_TEST
import io.hackle.sdk.core.model.Target.Key.Type.FEATURE_FLAG
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig

internal class ExperimentConditionMatcher(
    private val abTestMatcher: AbTestReferenceLocalEvaluateMatcher,
    private val featureFlagMatcher: FeatureFlagReferenceLocalEvaluateMatcher,
) : ConditionMatcher {

    override fun matches(request: EvaluateRequest, context: Evaluator.Context, condition: Target.Condition): Boolean {
        if (request !is LocalEvaluateRequest) {
            return false
        }
        return when (condition.key.type) {
            AB_TEST -> abTestMatcher.matches(request, context, condition)
            FEATURE_FLAG -> featureFlagMatcher.matches(request, context, condition)
            else -> throw IllegalArgumentException("Unsupported Target.Key.Type[${condition.key.type}]")
        }
    }
}

internal abstract class ExperimentReferenceLocalEvaluateMatcher {

    protected abstract val evaluator: ExperimentReferenceLocalEvaluator
    protected abstract val valueOperatorMatcher: ValueOperatorMatcher

    fun matches(request: LocalEvaluateRequest, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val key =
            requireNotNull(condition.key.name.toLongOrNull()) { "Invalid key [${condition.key.type}, ${condition.key.name}]" }
        val experiment = experiment(request, key) ?: return false
        val evaluation = evaluator.evaluate(request, context, experiment)
        return matches(evaluation, condition)
    }

    protected abstract fun experiment(request: LocalEvaluateRequest, key: Long): ExperimentConfig?

    protected abstract fun matches(evaluation: ExperimentEvaluation, condition: Target.Condition): Boolean
}

internal class AbTestReferenceLocalEvaluateMatcher(
    override val evaluator: ExperimentReferenceLocalEvaluator,
    override val valueOperatorMatcher: ValueOperatorMatcher,
) : ExperimentReferenceLocalEvaluateMatcher() {
    override fun experiment(request: LocalEvaluateRequest, key: Long): ExperimentConfig? {
        return request.workspace.getExperimentOrNull(key)
    }

    override fun matches(evaluation: ExperimentEvaluation, condition: Target.Condition): Boolean {
        if (evaluation.result.reason !in AB_TEST_MATCHED_REASONS) {
            return false
        }
        return valueOperatorMatcher.matches(evaluation.result.variation.key, condition.match)
    }

    companion object {

        private val AB_TEST_MATCHED_REASONS = setOf(
            OVERRIDDEN,
            TRAFFIC_ALLOCATED,
            EXPERIMENT_COMPLETED,
        )
    }
}

internal class FeatureFlagReferenceLocalEvaluateMatcher(
    override val evaluator: ExperimentReferenceLocalEvaluator,
    override val valueOperatorMatcher: ValueOperatorMatcher,
) : ExperimentReferenceLocalEvaluateMatcher() {
    override fun experiment(request: LocalEvaluateRequest, key: Long): ExperimentConfig? {
        return request.workspace.getFeatureFlagOrNull(key)
    }

    override fun matches(evaluation: ExperimentEvaluation, condition: Target.Condition): Boolean {
        val on = Variation.from(evaluation.result.variation.key).isOn
        return valueOperatorMatcher.matches(on, condition.match)
    }

    private val Variation.isOn: Boolean get() = isExperimental
}
