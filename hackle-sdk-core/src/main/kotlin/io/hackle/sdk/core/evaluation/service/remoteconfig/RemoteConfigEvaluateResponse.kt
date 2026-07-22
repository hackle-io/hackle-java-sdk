package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class RemoteConfigEvaluateResponse(
    override val user: HackleUser,
    override val workspace: Workspace,
    override val evaluation: RemoteConfigEvaluation,
    override val references: List<Evaluation>,
) : EvaluateResponse {

    companion object {
        fun of(
            request: RemoteConfigEvaluateRequest,
            context: Evaluator.Context,
            result: RemoteConfigEvaluateResult,
        ): RemoteConfigEvaluateResponse {
            return RemoteConfigEvaluateResponse(
                user = request.user,
                workspace = request.workspace,
                evaluation = RemoteConfigEvaluation(entity = request.entity, result = result),
                references = context.references
            )
        }
    }
}
