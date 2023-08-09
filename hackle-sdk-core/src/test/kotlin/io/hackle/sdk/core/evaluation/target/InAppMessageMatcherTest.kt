package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessages
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
class InAppMessageMatcherTest {

    @Nested
    inner class InAppMessageUserOverrideMatcherTest {

        @InjectMockKs
        private lateinit var sut: InAppMessageUserOverrideMatcher

        @Test
        fun `when override info is empty then returns false`() {
            // given
            val inAppMessage = InAppMessages.create()
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `overridden`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "a").build()
            val inAppMessage = InAppMessages.create(
                targetContext = InAppMessages.targetContext(
                    overrides = listOf(
                        InAppMessage.UserOverride("\$id", listOf("a")),
                        InAppMessage.UserOverride("\$userId", listOf("a")),
                    )
                )
            )
            val request = InAppMessages.request(user = user, inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not overridden`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "a").build()
            val inAppMessage = InAppMessages.create(
                targetContext = InAppMessages.targetContext(
                    overrides = listOf(
                        InAppMessage.UserOverride("\$id", listOf("a")),
                        InAppMessage.UserOverride("\$userId", listOf("a")),
                    )
                )
            )
            val request = InAppMessages.request(user = user, inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }
    }

    @Nested
    inner class InAppMessageTargetMatcherTest {

        @MockK
        private lateinit var targetMatcher: TargetMatcher

        @InjectMockKs
        private lateinit var sut: InAppMessageTargetMatcher

        @Test
        fun `match`() {
            // given
            every { targetMatcher.anyMatches(any(), any(), any()) } returns true

            // when
            val actual = sut.matches(InAppMessages.request(), Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not match`() {
            // given
            every { targetMatcher.anyMatches(any(), any(), any()) } returns false

            // when
            val actual = sut.matches(InAppMessages.request(), Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }
    }

    @Nested
    inner class InAppMessageHiddenMatcherTest {

        @MockK
        private lateinit var storage: InAppMessageHiddenStorage

        @InjectMockKs
        private lateinit var sut: InAppMessageHiddenMatcher

        @Test
        fun `exist`() {
            // given
            every { storage.exist(any(), any()) } returns true

            // when
            val actual = sut.matches(InAppMessages.request(), Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not exist`() {
            // given
            every { storage.exist(any(), any()) } returns false

            // when
            val actual = sut.matches(InAppMessages.request(), Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }
    }

}