package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.target.TargetAudienceDeterminer
import io.hackle.sdk.core.evaluation.target.TargetRuleDeterminer
import io.hackle.sdk.core.model.Experiment
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
        if (overriddenVariation != null) {
            return Evaluation.of(DecisionReason.OVERRIDDEN, overriddenVariation.key)
        }

        return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
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
            Evaluation.of(DecisionReason.EXPERIMENT_PAUSED, defaultVariationKey)
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

internal class AudienceEvaluator(
    private val targetAudienceDeterminer: TargetAudienceDeterminer
) : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {
        require(experiment is Experiment.Running) { "experiment must be running [${experiment.id}]" }
        require(experiment.type == Experiment.Type.AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val isUserInAudiences = targetAudienceDeterminer.isUserInAudiences(workspace, experiment, user)
        return if (isUserInAudiences) {
            nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
        } else {
            Evaluation.of(DecisionReason.NOT_IN_AUDIENCE, defaultVariationKey)
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
        require(experiment.type == Experiment.Type.AB_TEST) { "experiment type must be AB_TEST [${experiment.id}]" }

        val variation = actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)
            ?: return Evaluation.of(DecisionReason.TRAFFIC_NOT_ALLOCATED, defaultVariationKey)

        if (variation.isDropped) {
            return Evaluation.of(DecisionReason.VARIATION_DROPPED, defaultVariationKey)
        }

        return Evaluation.of(DecisionReason.TRAFFIC_ALLOCATED, variation)
    }
}

internal class IndividualTargetEvaluator : FlowEvaluator {
    override fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation {

        val overriddenVariation = experiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Evaluation.of(DecisionReason.INDIVIDUAL_TARGET, overriddenVariation)
        }

        return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)
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
        require(experiment.type == Experiment.Type.FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(workspace, experiment, user)
            ?: return nextFlow.evaluate(workspace, experiment, user, defaultVariationKey)

        val variation = requireNotNull(actionResolver.resolveOrNull(targetRule.action, workspace, experiment, user)) {
            "FeatureFlag must decide the Variation [${experiment.id}]"
        }

        return Evaluation.of(DecisionReason.TARGET_RULE, variation)
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
        require(experiment.type == Experiment.Type.FEATURE_FLAG) { "experiment type must be FEATURE_FLAG [${experiment.id}]" }

        val variation =
            requireNotNull(actionResolver.resolveOrNull(experiment.defaultRule, workspace, experiment, user)) {
                "FeatureFlag must decide the Variation [${experiment.id}]"
            }

        return Evaluation.of(DecisionReason.DEFAULT_RULE, variation)
    }
}
