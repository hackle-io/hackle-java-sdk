package io.hackle.sdk.core.evaluation.service.experiment

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

interface ExperimentEvaluator<REQUEST : ExperimentEvaluateRequest> : Evaluator<REQUEST, ExperimentEvaluateResponse>
