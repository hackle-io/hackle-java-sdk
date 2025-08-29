package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluatorRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

abstract class InAppMessageEvaluatorRequest : AbstractEvaluatorRequest() {
    abstract override val workspace: Workspace
    abstract override val user: HackleUser
    abstract val inAppMessage: InAppMessage

    final override val key: Evaluator.Key get() = Evaluator.Key(Evaluator.Type.IN_APP_MESSAGE, inAppMessage.id)
}
