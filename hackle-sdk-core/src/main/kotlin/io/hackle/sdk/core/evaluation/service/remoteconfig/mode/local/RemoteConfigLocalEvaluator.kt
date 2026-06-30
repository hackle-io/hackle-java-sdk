package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.IDENTIFIER_NOT_FOUND
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResult
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.cast

internal class RemoteConfigLocalEvaluator<T : Any>(
    private val targetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer,
    private val eventRecorder: EvaluationEventRecorder,
) : LocalEvaluator<RemoteConfigLocalEvaluateRequest<T>, RemoteConfigEvaluateResponse<T>>(),
    RemoteConfigEvaluator<T, RemoteConfigLocalEvaluateRequest<T>> {
    override fun supports(request: EvaluateRequest): Boolean {
        return request is RemoteConfigLocalEvaluateRequest<*>
    }

    override fun doEvaluate(
        request: RemoteConfigLocalEvaluateRequest<T>,
        context: Evaluator.Context,
    ): RemoteConfigEvaluateResponse<T> {

        if (request.user.identifiers[request.entity.identifierType] == null) {
            val result = RemoteConfigEvaluateResult.of(IDENTIFIER_NOT_FOUND, request.defaultValue, null)
            return RemoteConfigEvaluateResponse.of(request, context, result)
        }

        val targetRule = targetRuleDeterminer.determine(request, context)
        if (targetRule != null) {
            val result = result(request, targetRule.value, DecisionReason.TARGET_RULE_MATCH)
            return RemoteConfigEvaluateResponse.of(request, context, result)
        }

        val result = result(request, request.entity.defaultValue, DecisionReason.DEFAULT_RULE)
        return RemoteConfigEvaluateResponse.of(request, context, result)
    }

    private fun <T : Any> result(
        request: RemoteConfigLocalEvaluateRequest<T>,
        value: RemoteConfigParameter.Value,
        reason: DecisionReason,
    ): RemoteConfigEvaluateResult<T> {
        val typedValue = request.requiredType.cast<T>(value)
        return if (typedValue != null) {
            RemoteConfigEvaluateResult.of(reason, typedValue, value.id)
        } else {
            RemoteConfigEvaluateResult.of(DecisionReason.TYPE_MISMATCH, request.defaultValue, null)
        }
    }

    override fun record(
        request: RemoteConfigLocalEvaluateRequest<T>,
        response: RemoteConfigEvaluateResponse<T>,
    ) {
        eventRecorder.record(response)
    }
}
