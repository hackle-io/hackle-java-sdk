package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageEvaluatorRequest
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class InAppMessageLayoutRequest(
    override val workspace: Workspace,
    override val user: HackleUser,
    override val inAppMessage: InAppMessage,
) : InAppMessageEvaluatorRequest() {

    companion object {
        fun of(request: InAppMessageEvaluatorRequest): InAppMessageLayoutRequest {
            if (request is InAppMessageLayoutRequest) {
                return request
            }
            return InAppMessageLayoutRequest(
                workspace = request.workspace,
                user = request.user,
                inAppMessage = request.inAppMessage
            )
        }
    }
}
