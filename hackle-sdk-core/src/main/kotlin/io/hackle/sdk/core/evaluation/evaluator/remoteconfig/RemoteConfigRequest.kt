package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.core.evaluation.evaluator.AbstractEvaluatorRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class RemoteConfigRequest<T : Any>(
    override val workspace: Workspace,
    override val user: HackleUser,
    override val by: Evaluator.Request?,
    val parameter: RemoteConfigParameter,
    val requiredType: ValueType,
    val defaultValue: T,
) : AbstractEvaluatorRequest() {
    override val key: Evaluator.Key = Evaluator.Key(Evaluator.Type.REMOTE_CONFIG, parameter.id)
}