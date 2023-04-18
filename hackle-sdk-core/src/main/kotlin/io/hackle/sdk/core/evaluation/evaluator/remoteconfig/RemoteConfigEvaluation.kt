package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.RemoteConfigParameter

internal class RemoteConfigEvaluation<T> internal constructor(
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val parameter: RemoteConfigParameter,
    val valueId: Long?,
    val value: T,
    val properties: Map<String, Any>
) : Evaluator.Evaluation {
    companion object {
        fun <T : Any> of(
            request: RemoteConfigRequest<T>,
            context: Evaluator.Context,
            valueId: Long?,
            value: T,
            reason: DecisionReason,
            propertiesBuilder: PropertiesBuilder
        ): RemoteConfigEvaluation<T> {
            propertiesBuilder.add("returnValue", value)
            return RemoteConfigEvaluation(
                reason,
                context.targetEvaluations,
                request.parameter,
                valueId,
                value,
                propertiesBuilder.build()
            )
        }

        fun <T : Any> ofDefault(
            request: RemoteConfigRequest<T>,
            context: Evaluator.Context,
            reason: DecisionReason,
            properties: PropertiesBuilder
        ): RemoteConfigEvaluation<T> {
            return of(request, context, null, request.defaultValue, reason, properties)
        }
    }
}
