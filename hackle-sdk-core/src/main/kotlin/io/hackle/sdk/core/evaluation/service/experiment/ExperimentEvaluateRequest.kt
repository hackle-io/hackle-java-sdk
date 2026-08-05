package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.model.Experiment

interface ExperimentEvaluateRequest : EvaluateRequest {
    override val entity: Experiment
}
