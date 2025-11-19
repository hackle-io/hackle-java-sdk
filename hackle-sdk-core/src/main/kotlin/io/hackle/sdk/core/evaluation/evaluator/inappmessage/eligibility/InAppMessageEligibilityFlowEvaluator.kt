package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutEvaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutRequest
import io.hackle.sdk.core.evaluation.evaluator.set
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.FlowEvaluator
import io.hackle.sdk.core.evaluation.target.InAppMessageFrequencyCapMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.model.InAppMessage.PlatformType.ANDROID
import io.hackle.sdk.core.model.InAppMessage.Status.DRAFT
import io.hackle.sdk.core.model.InAppMessage.Status.PAUSE
import io.hackle.sdk.core.model.contains
import io.hackle.sdk.core.model.supports

typealias InAppMessageEligibilityFlow = EvaluationFlow<InAppMessageEligibilityRequest, InAppMessageEligibilityEvaluation>

internal interface InAppMessageEligibilityFlowEvaluator :
    FlowEvaluator<InAppMessageEligibilityRequest, InAppMessageEligibilityEvaluation> {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation?
}

/**
 * Android platform check
 *
 * 안드로이드를 지원안하면 UNSUPPORTED_PLATFORM
 */
internal class PlatformInAppMessageEligibilityFlowEvaluator : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isAndroidSupport = request.inAppMessage.supports(ANDROID)
        if (!isAndroidSupport) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, UNSUPPORTED_PLATFORM)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Specific User Check
 *
 * 테스트 디바이스에서 사용
 */
internal class OverrideInAppMessageEligibilityFlowEvaluator(
    private val userOverrideMatcher: InAppMessageUserOverrideMatcher,
) : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isOverrideMatched = userOverrideMatcher.matches(request, context)
        if (isOverrideMatched) {
            return InAppMessageEligibilityEvaluation.eligible(request, context, OVERRIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Draft Check
 *
 * 초안인지 확인
 */
internal class DraftInAppMessageEligibilityFlowEvaluator : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isDraft = request.inAppMessage.status == DRAFT
        if (isDraft) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, IN_APP_MESSAGE_DRAFT)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Pause Status Check
 *
 * 진행중인지 확인
 */
internal class PauseInAppMessageEligibilityFlowEvaluator : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isPaused = request.inAppMessage.status == PAUSE
        if (isPaused) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, IN_APP_MESSAGE_PAUSED)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Period Check
 *
 * IAM의 기간에 포함되지 않는 경우 NOT_IN_IN_APP_MESSAGE_PERIOD
 */
internal class PeriodInAppMessageEligibilityFlowEvaluator : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isWithinPeriod = request.inAppMessage.period.contains(request.timestamp)
        if (!isWithinPeriod) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, NOT_IN_IN_APP_MESSAGE_PERIOD)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Timetable Check
 *
 * IAM의 시간표에 포함되지 않는 경우 NOT_IN_IN_APP_MESSAGE_TIMETABLE
 */
internal class TimetableInAppMessageEligibilityFlowEvaluator : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isWithinTimetable = request.inAppMessage.timetable.within(request.timestamp)
        if (!isWithinTimetable) {
            return InAppMessageEligibilityEvaluation.ineligible(
                request,
                context,
                NOT_IN_IN_APP_MESSAGE_TIMETABLE
            )
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Target Check
 *
 * IAM 타겟팅이 된 경우
 */
internal class TargetInAppMessageEligibilityFlowEvaluator(
    private val targetMatcher: InAppMessageTargetMatcher,
) : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isTargetMatched = targetMatcher.matches(request, context)
        if (!isTargetMatched) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, NOT_IN_IN_APP_MESSAGE_TARGET)
        }

        return nextFlow.evaluate(request, context)
    }
}

internal class LayoutResolveInAppMessageEligibilityFlowEvaluator(
    private val layoutEvaluator: InAppMessageLayoutEvaluator,
) : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {

        val layoutRequest = InAppMessageLayoutRequest.of(request)
        val layoutEvaluation = layoutEvaluator.evaluate(layoutRequest, Evaluators.context())
        context.set(layoutEvaluation)

        return nextFlow.evaluate(request, context)
    }
}

/**
 * 노출 빈도수 체크
 */
internal class FrequencyCapInAppMessageEligibilityFlowEvaluator(
    private val frequencyCapMatcher: InAppMessageFrequencyCapMatcher,
) : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isFrequencyCapped = frequencyCapMatcher.matches(request, context)
        if (isFrequencyCapped) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, IN_APP_MESSAGE_FREQUENCY_CAPPED)
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
internal class HiddenInAppMessageEligibilityFlowEvaluator(
    private val hiddenMatcher: InAppMessageHiddenMatcher,
) : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation? {
        val isHidden = hiddenMatcher.matches(request, context)
        if (isHidden) {
            return InAppMessageEligibilityEvaluation.ineligible(request, context, IN_APP_MESSAGE_HIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

internal class EligibleInAppMessageEligibilityFlowEvaluator : InAppMessageEligibilityFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityFlow,
    ): InAppMessageEligibilityEvaluation {
        return InAppMessageEligibilityEvaluation.eligible(request, context, IN_APP_MESSAGE_TARGET)
    }
}
