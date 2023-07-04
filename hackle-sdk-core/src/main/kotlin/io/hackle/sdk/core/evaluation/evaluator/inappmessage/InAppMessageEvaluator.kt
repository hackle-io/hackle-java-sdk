package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.storage.HackleInAppMessageStorage
import io.hackle.sdk.core.evaluation.target.InAppMessageResolver
import io.hackle.sdk.core.evaluation.target.InAppMessageTargetDeterminer
import io.hackle.sdk.core.evaluation.target.InAppMessageUserOverrideDeterminer
import io.hackle.sdk.core.model.InAppMessage

internal class InAppMessageEvaluator(
    private val inAppMessageUserOverrideDeterminer: InAppMessageUserOverrideDeterminer,
    private val inAppMessageTargetDeterminer: InAppMessageTargetDeterminer,
    private val inAppMessageStorage: HackleInAppMessageStorage,
    private val inAppMessageResolver: InAppMessageResolver
) : AbstractEvaluator<InAppMessageRequest, InAppMessageEvaluation>() {


    override fun supports(request: Evaluator.Request): Boolean {
        return request is InAppMessageRequest
    }

    override fun evaluateInternal(request: InAppMessageRequest, context: Evaluator.Context): InAppMessageEvaluation {
        val inAppMessage = request.inAppMessage

        if (inAppMessage.messageContext.platformTypes.none { it == InAppMessage.MessageContext.PlatformType.ANDROID }) {
            return InAppMessageEvaluation.of(DecisionReason.UNSUPPORTED_PLATFORM, context)
        }

        if (inAppMessageUserOverrideDeterminer.determine(request)) {
            return evaluation(request, context, DecisionReason.OVERRIDDEN)
        }

        if (inAppMessage.status == InAppMessage.Status.PAUSE) {
            return InAppMessageEvaluation.of(DecisionReason.IN_APP_MESSAGE_PAUSED, context)
        }

        if (inAppMessage.status == InAppMessage.Status.DRAFT) {
            return InAppMessageEvaluation.of(DecisionReason.IN_APP_MESSAGE_DRAFT, context)
        }

        if (!isWithInTimeRange(inAppMessage, request.timestamp)) {
            return InAppMessageEvaluation.of(DecisionReason.NOT_IN_IN_APP_MESSAGE_PERIOD, context)
        }

        if (isHidden(inAppMessage, request.timestamp)) {
            return InAppMessageEvaluation.of(DecisionReason.IN_APP_MESSAGE_HIDDEN, context)
        }

        if (inAppMessageTargetDeterminer.determine(request, context)) {
            return evaluation(request, context, DecisionReason.IN_APP_MESSAGE_TARGET)
        }

        return InAppMessageEvaluation.of(DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET, context)
    }


    private fun isWithInTimeRange(inAppMessage: InAppMessage, nowTimeMillis: Long): Boolean {
        if (!inAppMessage.displayTimeRange.within(nowTimeMillis)) {
            return false
        }
        return true
    }

    private fun isHidden(inAppMessage: InAppMessage, nowTimeMillis: Long): Boolean {
        return inAppMessageStorage.exist(inAppMessage, nowTimeMillis)
    }

    private fun evaluation(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        decisionReason: DecisionReason
    ): InAppMessageEvaluation {
        val message = inAppMessageResolver.resolve(request, context)
        return InAppMessageEvaluation.of(decisionReason, context, message)
    }
}
