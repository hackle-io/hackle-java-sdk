package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResult
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluation
import io.hackle.sdk.core.model.AbstractRemoteConfigParameter
import io.hackle.sdk.core.model.Entity
import io.hackle.sdk.core.model.ValueType

class RemoteConfigParameterRemoteEvaluateResult(
    override val id: Long,
    override val key: String,
    override val type: ValueType,
    override val value: Any,
    override val valueId: Long?,
    override val reason: DecisionReason,
    override val references: List<Entity>,
) : AbstractRemoteConfigParameter(),
    RemoteConfigEvaluateResult<Any>,
    RemoteEvaluateResult {
    override fun toEvaluation(): Evaluation {
        return RemoteConfigEvaluation(this, this, emptyMap())
    }
}
