package io.hackle.sdk.core.evaluation.service.experiment.flow

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator.Context
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.FlowEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResult
import io.hackle.sdk.core.evaluation.service.experiment.match.*
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.model.Experiment.Status.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

internal typealias ExperimentLocalEvaluationFlow = EvaluationFlow<ExperimentLocalEvaluateRequest, ExperimentEvaluateResult>

internal interface ExperimentLocalFlowEvaluator :
    FlowEvaluator<ExperimentLocalEvaluateRequest, ExperimentEvaluateResult> {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult?
}

internal class OverrideExperimentLocalFlowEvaluator(
    private val overrideResolver: ExperimentOverrideResolver,
) : ExperimentLocalFlowEvaluator {

    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        val overriddenVariation = overrideResolver.resolveOrNull(request, context)
        return if (overriddenVariation != null) {
            when (request.experiment.type) {
                AB_TEST -> ExperimentEvaluateResult.of(OVERRIDDEN, overriddenVariation)
                FEATURE_FLAG -> ExperimentEvaluateResult.of(INDIVIDUAL_TARGET_MATCH, overriddenVariation)
            }
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class DraftExperimentLocalFlowEvaluator : ExperimentLocalFlowEvaluator {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        return if (request.experiment.status == DRAFT) {
            ExperimentEvaluateResult.ofDefault(EXPERIMENT_DRAFT, request)
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class PausedExperimentLocalFlowEvaluator : ExperimentLocalFlowEvaluator {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        return if (request.experiment.status == PAUSED) {
            when (request.experiment.type) {
                AB_TEST -> ExperimentEvaluateResult.ofDefault(EXPERIMENT_PAUSED, request)
                FEATURE_FLAG -> ExperimentEvaluateResult.ofDefault(FEATURE_FLAG_INACTIVE, request)
            }
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class CompletedExperimentLocalFlowEvaluator : ExperimentLocalFlowEvaluator {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        return if (request.experiment.status == COMPLETED) {
            val winnerVariation =
                requireNotNull(request.experiment.winnerVariation) { "winner variation [${request.experiment.id}]" }
            ExperimentEvaluateResult.of(EXPERIMENT_COMPLETED, winnerVariation)
        } else {
            nextFlow.evaluate(request, context)
        }
    }
}

internal class TargetExperimentLocalFlowEvaluator(
    private val experimentTargetDeterminer: ExperimentTargetDeterminer,
) : ExperimentLocalFlowEvaluator {

    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        require(request.experiment.type == AB_TEST) { "experiment type must be AB_TEST [${request.experiment.id}]" }
        val isUserInExperimentTarget = experimentTargetDeterminer.isUserInExperimentTarget(request, context)
        return if (isUserInExperimentTarget) {
            nextFlow.evaluate(request, context)
        } else {
            ExperimentEvaluateResult.ofDefault(NOT_IN_EXPERIMENT_TARGET, request)
        }
    }
}

internal class TrafficAllocateExperimentLocalFlowEvaluator(
    private val actionResolver: ExperimentActionResolver,
) : ExperimentLocalFlowEvaluator {

    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult {
        val experiment = request.experiment
        require(request.experiment.status == RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(request.experiment.type == AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val defaultRule = experiment.defaultRule
        val variation = actionResolver.resolveOrNull(request, defaultRule)
            ?: return ExperimentEvaluateResult.ofDefault(TRAFFIC_NOT_ALLOCATED, request)

        if (variation.isDropped) {
            return ExperimentEvaluateResult.ofDefault(VARIATION_DROPPED, request)
        }

        return ExperimentEvaluateResult.of(TRAFFIC_ALLOCATED, variation)
    }

}

internal class TargetRuleExperimentLocalFlowEvaluator(
    private val targetRuleDeterminer: ExperimentTargetRuleDeterminer,
    private val actionResolver: ExperimentActionResolver,
) : ExperimentLocalFlowEvaluator {

    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        val experiment = request.experiment
        require(experiment.status == RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        if (request.user.identifiers[experiment.identifierType] == null) {
            return nextFlow.evaluate(request, context)
        }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(request, context)
            ?: return nextFlow.evaluate(request, context)

        val variation = requireNotNull(actionResolver.resolveOrNull(request, targetRule.action)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return ExperimentEvaluateResult.of(TARGET_RULE_MATCH, variation)
    }
}

internal class DefaultRuleExperimentLocalFlowEvaluator(
    private val actionResolver: ExperimentActionResolver,
) : ExperimentLocalFlowEvaluator {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        val experiment = request.experiment
        require(experiment.status == RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        if (request.user.identifiers[experiment.identifierType] == null) {
            return ExperimentEvaluateResult.ofDefault(DEFAULT_RULE, request)
        }

        val variation = requireNotNull(actionResolver.resolveOrNull(request, experiment.defaultRule)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return ExperimentEvaluateResult.of(DEFAULT_RULE, variation)
    }
}

internal class ContainerExperimentLocalFlowEvaluator(
    private val containerResolver: ExperimentContainerResolver,
) : ExperimentLocalFlowEvaluator {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        val experiment = request.experiment
        val containerId = experiment.containerId ?: return nextFlow.evaluate(request, context)
        val container = requireNotNull(request.workspace.getContainerOrNull(containerId)) { "Container[$containerId]" }
        return if (containerResolver.isUserInContainerGroup(request, container)) {
            nextFlow.evaluate(request, context)
        } else {
            ExperimentEvaluateResult.ofDefault(NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT, request)
        }
    }
}

internal class IdentifierExperimentLocalFlowEvaluator : ExperimentLocalFlowEvaluator {
    override fun evaluate(
        request: ExperimentLocalEvaluateRequest,
        context: Context,
        nextFlow: ExperimentLocalEvaluationFlow,
    ): ExperimentEvaluateResult? {
        return if (request.user.identifiers[request.experiment.identifierType] != null) {
            nextFlow.evaluate(request, context)
        } else {
            ExperimentEvaluateResult.ofDefault(IDENTIFIER_NOT_FOUND, request)
        }
    }
}
