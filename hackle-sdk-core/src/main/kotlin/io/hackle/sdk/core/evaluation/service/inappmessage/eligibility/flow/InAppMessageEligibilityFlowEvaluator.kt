package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.FlowEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageFrequencyCapMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageHiddenMatcher
import io.hackle.sdk.core.model.contains


interface InAppMessageEligibilityFlowEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest> :
    FlowEvaluator<REQUEST, InAppMessageEligibilityEvaluateResult> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult?
}

/**
 * Period Check
 *
 * IAM의 기간에 포함되지 않는 경우 NOT_IN_IN_APP_MESSAGE_PERIOD
 */
class PeriodInAppMessageEligibilityFlowEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest> :
    InAppMessageEligibilityFlowEvaluator<REQUEST> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult? {
        val isWithinPeriod = request.inAppMessage.period.contains(request.timestamp)
        if (!isWithinPeriod) {
            return InAppMessageEligibilityEvaluateResult.ineligible(NOT_IN_IN_APP_MESSAGE_PERIOD)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Timetable Check
 *
 * IAM의 시간표에 포함되지 않는 경우 NOT_IN_IN_APP_MESSAGE_TIMETABLE
 */
class TimetableInAppMessageEligibilityFlowEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest> :
    InAppMessageEligibilityFlowEvaluator<REQUEST> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult? {
        val isWithinTimetable = request.inAppMessage.timetable.within(request.timestamp)
        if (!isWithinTimetable) {
            return InAppMessageEligibilityEvaluateResult.ineligible(NOT_IN_IN_APP_MESSAGE_TIMETABLE)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * 노출 빈도수 체크
 */
class FrequencyCapInAppMessageEligibilityFlowEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest>(
    private val frequencyCapMatcher: InAppMessageFrequencyCapMatcher,
) : InAppMessageEligibilityFlowEvaluator<REQUEST> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult? {
        val isFrequencyCapped = frequencyCapMatcher.matches(request, context)
        if (isFrequencyCapped) {
            return InAppMessageEligibilityEvaluateResult.ineligible(IN_APP_MESSAGE_FREQUENCY_CAPPED)
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
class HiddenInAppMessageEligibilityFlowEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest>(
    private val hiddenMatcher: InAppMessageHiddenMatcher,
) : InAppMessageEligibilityFlowEvaluator<REQUEST> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult? {
        val isHidden = hiddenMatcher.matches(request, context)
        if (isHidden) {
            return InAppMessageEligibilityEvaluateResult.ineligible(IN_APP_MESSAGE_HIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

class EligibleInAppMessageEligibilityFlowEvaluator<REQUEST : InAppMessageEligibilityEvaluateRequest> :
    InAppMessageEligibilityFlowEvaluator<REQUEST> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, InAppMessageEligibilityEvaluateResult>,
    ): InAppMessageEligibilityEvaluateResult {
        return InAppMessageEligibilityEvaluateResult.eligible(IN_APP_MESSAGE_TARGET)
    }
}
