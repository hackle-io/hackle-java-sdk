package io.hackle.sdk.core

import io.hackle.sdk.core.evaluation.evaluator.DelegatingEvaluator
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

@ExtendWith(MockKExtension::class)
internal class HackleCoreContextTest {

    @BeforeEach
    fun `setup`() {
        HackleCoreContext.clear()
    }

    @Test
    fun `Core 의 인스턴스를 저장한다`() {
        val delegatingEvaluator = DelegatingEvaluator()
        val targetMatcher = TargetMatcher(ConditionMatcherFactory(delegatingEvaluator))

        HackleCoreContext.registerInstance(targetMatcher)

        expectThat(HackleCoreContext.getInstanceMapSize()) {
            isEqualTo(1)
        }
    }

    @Test
    fun `Core 의 인스턴스를 클래스를 통해 가져온다`() {
        val delegatingEvaluator = DelegatingEvaluator()
        val targetMatcher = TargetMatcher(ConditionMatcherFactory(delegatingEvaluator))

        HackleCoreContext.registerInstance(targetMatcher)

        val actual = HackleCoreContext.get(TargetMatcher::class.java)
        expectThat(actual) { isA<TargetMatcher>() }
    }

    @Test
    fun `Core 의 인스턴스를 클래스를 통해 가져온다 - getOrNull`() {
        val delegatingEvaluator = DelegatingEvaluator()
        val targetMatcher = TargetMatcher(ConditionMatcherFactory(delegatingEvaluator))

        HackleCoreContext.registerInstance(targetMatcher)

        val actual = HackleCoreContext.getOrNull(TargetMatcher::class.java)
        expectThat(actual) { isA<TargetMatcher>() }
    }


    @Test
    fun `같은 클래스인 경우 가장 먼저 등록한 인스턴스만 컨텍스트에 등록된다`() {
        val delegatingEvaluator = DelegatingEvaluator()
        val targetMatcher = TargetMatcher(ConditionMatcherFactory(delegatingEvaluator))
        val targetMatcherTwo = TargetMatcher(ConditionMatcherFactory(delegatingEvaluator))

        HackleCoreContext.registerInstance(targetMatcher)
        HackleCoreContext.registerInstance(targetMatcherTwo)

        expectThat(HackleCoreContext.getInstanceMapSize()) { isEqualTo(1) }

    }

    @Test
    fun `없는 클래스 인스턴스 요청시 null 을 반환한다`() {
        val actual: TargetMatcher? = HackleCoreContext.getOrNull(TargetMatcher::class.java)
        expectThat(actual) { isNull() }
    }


    @Test
    fun `getOrNull`() {
        val actual: TargetMatcher? = HackleCoreContext.getOrNull(TargetMatcher::class.java)
        expectThat(actual) { isNull() }
    }

}
