package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class ExperimentEvaluateResponse(
    override val user: HackleUser,
    override val workspace: Workspace,
    override val evaluation: ExperimentEvaluation,
    override val references: List<Evaluation>,
) : EvaluateResponse {

    companion object {
        fun of(
            request: ExperimentEvaluateRequest,
            context: Evaluator.Context,
            result: ExperimentEvaluateResult,
        ): ExperimentEvaluateResponse {
            return ExperimentEvaluateResponse(
                user = request.user,
                workspace = request.workspace,
                evaluation = ExperimentEvaluation(entity = request.entity, result = result),
                references = context.references
            )
        }
    }
}
