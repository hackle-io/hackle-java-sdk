package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.target.ExperimentTargetRuleDeterminer
import io.hackle.sdk.core.evaluation.target.OverrideResolver
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Status.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

internal class OverrideEvaluator(
    private val overrideResolver: OverrideResolver
) : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        val overriddenVariation = overrideResolver.resolveOrNull(request, context)
        return if (overriddenVariation != null) {
            when (request.experiment.type) {
                AB_TEST -> ExperimentEvaluation.of(request, overriddenVariation, OVERRIDDEN)
                FEATURE_FLAG -> ExperimentEvaluation.of(request, overriddenVariation, INDIVIDUAL_TARGET_MATCH)
            }
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class DraftExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        return if (request.experiment.status == DRAFT) {
            ExperimentEvaluation.ofDefault(request, context, EXPERIMENT_DRAFT)
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class PausedExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        return if (request.experiment.status == PAUSED) {
            when (request.experiment.type) {
                AB_TEST -> ExperimentEvaluation.ofDefault(request, context, EXPERIMENT_PAUSED)
                FEATURE_FLAG -> ExperimentEvaluation.ofDefault(request, context, FEATURE_FLAG_INACTIVE)
            }
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class CompletedExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        return if (request.experiment.status == COMPLETED) {
            val winnerVariation =
                requireNotNull(request.experiment.winnerVariation) { "winner variation [${request.experiment.id}]" }
            ExperimentEvaluation.of(request, winnerVariation, EXPERIMENT_COMPLETED)
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class ExperimentTargetEvaluator(
    private val experimentTargetDeterminer: ExperimentTargetDeterminer
) : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        require(request.experiment.type == AB_TEST) { "experiment type must be AB_TEST [${request.experiment.id}]" }
        val isUserInExperimentTarget = experimentTargetDeterminer.isUserInExperimentTarget(request, context)
        return if (isUserInExperimentTarget) {
            nextFlow.evaluate(request, context)
        } else {
            ExperimentEvaluation.ofDefault(request, context, NOT_IN_EXPERIMENT_TARGET)
        }
    }
}

internal class TrafficAllocateEvaluator(
    private val actionResolver: ActionResolver
) : FlowEvaluator {

    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        val experiment = request.experiment
        require(request.experiment.status == Experiment.Status.RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(request.experiment.type == AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val defaultRule = experiment.defaultRule
        val variation = actionResolver.resolveOrNull(request, defaultRule)
            ?: return ExperimentEvaluation.ofDefault(request, context, TRAFFIC_NOT_ALLOCATED)

        if (variation.isDropped) {
            return ExperimentEvaluation.ofDefault(request, context, VARIATION_DROPPED)
        }

        return ExperimentEvaluation.of(request, variation, TRAFFIC_ALLOCATED)
    }

}

internal class TargetRuleEvaluator(
    private val targetRuleDeterminer: ExperimentTargetRuleDeterminer, private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        val experiment = request.experiment
        require(experiment.status == Experiment.Status.RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        if (request.user.identifiers[experiment.identifierType] == null) {
            return nextFlow.evaluate(request, context)
        }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(request, context)
            ?: return nextFlow.evaluate(request, context)

        val variation = requireNotNull(actionResolver.resolveOrNull(request, targetRule.action)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return ExperimentEvaluation.of(request, variation, TARGET_RULE_MATCH)
    }

}

internal class DefaultRuleEvaluator(
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        val experiment = request.experiment
        require(experiment.status == Experiment.Status.RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        if (request.user.identifiers[experiment.identifierType] == null) {
            return ExperimentEvaluation.ofDefault(request, context, DEFAULT_RULE)
        }

        val variation = requireNotNull(actionResolver.resolveOrNull(request, experiment.defaultRule)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return ExperimentEvaluation.of(request, variation, DEFAULT_RULE)
    }
}

internal class ContainerEvaluator(
    private val containerResolver: ContainerResolver
) : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        val experiment = request.experiment
        val containerId = experiment.containerId ?: return nextFlow.evaluate(request, context)
        val container = requireNotNull(request.workspace.getContainerOrNull(containerId)) { "Container[$containerId]" }
        return if (containerResolver.isUserInContainerGroup(request, container)) {
            nextFlow.evaluate(request, context)
        } else {
            ExperimentEvaluation.ofDefault(request, context, NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT)
        }
    }
}

internal class IdentifierEvaluator : FlowEvaluator {
    override fun evaluate(
        request: ExperimentRequest,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow
    ): ExperimentEvaluation {
        return if (request.user.identifiers[request.experiment.identifierType] != null) {
            nextFlow.evaluate(request, context)
        } else {
            ExperimentEvaluation.ofDefault(request, context, IDENTIFIER_NOT_FOUND)
        }
    }
}
