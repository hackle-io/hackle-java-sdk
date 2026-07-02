package io.hackle.sdk.core.evaluation.service.experiment.mode.local

import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig

class ExperimentLocalEvaluateRequest(
    override val workspace: WorkspaceConfig,
    override val entity: ExperimentConfig,
    override val user: HackleUser,
    override val record: Boolean,
) : LocalEvaluateRequest(), ExperimentEvaluateRequest {
    val experiment: ExperimentConfig get() = entity

    companion object {
        fun of(
            workspace: WorkspaceConfig,
            experiment: ExperimentConfig,
            user: HackleUser,
            record: Boolean = true,
        ): ExperimentLocalEvaluateRequest {
            return ExperimentLocalEvaluateRequest(workspace, experiment, user, record)
        }

        fun of(request: LocalEvaluateRequest, experiment: ExperimentConfig): ExperimentLocalEvaluateRequest {
            return ExperimentLocalEvaluateRequest(
                workspace = request.workspace,
                entity = experiment,
                user = request.user,
                record = request.record,
            )
        }
    }
}
