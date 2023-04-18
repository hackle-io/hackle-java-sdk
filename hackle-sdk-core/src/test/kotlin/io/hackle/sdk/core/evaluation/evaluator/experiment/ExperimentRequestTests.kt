package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.Variation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.Workspace
import java.util.*

internal fun experimentRequest(
    workspace: Workspace = io.hackle.sdk.core.workspace.workspace { },
    user: HackleUser = HackleUser.builder().identifier(IdentifierType.ID, UUID.randomUUID().toString()).build(),
    experiment: Experiment = experiment(),
    defaultVariation: Variation = Variation.CONTROL
): ExperimentRequest {
    return ExperimentRequest.of(workspace, user, experiment, defaultVariation)
}