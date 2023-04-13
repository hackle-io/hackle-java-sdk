package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator

internal class RemoteConfigEvaluation<T> private constructor(
    override val reason: DecisionReason,
    override val context: Evaluator.Context,
    val valueId: Long?,
    val value: T,
    val properties: Map<String, Any> = emptyMap()
) : Evaluator.Evaluation {
    companion object {
        fun <T : Any> of(
            context: Evaluator.Context,
            valueId: Long?,
            value: T,
            reason: DecisionReason,
            propertiesBuilder: PropertiesBuilder
        ): RemoteConfigEvaluation<T> {
            propertiesBuilder.add("returnValue", value)
            return RemoteConfigEvaluation(reason, context, valueId, value, propertiesBuilder.build())
        }

        fun <T : Any> ofDefault(
            request: RemoteConfigRequest<T>,
            context: Evaluator.Context,
            reason: DecisionReason,
            parameters: PropertiesBuilder
        ): RemoteConfigEvaluation<T> {
            return of(context, null, request.defaultValue, reason, parameters)
        }
    }
}
