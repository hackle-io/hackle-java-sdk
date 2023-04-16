package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.Variation
import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluatorRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class ExperimentRequest private constructor(
    override val workspace: Workspace,
    override val user: HackleUser,
    val experiment: Experiment,
    val defaultVariationKey: String
) : AbstractEvaluatorRequest() {

    override val key: Evaluator.Key = Evaluator.Key(Evaluator.Type.EXPERIMENT, experiment.id)

    override fun toString(): String {
        return "EvaluatorRequest(type=${experiment.type}, key=${experiment.key})"
    }

    companion object {

        fun of(
            workspace: Workspace,
            user: HackleUser,
            experiment: Experiment,
            defaultVariation: Variation
        ): ExperimentRequest {
            return ExperimentRequest(
                workspace = workspace,
                user = user,
                experiment = experiment,
                defaultVariationKey = defaultVariation.name
            )
        }

        fun of(requestedBy: Evaluator.Request, experiment: Experiment): ExperimentRequest {
            return ExperimentRequest(
                workspace = requestedBy.workspace,
                user = requestedBy.user,
                experiment = experiment,
                defaultVariationKey = Variation.CONTROL.name
            )
        }
    }
}
