package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.target.TargetRuleDeterminer
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.workspace.Workspace

internal class OverrideEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {

        val overriddenVariation = experiment.getOverriddenVariationOrNull(user)
        return if (overriddenVariation != null) {
            when (experiment.type) {
                AB_TEST -> Evaluation.of(DecisionReason.OVERRIDDEN, overriddenVariation.key)
                FEATURE_FLAG -> Evaluation.of(DecisionReason.INDIVIDUAL_TARGET_MATCH, overriddenVariation)
            }
        } else {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        }
    }
}

internal class DraftExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (experiment is Experiment.Draft) {
            Evaluation.of(DecisionReason.EXPERIMENT_DRAFT, defaultVariationKey)
        } else {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        }
    }
}

internal class PausedExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (experiment is Experiment.Paused) {
            when (experiment.type) {
                AB_TEST -> Evaluation.of(DecisionReason.EXPERIMENT_PAUSED, defaultVariationKey)
                FEATURE_FLAG -> Evaluation.of(DecisionReason.FEATURE_FLAG_INACTIVE, defaultVariationKey)
            }
        } else {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        }
    }
}

internal class CompletedExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (experiment is Experiment.Completed) {
            Evaluation.of(DecisionReason.EXPERIMENT_COMPLETED, experiment.winnerVariation.key)
        } else {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        }
    }
}

internal class ExperimentTargetEvaluator(
    private val experimentTargetDeterminer: ExperimentTargetDeterminer
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment is Experiment.Running) { "experiment must be running [${experiment.id}]" }
        require(experiment.type == AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val isUserInExperimentTarget = experimentTargetDeterminer.isUserInExperimentTarget(workspace, experiment, user)
        return if (isUserInExperimentTarget) {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        } else {
            Evaluation.of(DecisionReason.NOT_IN_EXPERIMENT_TARGET, defaultVariationKey)
        }
    }
}

internal class TrafficAllocateEvaluator(
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment is Experiment.Running) { "experiment must be running [${experiment.id}]" }
        require(experiment.type == AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val variation = actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)
            ?: return Evaluation.of(DecisionReason.TRAFFIC_NOT_ALLOCATED, defaultVariationKey)

        if (variation.isDropped) {
            return Evaluation.of(DecisionReason.VARIATION_DROPPED, defaultVariationKey)
        }

        return Evaluation.of(DecisionReason.TRAFFIC_ALLOCATED, variation)
    }
}

internal class TargetRuleEvaluator(
    private val targetRuleDeterminer: TargetRuleDeterminer,
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment is Experiment.Running) { "experiment must be running [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(workspace, experiment, user)
            ?: return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)

        val variation = requireNotNull(actionResolver.resolveOrNull(targetRule.action, workspace, experiment, user)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return Evaluation.of(DecisionReason.TARGET_RULE_MATCH, variation)
    }
}

internal class DefaultRuleEvaluator(
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment is Experiment.Running) { "experiment must be running [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        val variation =
            requireNotNull(actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)) {
                "FeatureFlag must decide the Variation [${experiment.id}]"
            }

        return Evaluation.of(DecisionReason.DEFAULT_RULE, variation)
    }
}
