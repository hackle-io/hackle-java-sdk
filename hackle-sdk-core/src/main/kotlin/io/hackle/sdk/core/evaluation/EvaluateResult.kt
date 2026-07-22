package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason

interface EvaluateResult {
    val reason: DecisionReason
}
