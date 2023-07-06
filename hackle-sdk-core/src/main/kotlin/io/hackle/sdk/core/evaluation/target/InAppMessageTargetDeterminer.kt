package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher


internal class InAppMessageTargetDeterminer(
    private val targetMatcher: TargetMatcher
) {

    fun determine(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        
        val targets = request.inAppMessage.targetContext.targets
        if (targets.isEmpty()) {
            return true
        }
        return targets.any { targetMatcher.matches(request, context, it) }
    }
}
