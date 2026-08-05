package io.hackle.sdk.core.evaluation.service.inappmessage.layout

import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutEvaluation(
    override val entity: InAppMessage,
    override val result: InAppMessageLayoutEvaluateResult,
) : Evaluation
