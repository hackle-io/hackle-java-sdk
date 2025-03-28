package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.FlowEvaluator
import io.hackle.sdk.core.evaluation.target.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageResolver
import io.hackle.sdk.core.evaluation.target.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.model.InAppMessage.PlatformType.ANDROID
import io.hackle.sdk.core.model.InAppMessage.Status.DRAFT
import io.hackle.sdk.core.model.InAppMessage.Status.PAUSE
import io.hackle.sdk.core.model.contains
import io.hackle.sdk.core.model.supports

internal typealias InAppMessageFlow = EvaluationFlow<InAppMessageRequest, InAppMessageEvaluation>

internal interface InAppMessageFlowEvaluator : FlowEvaluator<InAppMessageRequest, InAppMessageEvaluation> {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation?
}

private fun InAppMessageResolver.resolve(
    request: InAppMessageRequest,
    context: Evaluator.Context,
    reason: DecisionReason
): InAppMessageEvaluation {
    val message = resolve(request, context)
    return InAppMessageEvaluation.of(request, context, reason, message)
}

/**
 * Android platform check
 *
 * 안드로이드를 지원안하면 UNSUPPORTED_PLATFORM
 */
internal class PlatformInAppMessageFlowEvaluator : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        if (!request.inAppMessage.supports(ANDROID)) {
            return InAppMessageEvaluation.of(request, context, UNSUPPORTED_PLATFORM)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Specific User Check
 *
 * 특정 유저가 타겟팅 된 경우 OVERRIDDEN
 */
internal class OverrideInAppMessageFlowEvaluator(
    private val userOverrideMatcher: InAppMessageUserOverrideMatcher,
    private val inAppMessageResolver: InAppMessageResolver
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        if (userOverrideMatcher.matches(request, context)) {
            return inAppMessageResolver.resolve(request, context, OVERRIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Draft Check
 *
 * IAM이 초안인 경우 IN_APP_MESSAGE_DRAFT
 */
internal class DraftInAppMessageFlowEvaluator : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        if (request.inAppMessage.status == DRAFT) {
            return InAppMessageEvaluation.of(request, context, IN_APP_MESSAGE_DRAFT)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Pause Status Check
 *
 * 현재 Pause 상태인 경우 IN_APP_MESSAGE_PAUSED
 */
internal class PauseInAppMessageFlowEvaluator : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        if (request.inAppMessage.status == PAUSE) {
            return InAppMessageEvaluation.of(request, context, IN_APP_MESSAGE_PAUSED)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Period Check
 *
 * IAM의 기간에 포함되지 않는 경우 NOT_IN_IN_APP_MESSAGE_PERIOD
 */
internal class PeriodInAppMessageFlowEvaluator : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        if (request.timestamp !in request.inAppMessage.period) {
            return InAppMessageEvaluation.of(request, context, NOT_IN_IN_APP_MESSAGE_PERIOD)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Hidden Check
 *
 * SDK에서 판단해서 숨겨야 하는 경우
 * - 하루동안 가리기 설정된 경우
 */
internal class HiddenInAppMessageFlowEvaluator(
    private val hiddenMatcher: InAppMessageHiddenMatcher
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        if (hiddenMatcher.matches(request, context)) {
            return InAppMessageEvaluation.of(request, context, IN_APP_MESSAGE_HIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Target Check
 *
 * IAM 타겟팅이 된 경우
 */
internal class TargetInAppMessageFlowEvaluator(
    private val targetMatcher: InAppMessageTargetMatcher,
    private val inAppMessageResolver: InAppMessageResolver
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation {
        if (targetMatcher.matches(request, context)) {
            return inAppMessageResolver.resolve(request, context, IN_APP_MESSAGE_TARGET)
        }
        return InAppMessageEvaluation.of(request, context, NOT_IN_IN_APP_MESSAGE_TARGET)
    }
}
