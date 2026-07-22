package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment

class ExperimentEvaluation(
    override val entity: Experiment,
    override val result: ExperimentEvaluateResult,
) : Evaluation
