package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Bucket
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
internal class RemoteConfigParameterTargetRuleDeterminerTest {

    @MockK
    private lateinit var matcher: RemoteConfigParameterTargetRuleDeterminer.Matcher

    @InjectMockKs
    private lateinit var sut: RemoteConfigParameterTargetRuleDeterminer


    @Test
    fun `첫번째로 매치되는 룰을 리턴한다`() {
        // given
        val matchedTargetRule = targetRule(true)
        val parameter = mockk<RemoteConfigParameter> {
            every { id } returns 42
            every { targetRules } returns listOf(
                targetRule(false),
                targetRule(false),
                targetRule(false),
                matchedTargetRule,
                targetRule(false),
                targetRule(false),
            )
        }

        val request = RemoteConfigRequest(mockk(), mockk(), parameter, mockk(), mockk())

        // when
        val actual = sut.determineTargetRuleOrNull(request, Evaluators.context())

        // then
        expectThat(actual) isSameInstanceAs matchedTargetRule
        verify(exactly = 4) { matcher.matches(any(), any(), any()) }
    }

    @Test
    fun `매치되는 룰이 없으면 null 리턴`() {
        val parameter = mockk<RemoteConfigParameter> {
            every { id } returns 42
            every { targetRules } returns listOf(
                targetRule(false),
                targetRule(false),
                targetRule(false),
                targetRule(false),
                targetRule(false),
            )
        }

        val request = RemoteConfigRequest(mockk(), mockk(), parameter, mockk(), mockk())

        // when
        val actual = sut.determineTargetRuleOrNull(request, Evaluators.context())

        // then
        expectThat(actual).isNull()
        verify(exactly = 5) { matcher.matches(any(), any(), any()) }
    }

    @Test
    fun `TargetRule 이 없으면 null 리턴`() {
        val parameter = mockk<RemoteConfigParameter> {
            every { id } returns 42
            every { targetRules } returns listOf()
        }

        val request = RemoteConfigRequest(mockk(), mockk(), parameter, mockk(), mockk())

        // when
        val actual = sut.determineTargetRuleOrNull(request, Evaluators.context())

        // then
        expectThat(actual).isNull()
        verify { matcher wasNot Called }
    }

    private fun targetRule(isMatch: Boolean): RemoteConfigParameter.TargetRule {
        val targetRule = mockk<RemoteConfigParameter.TargetRule>()
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
        private lateinit var sut: RemoteConfigParameterTargetRuleDeterminer.Matcher


        @Test
        fun `Target 에 매치되지 않으면 false`() {
            // given
            val targetRule = mockk<RemoteConfigParameter.TargetRule>()
            val target = mockk<Target>()
            every { targetMatcher.matches(any(), any(), target) } returns false
            every { targetRule.target } returns target

            val request = mockk<RemoteConfigRequest<Any>>()


            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isFalse()
        }


        @Test
        fun `식별자가 없으면 false`() {
            // given
            val targetRule = mockk<RemoteConfigParameter.TargetRule>()
            val target = mockk<Target>()
            every { targetMatcher.matches(any(), any(), target) } returns true
            every { targetRule.target } returns target

            val parameter = mockk<RemoteConfigParameter> {
                every { id } returns 42
                every { identifierType } returns "customId"
            }

            val request = RemoteConfigRequest(mockk(), HackleUser.of("a"), parameter, mockk(), mockk())

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `Bucket 을 찾을 수 없으면 에러`() {
            // given
            val targetRule = mockk<RemoteConfigParameter.TargetRule>()
            val target = mockk<Target>()
            every { targetMatcher.matches(any(), any(), target) } returns true
            every { targetRule.target } returns target

            val parameter = mockk<RemoteConfigParameter> {
                every { id } returns 42
                every { identifierType } returns "\$id"
            }

            val bucketId = 42L
            every { targetRule.bucketId } returns bucketId

            val workspace = mockk<Workspace>()
            every { workspace.getBucketOrNull(bucketId) } returns null

            val request = RemoteConfigRequest(workspace, HackleUser.of("a"), parameter, mockk(), mockk())

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
            val targetRule = mockk<RemoteConfigParameter.TargetRule>()
            val target = mockk<Target>()
            every { targetMatcher.matches(any(), any(), target) } returns true
            every { targetRule.target } returns target

            val parameter = mockk<RemoteConfigParameter> {
                every { id } returns 42
                every { identifierType } returns "\$id"
            }

            val bucketId = 42L
            every { targetRule.bucketId } returns bucketId

            val bucket = mockk<Bucket>()
            every { bucketer.bucketing(bucket, any()) } returns null

            val workspace = mockk<Workspace>()
            every { workspace.getBucketOrNull(bucketId) } returns bucket

            val request = RemoteConfigRequest(workspace, HackleUser.of("a"), parameter, mockk(), mockk())

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `Slot 에 할당되어 있으면 true`() {
            // given
            val target = io.hackle.sdk.core.model.Target(emptyList())
            val targetRule = RemoteConfigParameter.TargetRule(
                "target_rule_key",
                "target_rule_name",
                target,
                42,
                RemoteConfigParameter.Value(320, "targetRuleValue")
            )

            every { targetMatcher.matches(any(), any(), target) } returns true

            val parameter = mockk<RemoteConfigParameter> {
                every { id } returns 42
                every { identifierType } returns "\$id"
            }

            val bucket = mockk<Bucket>()
            every { bucketer.bucketing(bucket, any()) } returns mockk()

            val workspace = mockk<Workspace>()
            every { workspace.getBucketOrNull(42) } returns bucket

            val request = RemoteConfigRequest(workspace, HackleUser.of("a"), parameter, mockk(), mockk())

            // when
            val actual = sut.matches(request, Evaluators.context(), targetRule)

            // then
            expectThat(actual).isTrue()
        }
    }
}