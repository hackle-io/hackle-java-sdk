package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class ExperimentRequest(
    override val workspace: Workspace,
    override val user: HackleUser,
    val experiment: Experiment,
    val defaultVariationKey: String
) : Evaluator.Request
