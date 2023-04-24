package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.target.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType.*

internal class RemoteConfigEvaluator<T : Any>(
    private val targetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer
) : AbstractEvaluator<RemoteConfigRequest<T>, RemoteConfigEvaluation<T>>() {

    override fun supports(request: Evaluator.Request): Boolean {
        return request is RemoteConfigRequest<*>
    }

    override fun evaluateInternal(
        request: RemoteConfigRequest<T>,
        context: Evaluator.Context
    ): RemoteConfigEvaluation<T> {
        val propertiesBuilder = PropertiesBuilder()
            .add("requestValueType", request.requiredType.name)
            .add("requestDefaultValue", request.defaultValue)

        if (request.user.identifiers[request.parameter.identifierType] == null) {
            return RemoteConfigEvaluation.ofDefault(request, context, IDENTIFIER_NOT_FOUND, propertiesBuilder)
        }

        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(request, context)
        if (targetRule != null) {
            propertiesBuilder.add("targetRuleKey", targetRule.key)
            propertiesBuilder.add("targetRuleName", targetRule.name)
            return evaluation(request, context, targetRule.value, TARGET_RULE_MATCH, propertiesBuilder)
        }

        return evaluation(request, context, request.parameter.defaultValue, DEFAULT_RULE, propertiesBuilder)
    }

    private fun <T : Any> evaluation(
        request: RemoteConfigRequest<T>,
        context: Evaluator.Context,
        parameterValue: RemoteConfigParameter.Value,
        reason: DecisionReason,
        propertiesBuilder: PropertiesBuilder,
    ): RemoteConfigEvaluation<T> {

        @Suppress("UNCHECKED_CAST")
        val value = when (request.requiredType) {
            STRING -> parameterValue.rawValue as? String
            NUMBER -> parameterValue.rawValue as? Number
            BOOLEAN -> parameterValue.rawValue as? Boolean
            VERSION, JSON -> null
        } as? T

        return if (value != null) {
            RemoteConfigEvaluation.of(request, context, parameterValue.id, value, reason, propertiesBuilder)
        } else {
            RemoteConfigEvaluation.ofDefault(request, context, TYPE_MISMATCH, propertiesBuilder)
        }
    }
}
