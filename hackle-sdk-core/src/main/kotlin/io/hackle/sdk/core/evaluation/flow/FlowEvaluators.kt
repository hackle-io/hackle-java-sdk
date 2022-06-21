package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.target.OverrideResolver
import io.hackle.sdk.core.evaluation.target.TargetRuleDeterminer
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class OverrideEvaluator(
    private val overrideResolver: OverrideResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        val overriddenVariation = overrideResolver.resolveOrNull(workspace, experiment, user)
        return if (overriddenVariation != null) {
            when (experiment.type) {
                AB_TEST -> Evaluation.of(overriddenVariation, DecisionReason.OVERRIDDEN)
                FEATURE_FLAG -> Evaluation.of(overriddenVariation, DecisionReason.INDIVIDUAL_TARGET_MATCH)
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
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (experiment.status == Experiment.Status.DRAFT) {
            Evaluation.of(experiment, defaultVariationKey, DecisionReason.EXPERIMENT_DRAFT)
        } else {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        }
    }
}

internal class PausedExperimentEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (experiment.status == Experiment.Status.PAUSED) {
            when (experiment.type) {
                AB_TEST -> Evaluation.of(experiment, defaultVariationKey, DecisionReason.EXPERIMENT_PAUSED)
                FEATURE_FLAG -> Evaluation.of(experiment, defaultVariationKey, DecisionReason.FEATURE_FLAG_INACTIVE)
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
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (experiment.status == Experiment.Status.COMPLETED) {
            val winnerVariation = requireNotNull(experiment.winnerVariation) { "winner variation [${experiment.id}]" }
            Evaluation.of(winnerVariation, DecisionReason.EXPERIMENT_COMPLETED)
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
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment.type == AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val isUserInExperimentTarget = experimentTargetDeterminer.isUserInExperimentTarget(workspace, experiment, user)
        return if (isUserInExperimentTarget) {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        } else {
            Evaluation.of(experiment, defaultVariationKey, DecisionReason.NOT_IN_EXPERIMENT_TARGET)
        }
    }
}

internal class TrafficAllocateEvaluator(
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment.status == Experiment.Status.RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val variation = actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)
            ?: return Evaluation.of(experiment, defaultVariationKey, DecisionReason.TRAFFIC_NOT_ALLOCATED)

        if (variation.isDropped) {
            return Evaluation.of(experiment, defaultVariationKey, DecisionReason.VARIATION_DROPPED)
        }

        return Evaluation.of(variation, DecisionReason.TRAFFIC_ALLOCATED)
    }
}

internal class TargetRuleEvaluator(
    private val targetRuleDeterminer: TargetRuleDeterminer,
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment.status == Experiment.Status.RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(workspace, experiment, user)
            ?: return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)

        val variation = requireNotNull(actionResolver.resolveOrNull(targetRule.action, workspace, experiment, user)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return Evaluation.of(variation, DecisionReason.TARGET_RULE_MATCH)
    }
}

internal class DefaultRuleEvaluator(
    private val actionResolver: ActionResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment.status == Experiment.Status.RUNNING) { "experiment status must be RUNNING [${experiment.id}]" }
        require(experiment.type == FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        val variation =
            requireNotNull(actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)) {
                "FeatureFlag must decide the Variation [${experiment.id}]"
            }

        return Evaluation.of(variation, DecisionReason.DEFAULT_RULE)
    }
}
