package io.hackle.sdk.core.evaluation.service.experiment.mode.remote

import io.hackle.sdk.core.evaluation.mode.remote.RemoteEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluation
import io.hackle.sdk.core.workspace.evaluation.entity.ExperimentRemoteEvaluateResult

class ExperimentRemoteEvaluateRequest(
    override val workspace: WorkspaceEvaluation,
    override val entity: ExperimentRemoteEvaluateResult,
    override val user: HackleUser,
    override val record: Boolean,
) : RemoteEvaluateRequest(), ExperimentEvaluateRequest {
    companion object {
        fun of(
            workspace: WorkspaceEvaluation,
            experiment: ExperimentRemoteEvaluateResult,
            user: HackleUser,
            record: Boolean = true,
        ): ExperimentRemoteEvaluateRequest {
            return ExperimentRemoteEvaluateRequest(
                workspace = workspace,
                entity = experiment,
                user = user,
                record = record
            )
        }

        fun of(
            request: RemoteEvaluateRequest,
            experiment: ExperimentRemoteEvaluateResult,
        ): ExperimentRemoteEvaluateRequest {
            return ExperimentRemoteEvaluateRequest(
                workspace = request.workspace,
                entity = experiment,
                user = request.user,
                record = request.record
            )
        }
    }
}
