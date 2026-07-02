package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResult
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.isInstance

internal class RemoteConfigLocalEvaluator(
    private val targetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer,
    private val eventRecorder: EvaluationEventRecorder,
) : LocalEvaluator<RemoteConfigLocalEvaluateRequest, RemoteConfigEvaluateResponse>(),
    RemoteConfigEvaluator<RemoteConfigLocalEvaluateRequest> {
    override fun supports(request: EvaluateRequest): Boolean {
        return request is RemoteConfigLocalEvaluateRequest
    }

    override fun doEvaluate(
        request: RemoteConfigLocalEvaluateRequest,
        context: Evaluator.Context,
    ): RemoteConfigEvaluateResponse {
        if (request.user.identifiers[request.entity.identifierType] == null) {
            val result = RemoteConfigEvaluateResult.of(IDENTIFIER_NOT_FOUND, null)
            return RemoteConfigEvaluateResponse.of(request, context, result)
        }

        val targetRule = targetRuleDeterminer.determine(request, context)
        if (targetRule != null) {
            val result = result(request, targetRule.value, TARGET_RULE_MATCH)
            return RemoteConfigEvaluateResponse.of(request, context, result)
        }

        val result = result(request, request.entity.defaultValue, DEFAULT_RULE)
        return RemoteConfigEvaluateResponse.of(request, context, result)
    }

    private fun result(
        request: RemoteConfigLocalEvaluateRequest,
        value: RemoteConfigParameter.Value,
        reason: DecisionReason,
    ): RemoteConfigEvaluateResult {
        return if (request.requiredType.isInstance(value)) {
            RemoteConfigEvaluateResult.of(reason, value)
        } else {
            RemoteConfigEvaluateResult.of(TYPE_MISMATCH, value)
        }
    }

    override fun record(
        request: RemoteConfigLocalEvaluateRequest,
        response: RemoteConfigEvaluateResponse,
    ) {
        eventRecorder.record(response)
    }
}
