package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.common.PropertiesBuilder
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

internal class RemoteConfigEvaluationTest {
    @Test
    fun `create`() {
        val parameter = mockk<RemoteConfigParameter> {
            every { id } returns 1
        }
        val request = RemoteConfigRequest(mockk(), mockk(), parameter, mockk(), mockk())

        val context = Evaluators.context()
        context.add(mockk<Evaluator.Evaluation>())
        val evaluation = RemoteConfigEvaluation.of(
            request,
            context,
            42,
            "go",
            DecisionReason.DEFAULT_RULE,
            PropertiesBuilder()
        )

        expectThat(evaluation) {
            get { this.reason } isEqualTo DecisionReason.DEFAULT_RULE
            get { this.targetEvaluations }.hasSize(1)
            get { this.parameter } isSameInstanceAs parameter
            get { properties["returnValue"] } isEqualTo "go"
        }
    }
}