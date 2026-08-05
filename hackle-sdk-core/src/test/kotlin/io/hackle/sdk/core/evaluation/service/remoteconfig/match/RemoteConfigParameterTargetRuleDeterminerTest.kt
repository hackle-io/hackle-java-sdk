package io.hackle.sdk.core.evaluation.service.remoteconfig.match

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.support.RemoteConfigs
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.bucket
import io.hackle.sdk.core.support.slot
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
internal class RemoteConfigParameterTargetRuleDeterminerTest {

    @MockK
    private lateinit var matcher: RemoteConfigParameterTargetRuleMatcher

    @InjectMockKs
    private lateinit var sut: RemoteConfigParameterTargetRuleDeterminer

    @Test
    fun `첫번째로 매치되는 룰을 리턴한다`() {
        // given
        val matchedTargetRule = targetRule("rule_4", true)
        val parameter = RemoteConfigs.config(
            targetRules = listOf(
                targetRule("rule_1", false),
                targetRule("rule_2", false),
                targetRule("rule_3", false),
                matchedTargetRule,
                targetRule("rule_5", false),
                targetRule("rule_6", false),
            )
        )
        val request = RemoteConfigs.localRequest(parameter = parameter)

        // when
        val actual = sut.determine(request, Evaluators.context())

        // then
        expectThat(actual) isSameInstanceAs matchedTargetRule
        verify(exactly = 4) { matcher.matches(any(), any(), any()) }
    }

    @Test
    fun `매치되는 룰이 없으면 null 리턴`() {
        // given
        val parameter = RemoteConfigs.config(
            targetRules = listOf(
                targetRule("rule_1", false),
                targetRule("rule_2", false),
                targetRule("rule_3", false),
                targetRule("rule_4", false),
                targetRule("rule_5", false),
            )
        )
        val request = RemoteConfigs.localRequest(parameter = parameter)

        // when
        val actual = sut.determine(request, Evaluators.context())

        // then
        expectThat(actual).isNull()
        verify(exactly = 5) { matcher.matches(any(), any(), any()) }
    }

    @Test
    fun `TargetRule 이 없으면 null 리턴`() {
        // given
        val parameter = RemoteConfigs.config(targetRules = emptyList())
        val request = RemoteConfigs.localRequest(parameter = parameter)

        // when
        val actual = sut.determine(request, Evaluators.context())

        // then
        expectThat(actual).isNull()
        verify { matcher wasNot Called }
    }

    private fun targetRule(key: String, isMatch: Boolean): RemoteConfigParameter.TargetRule {
        val targetRule = RemoteConfigs.targetRule(key = key)
        every { matcher.matches(any(), any(), targetRule) } returns isMatch
        return targetRule
    }

    @Nested
    inner class MatcherTest {

        @MockK
        private lateinit var targetMatcher: TargetMatcher

        @MockK
        private lateinit var bucketer: Bucketer

        @InjectMockKs
        private lateinit var sut: RemoteConfigParameterTargetRuleMatcher

        @Test
        fun `Target 에 매치되지 않으면 false`() {
            // given
            val targetRule = RemoteConfigs.targetRule()
            every { targetMatcher.matches(any(), any(), targetRule.target) } returns false

            val request = RemoteConfigs.localRequest()

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isFalse()
            verify { bucketer wasNot Called }
        }

        @Test
        fun `식별자가 없으면 false`() {
            // given
            val targetRule = RemoteConfigs.targetRule()
            every { targetMatcher.matches(any(), any(), targetRule.target) } returns true

            val parameter = RemoteConfigs.config(identifierType = "customId")
            val request = RemoteConfigs.localRequest(parameter = parameter)

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isFalse()
            verify { bucketer wasNot Called }
        }

        @Test
        fun `Bucket 을 찾을 수 없으면 에러`() {
            // given
            val targetRule = RemoteConfigs.targetRule(bucketId = 42)
            every { targetMatcher.matches(any(), any(), targetRule.target) } returns true

            val request = RemoteConfigs.localRequest()

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.matches(request, Evaluators.context(), targetRule)
            }

            // then
            expectThat(exception.message) isEqualTo "Bucket[42]"
        }

        @Test
        fun `Slot 에 할당되어있지 않으면 false`() {
            // given
            val targetRule = RemoteConfigs.targetRule(bucketId = 42)
            every { targetMatcher.matches(any(), any(), targetRule.target) } returns true
            every { bucketer.bucketing(any(), any()) } returns null

            val request = RemoteConfigs.localRequest(
                workspace = Workspaces.config(buckets = listOf(bucket(id = 42)))
            )

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `Slot 에 할당되어 있으면 true`() {
            // given
            val targetRule = RemoteConfigs.targetRule(bucketId = 42)
            every { targetMatcher.matches(any(), any(), targetRule.target) } returns true
            every { bucketer.bucketing(any(), any()) } returns slot()

            val request = RemoteConfigs.localRequest(
                workspace = Workspaces.config(buckets = listOf(bucket(id = 42)))
            )

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isTrue()
        }
    }
}
