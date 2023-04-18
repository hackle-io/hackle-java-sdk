package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*

internal class ExperimentConditionMatcher(
    private val abTestMatcher: AbTestConditionMatcher,
    private val featureFlagMatcher: FeatureFlagConditionMatcher,
) : ConditionMatcher {

    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        return when (condition.key.type) {
            AB_TEST -> abTestMatcher.matches(request, context, condition)
            FEATURE_FLAG -> featureFlagMatcher.matches(request, context, condition)
            USER_ID, USER_PROPERTY, HACKLE_PROPERTY, SEGMENT -> throw IllegalArgumentException("Unsupported Target.Key.Type[${condition.key.type}]")
        }
    }
}

internal abstract class ExperimentMatcher {

    protected abstract val evaluator: Evaluator
    protected abstract val valueOperatorMatcher: ValueOperatorMatcher

    fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val key = requireNotNull(condition.key.name.toLongOrNull()) { "Invalid key [${condition.key.type}, ${condition.key.name}]" }
        val experiment = experiment(request, key) ?: return false
        val evaluation = context[experiment] ?: evaluate(request, context, experiment)
        return matches(evaluation as ExperimentEvaluation, condition)
    }

    private fun evaluate(
        request: Evaluator.Request,
        context: Evaluator.Context,
        experiment: Experiment
    ): Evaluator.Evaluation {
        val experimentRequest = ExperimentRequest.of(requestedBy = request, experiment = experiment)
        val evaluation = evaluator.evaluate(experimentRequest, context)
        return resolve(request, evaluation as ExperimentEvaluation)
            .also { context.add(it) }
    }

    protected abstract fun experiment(request: Evaluator.Request, key: Long): Experiment?

    protected abstract fun resolve(request: Evaluator.Request, evaluation: ExperimentEvaluation): ExperimentEvaluation

    protected abstract fun matches(evaluation: ExperimentEvaluation, condition: Target.Condition): Boolean
}


internal class AbTestConditionMatcher(
    override val evaluator: Evaluator,
    override val valueOperatorMatcher: ValueOperatorMatcher
) : ExperimentMatcher() {
    override fun experiment(request: Evaluator.Request, key: Long): Experiment? {
        return request.workspace.getExperimentOrNull(key)
    }

    override fun resolve(request: Evaluator.Request, evaluation: ExperimentEvaluation): ExperimentEvaluation {
        if (request is ExperimentRequest && evaluation.reason == TRAFFIC_ALLOCATED) {
            return evaluation.with(TRAFFIC_ALLOCATED_BY_TARGETING)
        }
        return evaluation
    }

    override fun matches(evaluation: ExperimentEvaluation, condition: Target.Condition): Boolean {
        if (evaluation.reason !in AB_TEST_MATCHED_REASONS) {
            return false
        }
        return valueOperatorMatcher.matches(evaluation.variationKey, condition.match)
    }

    companion object {

        private val AB_TEST_MATCHED_REASONS = setOf(
            OVERRIDDEN,
            TRAFFIC_ALLOCATED,
            TRAFFIC_ALLOCATED_BY_TARGETING,
            EXPERIMENT_COMPLETED,
        )
    }
}

internal class FeatureFlagConditionMatcher(
    override val evaluator: Evaluator,
    override val valueOperatorMatcher: ValueOperatorMatcher
) : ExperimentMatcher() {
    override fun experiment(request: Evaluator.Request, key: Long): Experiment? {
        return request.workspace.getFeatureFlagOrNull(key)
    }

    override fun resolve(request: Evaluator.Request, evaluation: ExperimentEvaluation): ExperimentEvaluation {
        return evaluation
    }

    override fun matches(evaluation: ExperimentEvaluation, condition: Target.Condition): Boolean {
        val on = Variation.from(evaluation.variationKey).isOn
        return valueOperatorMatcher.matches(on, condition.match)
    }

    private val Variation.isOn: Boolean get() = isExperimental
}
