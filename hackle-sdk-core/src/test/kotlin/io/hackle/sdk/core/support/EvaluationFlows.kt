package io.hackle.sdk.core.support

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.FlowEvaluator
import strikt.api.Assertion
import strikt.assertions.isA
import strikt.assertions.isSameInstanceAs

internal fun <REQUEST : EvaluateRequest, RESULT : EvaluateResult> EvaluationFlow.Companion.create(
    result: RESULT,
): EvaluationFlow<REQUEST, RESULT> {
    return EvaluationFlow.of(FixedFlowEvaluator(result))
}

internal class FixedFlowEvaluator<REQUEST : EvaluateRequest, RESULT : EvaluateResult>(
    private val result: RESULT,
) : FlowEvaluator<REQUEST, RESULT> {
    override fun evaluate(
        request: REQUEST,
        context: Evaluator.Context,
        nextFlow: EvaluationFlow<REQUEST, RESULT>,
    ): RESULT {
        return result
    }
}

internal fun <REQUEST : EvaluateRequest, RESULT : EvaluateResult> Assertion.Builder<EvaluationFlow<REQUEST, RESULT>>.isStepWith(
    evaluator: FlowEvaluator<REQUEST, RESULT>,
): Assertion.Builder<EvaluationFlow<REQUEST, RESULT>> {

    return isA<EvaluationFlow.Step<REQUEST, RESULT>>()
        .and { get { flowEvaluator } isSameInstanceAs evaluator }
        .get { nextFlow }
}

internal fun Assertion.Builder<out EvaluationFlow<*, *>>.isEnd() {
    isA<EvaluationFlow.End<*, *>>()
}

internal inline fun <reified EVALUATOR : FlowEvaluator<*, *>> Assertion.Builder<out EvaluationFlow<*, *>>.isStepWith(): Assertion.Builder<out EvaluationFlow<*, *>> {

    return isA<EvaluationFlow.Step<*, *>>()
        .and { get { flowEvaluator }.isA<EVALUATOR>() }
        .get { nextFlow }
}
