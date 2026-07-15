package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResult
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleDeterminer

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
            val result = RemoteConfigEvaluateResult.of(request, targetRule.value, TARGET_RULE_MATCH)
            return RemoteConfigEvaluateResponse.of(request, context, result)
        }

        val result = RemoteConfigEvaluateResult.of(request, request.entity.defaultValue, DEFAULT_RULE)
        return RemoteConfigEvaluateResponse.of(request, context, result)
    }

    override fun record(
        request: RemoteConfigLocalEvaluateRequest,
        response: RemoteConfigEvaluateResponse,
    ) {
        eventRecorder.record(response)
    }
}
