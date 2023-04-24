package io.hackle.sdk.core.evaluation.evaluator

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

internal class DelegatingEvaluatorTest {

    @Test
    fun `evaluate`() {

        val sut = DelegatingEvaluator()

        val r1 = mockk<Evaluator.Request>()
        val e1 = mockk<Evaluator.Evaluation>()
        val evaluator1 = mockk<AbstractEvaluator<Evaluator.Request, Evaluator.Evaluation>> {
            every { supports(eq(r1)) } returns true
            every { supports(neq(r1)) } returns false
            every { evaluate(r1, any()) } returns e1
        }

        sut.add(evaluator1)
        assertThrows<IllegalArgumentException> { sut.evaluate(mockk(), mockk()) }
        expectThat(sut.evaluate(r1, Evaluators.context())) isSameInstanceAs e1

        val r2 = mockk<Evaluator.Request>()
        val e2 = mockk<Evaluator.Evaluation>()
        val evaluator2 = mockk<AbstractEvaluator<Evaluator.Request, Evaluator.Evaluation>> {
            every { supports(eq(r2)) } returns true
            every { supports(neq(r2)) } returns false
            every { evaluate(r2, any()) } returns e2
        }
        sut.add(evaluator2)
        expectThat(sut.evaluate(r2, Evaluators.context())) isSameInstanceAs e2
    }
}