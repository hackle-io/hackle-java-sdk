package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class Evaluator(
    private val evaluationFlowFactory: EvaluationFlowFactory,
) {
    fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String
    ): Evaluation {
        val evaluationFlow = evaluationFlowFactory.getFlow(experiment.type)
        return evaluationFlow.evaluate(workspace, experiment, user, defaultVariationKey)
    }

    fun <T : Any> evaluate(
        workspace: Workspace,
        parameter: RemoteConfigParameter,
        user: HackleUser,
        requiredType: ValueType,
        defaultValue: T,
    ): RemoteConfigEvaluation<T> {

        val propertiesBuilder = PropertiesBuilder()
            .add("request.valueType", requiredType.name)
            .add("request.defaultValue", defaultValue as Any)

        if (user.identifiers[parameter.identifierType] == null) {
            return RemoteConfigEvaluation.of(null, defaultValue, IDENTIFIER_NOT_FOUND, propertiesBuilder)
        }

        val targetRuleDeterminer = evaluationFlowFactory.remoteConfigParameterTargetRuleDeterminer
        val targetRule = targetRuleDeterminer.determineTargetRuleOrNull(workspace, parameter, user)
        if (targetRule != null) {
            propertiesBuilder.add("targetRuleKey", targetRule.key)
            propertiesBuilder.add("targetRuleName", targetRule.name)
            return evaluation(targetRule.value, TARGET_RULE_MATCH, requiredType, defaultValue, propertiesBuilder)
        }

        return evaluation(parameter.defaultValue, DEFAULT_RULE, requiredType, defaultValue, propertiesBuilder)
    }

    private fun <T : Any> evaluation(
        parameterValue: RemoteConfigParameter.Value,
        reason: DecisionReason,
        requiredType: ValueType,
        defaultValue: T,
        propertiesBuilder: PropertiesBuilder,
    ): RemoteConfigEvaluation<T> {

        @Suppress("UNCHECKED_CAST")
        val value = when (requiredType) {
            ValueType.STRING -> parameterValue.rawValue as? String
            ValueType.NUMBER -> parameterValue.rawValue as? Number
            ValueType.BOOLEAN -> parameterValue.rawValue as? Boolean
            ValueType.VERSION, ValueType.JSON -> null
        } as? T

        return if (value != null) {
            RemoteConfigEvaluation.of(parameterValue.id, value, reason, propertiesBuilder)
        } else {
            RemoteConfigEvaluation.of(null, defaultValue, TYPE_MISMATCH, propertiesBuilder)
        }
    }
}
