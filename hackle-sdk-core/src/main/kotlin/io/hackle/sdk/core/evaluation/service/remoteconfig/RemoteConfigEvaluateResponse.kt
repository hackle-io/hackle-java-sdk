package io.hackle.sdk.core.evaluation.service.remoteconfig

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class RemoteConfigEvaluateResponse<out T>(
    override val user: HackleUser,
    override val workspace: Workspace,
    override val evaluation: RemoteConfigEvaluation<T>,
    override val references: List<Evaluation>,
) : EvaluateResponse {

    companion object {
        fun <T : Any> of(
            request: RemoteConfigEvaluateRequest<T>,
            context: Evaluator.Context,
            result: RemoteConfigEvaluateResult<T>,
        ): RemoteConfigEvaluateResponse<T> {
            val properties = PropertiesBuilder()
                .add("requestValueType", request.requiredType.name)
                .add("requestDefaultValue", request.defaultValue)
                .add("returnValue", result.value)
                .build()
            return RemoteConfigEvaluateResponse(
                user = request.user,
                workspace = request.workspace,
                evaluation = RemoteConfigEvaluation(entity = request.entity, result = result, properties = properties),
                references = context.references
            )
        }
    }
}
