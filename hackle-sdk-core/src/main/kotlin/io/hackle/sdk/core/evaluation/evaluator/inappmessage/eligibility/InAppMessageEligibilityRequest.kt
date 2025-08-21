package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluatorRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class InAppMessageEligibilityRequest(
    override val workspace: Workspace,
    override val user: HackleUser,
    val inAppMessage: InAppMessage,
    val timestamp: Long,
) : AbstractEvaluatorRequest() {
    override val key: Evaluator.Key
        get() = Evaluator.Key(Evaluator.Type.IN_APP_MESSAGE, inAppMessage.id)
}
