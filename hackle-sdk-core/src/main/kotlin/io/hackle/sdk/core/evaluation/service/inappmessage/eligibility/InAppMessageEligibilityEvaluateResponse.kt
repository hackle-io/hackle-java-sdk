package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.get
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class InAppMessageEligibilityEvaluateResponse(
    override val user: HackleUser,
    override val workspace: Workspace,
    override val evaluation: InAppMessageEligibilityEvaluation,
    override val references: List<Evaluation>,
    val layout: InAppMessageLayoutEvaluateResponse?,
) : EvaluateResponse {
    companion object {
        fun of(
            request: InAppMessageEligibilityEvaluateRequest,
            context: Evaluator.Context,
            result: InAppMessageEligibilityEvaluateResult,
        ): InAppMessageEligibilityEvaluateResponse {
            return InAppMessageEligibilityEvaluateResponse(
                user = request.user,
                workspace = request.workspace,
                evaluation = InAppMessageEligibilityEvaluation(entity = request.entity, result = result),
                references = context.references,
                layout = context.get<InAppMessageLayoutEvaluateResponse>()
            )
        }
    }
}
