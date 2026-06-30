package io.hackle.sdk.core.evaluation.mode.local

import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.workspace.config.entity.ConfigEntity

abstract class ReferenceLocalEvaluator<REFERENCE : ConfigEntity, EVALUATION : Evaluation> {
    fun evaluate(sourceRequest: LocalEvaluateRequest, context: Evaluator.Context, reference: REFERENCE): EVALUATION {
        val evaluation = context[reference]
        if (evaluation != null) {
            @Suppress("UNCHECKED_CAST")
            return evaluation as EVALUATION
        }

        return doEvaluate(sourceRequest, context, reference)
            .also { context.add(it) }
    }

    protected abstract fun doEvaluate(
        sourceRequest: LocalEvaluateRequest,
        context: Evaluator.Context,
        reference: REFERENCE,
    ): EVALUATION
}
