package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.evaluation.target.ExperimentTargetRuleDeterminer
import io.hackle.sdk.core.evaluation.target.OverrideResolver
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
                AB_TEST -> Evaluation.of(workspace, overriddenVariation, OVERRIDDEN)
                FEATURE_FLAG -> Evaluation.of(workspace, overriddenVariation, INDIVIDUAL_TARGET_MATCH)
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
            Evaluation.of(workspace, experiment, defaultVariationKey, EXPERIMENT_DRAFT)
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
                AB_TEST -> Evaluation.of(workspace, experiment, defaultVariationKey, EXPERIMENT_PAUSED)
                FEATURE_FLAG -> Evaluation.of(workspace, experiment, defaultVariationKey, FEATURE_FLAG_INACTIVE)
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
            Evaluation.of(workspace, winnerVariation, EXPERIMENT_COMPLETED)
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
            Evaluation.of(workspace, experiment, defaultVariationKey, NOT_IN_EXPERIMENT_TARGET)
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
            ?: return Evaluation.of(workspace, experiment, defaultVariationKey, TRAFFIC_NOT_ALLOCATED)

        if (variation.isDropped) {
            return Evaluation.of(workspace, experiment, defaultVariationKey, VARIATION_DROPPED)
        }

        return Evaluation.of(workspace, variation, TRAFFIC_ALLOCATED)
    }
}

internal class TargetRuleEvaluator(
    private val targetRuleDeterminer: ExperimentTargetRuleDeterminer,
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

        if (user.identifiers[experiment.identifierType] == null) {
            return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(workspace, experiment, user)
            ?: return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)

        val variation = requireNotNull(actionResolver.resolveOrNull(targetRule.action, workspace, experiment, user)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return Evaluation.of(workspace, variation, TARGET_RULE_MATCH)
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

        if (user.identifiers[experiment.identifierType] == null) {
            return Evaluation.of(workspace, experiment, defaultVariationKey, DEFAULT_RULE)
        }

        val variation =
            requireNotNull(actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)) {
                "FeatureFlag must decide the Variation [${experiment.id}]"
            }

        return Evaluation.of(workspace, variation, DEFAULT_RULE)
    }
}

internal class ContainerEvaluator(
    private val containerResolver: ContainerResolver
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        val containerId =
            experiment.containerId ?: return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        val container = workspace.getContainerOrNull(containerId)
        requireNotNull(container) { "container group not exist. containerId = ${experiment.containerId}" }
        val bucket = workspace.getBucketOrNull(container.bucketId)
        requireNotNull(bucket) { "container group bucket not exist. bucketId = ${container.bucketId}" }

        return if (containerResolver.isUserInContainerGroup(container, bucket, experiment, user)) {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        } else {
            Evaluation.of(workspace, experiment, defaultVariationKey, NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT)
        }
    }
}

internal class IdentifierEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        return if (user.identifiers[experiment.identifierType] != null) {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        } else {
            Evaluation.of(workspace, experiment, defaultVariationKey, IDENTIFIER_NOT_FOUND)
        }
    }
}