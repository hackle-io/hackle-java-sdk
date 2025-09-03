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
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

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
            val request = InAppMessages.eligibilityRequest(user = user, inAppMessage = inAppMessage)

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
            val request = InAppMessages.eligibilityRequest(user = user, inAppMessage = inAppMessage)

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
            val actual = sut.matches(InAppMessages.eligibilityRequest(), Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not match`() {
            // given
            every { targetMatcher.anyMatches(any(), any(), any()) } returns false

            // when
            val actual = sut.matches(InAppMessages.eligibilityRequest(), Evaluators.context())

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
            val actual = sut.matches(InAppMessages.eligibilityRequest(), Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not exist`() {
            // given
            every { storage.exist(any(), any()) } returns false

            // when
            val actual = sut.matches(InAppMessages.eligibilityRequest(), Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }
    }

    @Nested
    inner class InAppMessageFrequencyCapMatcherTest {

        @MockK
        private lateinit var storage: InAppMessageImpressionStorage

        @InjectMockKs
        private lateinit var sut: InAppMessageFrequencyCapMatcher

        @Test
        fun `when frequency cap is null then returns false`() {
            // given
            val inAppMessage = InAppMessages.create()
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `when no contexts then returns false`() {
            // given
            val inAppMessage = InAppMessages.create(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = emptyList(),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `when no impressions then returns false`() {
            // given
            val inAppMessage = InAppMessages.create(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(InAppMessages.identifierCap()),
                        durationCap = InAppMessages.durationCap()
                    )
                )
            )
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)
            every { storage.get(any()) } returns emptyList()

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `when identifier cap matched and threshold exceeded then returns true`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val inAppMessage = InAppMessages.create(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(
                            InAppMessage.EventTrigger.IdentifierCap(
                                identifierType = IdentifierType.ID.key,
                                count = 1
                            )
                        ),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityRequest(user = user, inAppMessage = inAppMessage)
            val impression = InAppMessageImpression(
                timestamp = System.currentTimeMillis(),
                identifiers = mapOf(IdentifierType.ID.key to "test-id")
            )
            every { storage.get(any()) } returns listOf(impression)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `when duration cap matched and threshold exceeded then returns true`() {
            // given
            val now = System.currentTimeMillis()
            val inAppMessage = InAppMessages.create(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = emptyList(),
                        durationCap = InAppMessage.EventTrigger.DurationCap(
                            durationMillis = 1000L,
                            count = 1
                        )
                    )
                )
            )
            val request = InAppMessages.eligibilityRequest(
                inAppMessage = inAppMessage,
                timestamp = now
            )
            val impression = InAppMessageImpression(
                timestamp = now - 500L,
                identifiers = emptyMap()
            )
            every { storage.get(any()) } returns listOf(impression)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `when user identifier is missing then returns false`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "test-device").build()
            val identifierCap = InAppMessage.EventTrigger.IdentifierCap(
                identifierType = IdentifierType.ID.key,
                count = 1
            )
            val impression = InAppMessageImpression(
                timestamp = System.currentTimeMillis(),
                identifiers = mapOf(IdentifierType.ID.key to "test-id")
            )

            // when
            val predicate = InAppMessageFrequencyCapMatcher.IdentifierCapPredicate(identifierCap)
            val actual = predicate.matches(user, 0L, impression)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `when impression identifier is missing then returns false`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val identifierCap = InAppMessage.EventTrigger.IdentifierCap(
                identifierType = IdentifierType.ID.key,
                count = 1
            )
            val impression = InAppMessageImpression(
                timestamp = System.currentTimeMillis(),
                identifiers = mapOf(IdentifierType.DEVICE.key to "test-device")
            )

            // when
            val predicate = InAppMessageFrequencyCapMatcher.IdentifierCapPredicate(identifierCap)
            val actual = predicate.matches(user, 0L, impression)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `when duration is within cap then returns true`() {
            // given
            val user = HackleUser.builder().build()
            val durationCap = InAppMessage.EventTrigger.DurationCap(
                durationMillis = 1000L,
                count = 1
            )
            val impression = InAppMessageImpression(
                timestamp = 1000L,
                identifiers = emptyMap()
            )

            // when
            val predicate = InAppMessageFrequencyCapMatcher.DurationCapPredicate(durationCap)
            val actual = predicate.matches(user, 1500L, impression)

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `when duration exceeds cap then returns false`() {
            // given
            val user = HackleUser.builder().build()
            val durationCap = InAppMessage.EventTrigger.DurationCap(
                durationMillis = 1000L,
                count = 1
            )
            val impression = InAppMessageImpression(
                timestamp = 1000L,
                identifiers = emptyMap()
            )

            // when
            val predicate = InAppMessageFrequencyCapMatcher.DurationCapPredicate(durationCap)
            val actual = predicate.matches(user, 2500L, impression)

            // then
            expectThat(actual).isFalse()
        }


    }

}
