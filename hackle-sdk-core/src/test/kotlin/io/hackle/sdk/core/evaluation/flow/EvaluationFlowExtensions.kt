package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import strikt.api.Assertion
import strikt.assertions.isA
import strikt.assertions.isSameInstanceAs


internal fun <REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> EvaluationFlow.Companion.create(evaluation: EVALUATION): EvaluationFlow<REQUEST, EVALUATION> {
    return EvaluationFlow.of(object : FlowEvaluator<REQUEST, EVALUATION> {
        override fun evaluate(
            request: REQUEST,
            context: Evaluator.Context,
            nextFlow: EvaluationFlow<REQUEST, EVALUATION>
        ): EVALUATION? {
            return evaluation
        }
    })
}


internal fun <REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> Assertion.Builder<EvaluationFlow<REQUEST, EVALUATION>>.isDecisionWith(
    evaluator: FlowEvaluator<REQUEST, EVALUATION>
): Assertion.Builder<EvaluationFlow<REQUEST, EVALUATION>> {

    return isA<EvaluationFlow.Decision<REQUEST, EVALUATION>>()
        .and { get { flowEvaluator } isSameInstanceAs evaluator }
        .get { nextFlow }
}

internal fun Assertion.Builder<out EvaluationFlow<*, *>>.isEnd() {
    isA<EvaluationFlow.End<*, *>>()
}

internal inline fun <reified EVALUATOR : FlowEvaluator<*, *>> Assertion.Builder<out EvaluationFlow<*, *>>.isDecisionWith(): Assertion.Builder<out EvaluationFlow<*, *>> {

    return isA<EvaluationFlow.Decision<*, *>>()
        .and { get { flowEvaluator }.isA<EVALUATOR>() }
        .get { nextFlow }
}
