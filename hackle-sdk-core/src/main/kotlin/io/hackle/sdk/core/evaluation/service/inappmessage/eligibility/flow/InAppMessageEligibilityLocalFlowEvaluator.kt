package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.set
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local.InAppMessageLayoutLocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local.InAppMessageLayoutLocalEvaluator
import io.hackle.sdk.core.model.InAppMessage.Status.DRAFT
import io.hackle.sdk.core.model.InAppMessage.Status.PAUSE
import io.hackle.sdk.core.model.supports

typealias InAppMessageEligibilityLocalEvaluationFlow = EvaluationFlow<InAppMessageEligibilityLocalEvaluateRequest, InAppMessageEligibilityEvaluateResult>

interface InAppMessageEligibilityLocalFlowEvaluator :
    InAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest> {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult?
}

/**
 * Android platform check
 *
 * 안드로이드를 지원안하면 UNSUPPORTED_PLATFORM
 */
class PlatformInAppMessageEligibilityLocalFlowEvaluator : InAppMessageEligibilityLocalFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        val isAndroidSupport = request.inAppMessage.supports(request.platformType)
        if (!isAndroidSupport) {
            return InAppMessageEligibilityEvaluateResult.ineligible(UNSUPPORTED_PLATFORM)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Specific User Check
 *
 * 테스트 디바이스에서 사용
 */
class OverrideInAppMessageEligibilityLocalFlowEvaluator(
    private val userOverrideMatcher: InAppMessageUserOverrideMatcher,
) : InAppMessageEligibilityLocalFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        val isOverrideMatched = userOverrideMatcher.matches(request, context)
        if (isOverrideMatched) {
            return InAppMessageEligibilityEvaluateResult.eligible(OVERRIDDEN)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Draft Check
 *
 * 초안인지 확인
 */
class DraftInAppMessageEligibilityLocalFlowEvaluator : InAppMessageEligibilityLocalFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        val isDraft = request.inAppMessage.status == DRAFT
        if (isDraft) {
            return InAppMessageEligibilityEvaluateResult.ineligible(IN_APP_MESSAGE_DRAFT)
        }
        return nextFlow.evaluate(request, context)
    }
}


/**
 * Pause Status Check
 *
 * 진행중인지 확인
 */
class PauseInAppMessageEligibilityLocalFlowEvaluator : InAppMessageEligibilityLocalFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        val isPaused = request.inAppMessage.status == PAUSE
        if (isPaused) {
            return InAppMessageEligibilityEvaluateResult.ineligible(IN_APP_MESSAGE_PAUSED)
        }
        return nextFlow.evaluate(request, context)
    }
}

/**
 * Target Check
 *
 * IAM 타겟팅이 된 경우
 */
class TargetInAppMessageEligibilityLocalFlowEvaluator(
    private val targetMatcher: InAppMessageTargetMatcher,
) : InAppMessageEligibilityLocalFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        val isTargetMatched = targetMatcher.matches(request, context)
        if (!isTargetMatched) {
            return InAppMessageEligibilityEvaluateResult.ineligible(NOT_IN_IN_APP_MESSAGE_TARGET)
        }

        return nextFlow.evaluate(request, context)
    }
}

class LayoutResolveInAppMessageEligibilityLocalFlowEvaluator(
    private val layoutEvaluator: InAppMessageLayoutLocalEvaluator,
) : InAppMessageEligibilityLocalFlowEvaluator {
    override fun evaluate(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
        nextFlow: InAppMessageEligibilityLocalEvaluationFlow,
    ): InAppMessageEligibilityEvaluateResult? {
        val layoutRequest = InAppMessageLayoutLocalEvaluateRequest.of(request)
        val layoutEvaluation = layoutEvaluator.evaluate(layoutRequest, Evaluators.context())
        context.set(layoutEvaluation)

        return nextFlow.evaluate(request, context)
    }
}
