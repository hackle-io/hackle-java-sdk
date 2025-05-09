package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.FlowEvaluator
import io.hackle.sdk.core.evaluation.target.*
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessage.PlatformType.ANDROID
import io.hackle.sdk.core.model.InAppMessage.Status.DRAFT
import io.hackle.sdk.core.model.InAppMessage.Status.PAUSE
import io.hackle.sdk.core.model.contains
import io.hackle.sdk.core.model.supports

internal typealias InAppMessageFlow = EvaluationFlow<InAppMessageRequest, InAppMessageEvaluation>

internal interface InAppMessageFlowEvaluator :
    FlowEvaluator<InAppMessageRequest, InAppMessageEvaluation> {
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
        val isAndroidSupport = request.inAppMessage.supports(ANDROID)
        if (!isAndroidSupport) {
            return InAppMessageEvaluation.of(request, context, UNSUPPORTED_PLATFORM)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Specific User Check
 *
 * 테스트 디바이스에서 사용
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
        val isOverrideMatched = userOverrideMatcher.matches(request, context)
        if (isOverrideMatched) {
            return inAppMessageResolver.resolve(request, context, OVERRIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Draft Check
 *
 * 초안인지 확인
 */
internal class DraftInAppMessageFlowEvaluator : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        val isDraft = request.inAppMessage.status == DRAFT
        if (isDraft) {
            return InAppMessageEvaluation.of(request, context, IN_APP_MESSAGE_DRAFT)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Pause Status Check
 *
 * 진행중인지 확인
 */
internal class PauseInAppMessageFlowEvaluator : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        val isPaused = request.inAppMessage.status == PAUSE
        if (isPaused) {
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
        val isWithinPeriod = request.inAppMessage.period.contains(request.timestamp)
        if (!isWithinPeriod) {
            return InAppMessageEvaluation.of(request, context, NOT_IN_IN_APP_MESSAGE_PERIOD)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Hidden Check
 *
 * SDK 에서 판단해서 숨겨야 하는 경우
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
        val isHidden = hiddenMatcher.matches(request, context)
        if (isHidden) {
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
    private val targetMatcher: InAppMessageTargetMatcher
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        val isTargetMatched = targetMatcher.matches(request, context)
        if (!isTargetMatched) {
            return InAppMessageEvaluation.of(request, context, NOT_IN_IN_APP_MESSAGE_TARGET)
        }

        return nextFlow.evaluate(request, context)
    }
}

/**
 * 노출 빈도수 체크
 */
internal class FrequencyCapInAppMessageFlowEvaluator(
    private val frequencyCapMatcher: InAppMessageFrequencyCapMatcher
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        val isFrequencyCapped = frequencyCapMatcher.matches(request, context)
        if (isFrequencyCapped) {
            return InAppMessageEvaluation.of(request, context, IN_APP_MESSAGE_FREQUENCY_CAPPED)
        }

        return nextFlow.evaluate(request, context)
    }
}

/**
 * IAM ABTest
 */
internal class ExperimentInAppMessageFlowEvaluator(
    private val inAppMessageResolver: InAppMessageResolver
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        val message = inAppMessageResolver.resolve(request, context)
        val isControllerGroup = message.layout.displayType == InAppMessage.DisplayType.NONE
        if (isControllerGroup) {
            return InAppMessageEvaluation.of(request, context, EXPERIMENT_CONTROL_GROUP)
        }

        return nextFlow.evaluate(request, context)
    }
}

/**
 * IAM Message check
 */
internal class MessageResolutionInAppMessageFlowEvaluator(
    private val inAppMessageResolver: InAppMessageResolver
) : InAppMessageFlowEvaluator {
    override fun evaluate(
        request: InAppMessageRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageFlow
    ): InAppMessageEvaluation? {
        val message = inAppMessageResolver.resolve(request, context)
        return InAppMessageEvaluation.of(request, context, IN_APP_MESSAGE_TARGET, message)
    }
}
