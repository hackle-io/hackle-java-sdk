package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

internal class DefaultContextTest {

    @Test
    fun `stack - 요청을 추가하고 제거한다`() {

        val context = Evaluators.context()
        expectThat(context.stack).hasSize(0)

        val request1 = Experiments.localRequest(experiment = Experiments.config(id = 1))
        context.add(request1)
        val stack1 = context.stack
        expectThat(stack1).hasSize(1)
        expectThat(request1 in context).isTrue()

        val request2 = Experiments.localRequest(experiment = Experiments.config(id = 2))
        context.add(request2)
        val stack2 = context.stack
        expectThat(stack2).hasSize(2)

        context.remove(request2)
        expectThat(context.stack).hasSize(1)

        context.remove(request1)
        expectThat(context.stack).hasSize(0)
        expectThat(request1 in context).isFalse()

        expectThat(stack1).hasSize(1)
        expectThat(stack2).hasSize(2)
    }

    @Test
    fun `references - 평가를 추가하고 entity 로 조회한다`() {

        val context = Evaluators.context()
        expectThat(context.references).hasSize(0)

        val experiment = Experiments.config(id = 1)

        val evaluation1 = RemoteConfigs.evaluation()
        context.add(evaluation1)
        val references1 = context.references
        expectThat(references1).hasSize(1)
        expectThat(context[experiment]).isNull()

        val evaluation2 = Experiments.evaluation(entity = experiment)
        context.add(evaluation2)
        val references2 = context.references
        expectThat(references1).hasSize(1)
        expectThat(references2).hasSize(2)
        expectThat(context[experiment]) isSameInstanceAs evaluation2

        expectThat(context[Experiments.config(id = 2)]).isNull()
    }

    @Test
    fun `class key 로 값을 저장하고 조회한다`() {
        val context = Evaluators.context()
        expectThat(context.get<String>()).isNull()

        context.set("hello")
        expectThat(context.get<String>()) isEqualTo "hello"

        context.set("world")
        expectThat(context.get<String>()) isEqualTo "world"
    }

    @Test
    fun `class key 에 저장된 값의 타입이 key 와 다르면 예외 발생`() {
        val context = Evaluators.context()

        @Suppress("UNCHECKED_CAST")
        context.set(Long::class.javaObjectType as Class<Any>, "not a long")

        assertThrows<NoSuchElementException> {
            context.get(Long::class.javaObjectType)
        }
    }
}
