package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.support.InAppMessages
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
internal class InAppMessageMatcherTest {

    @Nested
    inner class InAppMessageUserOverrideMatcherTest {

        @InjectMockKs
        private lateinit var sut: InAppMessageUserOverrideMatcher

        @Test
        fun `override 정보가 없으면 false`() {
            // given
            val inAppMessage = InAppMessages.config()
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `overridden`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "a").build()
            val inAppMessage = InAppMessages.config(
                targetContext = InAppMessages.targetContext(
                    overrides = listOf(
                        InAppMessage.UserOverride("\$id", listOf("a")),
                        InAppMessage.UserOverride("\$userId", listOf("a")),
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not overridden`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "a").build()
            val inAppMessage = InAppMessages.config(
                targetContext = InAppMessages.targetContext(
                    overrides = listOf(
                        InAppMessage.UserOverride("\$id", listOf("a")),
                        InAppMessage.UserOverride("\$userId", listOf("a")),
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage)

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
            val actual = sut.matches(InAppMessages.eligibilityLocalRequest(), Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not match`() {
            // given
            every { targetMatcher.anyMatches(any(), any(), any()) } returns false

            // when
            val actual = sut.matches(InAppMessages.eligibilityLocalRequest(), Evaluators.context())

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
            val actual = sut.matches(InAppMessages.eligibilityLocalRequest(), Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `not exist`() {
            // given
            every { storage.exist(any(), any()) } returns false

            // when
            val actual = sut.matches(InAppMessages.eligibilityLocalRequest(), Evaluators.context())

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
        fun `frequency cap 이 없으면 false`() {
            // given
            val inAppMessage = InAppMessages.config()
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `cap 조건이 하나도 없으면 false`() {
            // given
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = emptyList(),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `impression 이 없으면 false`() {
            // given
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(InAppMessages.identifierCap()),
                        durationCap = InAppMessages.durationCap()
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)
            every { storage.get(any()) } returns emptyList()

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `identifier cap 이 매치되고 임계치에 도달하면 true`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(
                            InAppMessages.identifierCap(identifierType = IdentifierType.ID.key, count = 1)
                        ),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage)
            val impression = InAppMessages.impression(
                identifiers = mapOf(IdentifierType.ID.key to "test-id"),
                timestamp = 42
            )
            every { storage.get(any()) } returns listOf(impression)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `cap 이 매치되지만 임계치에 도달하지 못하면 false`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(
                            InAppMessages.identifierCap(identifierType = IdentifierType.ID.key, count = 2)
                        ),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage)
            val impression = InAppMessages.impression(
                identifiers = mapOf(IdentifierType.ID.key to "test-id"),
                timestamp = 42
            )
            every { storage.get(any()) } returns listOf(impression)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `여러 impression 이 누적되어 임계치에 도달하면 true`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(
                            InAppMessages.identifierCap(identifierType = IdentifierType.ID.key, count = 2)
                        ),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage)
            val impressions = listOf(
                InAppMessages.impression(identifiers = mapOf(IdentifierType.ID.key to "test-id"), timestamp = 41),
                InAppMessages.impression(identifiers = mapOf(IdentifierType.ID.key to "test-id"), timestamp = 42),
            )
            every { storage.get(any()) } returns impressions

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `매치되지 않는 impression 은 카운트하지 않는다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(
                            InAppMessages.identifierCap(identifierType = IdentifierType.ID.key, count = 2)
                        ),
                        durationCap = null
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage)
            val impressions = listOf(
                InAppMessages.impression(identifiers = mapOf(IdentifierType.ID.key to "another-id"), timestamp = 41),
                InAppMessages.impression(identifiers = mapOf(IdentifierType.ID.key to "test-id"), timestamp = 42),
            )
            every { storage.get(any()) } returns impressions

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `identifier cap 이 매치되지 않아도 duration cap 이 임계치에 도달하면 true`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = listOf(
                            InAppMessages.identifierCap(identifierType = IdentifierType.USER.key, count = 1)
                        ),
                        durationCap = InAppMessages.durationCap(durationMillis = 1000, count = 1)
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(user = user, inAppMessage = inAppMessage, timestamp = 2000)
            val impression = InAppMessages.impression(identifiers = emptyMap(), timestamp = 1500)
            every { storage.get(any()) } returns listOf(impression)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `duration cap 이 매치되고 임계치에 도달하면 true`() {
            // given
            val inAppMessage = InAppMessages.config(
                eventTrigger = InAppMessages.eventTrigger(
                    frequencyCap = InAppMessages.frequencyCap(
                        identifierCaps = emptyList(),
                        durationCap = InAppMessages.durationCap(durationMillis = 1000, count = 1)
                    )
                )
            )
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage, timestamp = 2000)
            val impression = InAppMessages.impression(identifiers = emptyMap(), timestamp = 1500)
            every { storage.get(any()) } returns listOf(impression)

            // when
            val actual = sut.matches(request, Evaluators.context())

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `IdentifierCapPredicate - 사용자 식별자가 없으면 false`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "test-device").build()
            val identifierCap = InAppMessages.identifierCap(identifierType = IdentifierType.ID.key, count = 1)
            val impression = InAppMessages.impression(
                identifiers = mapOf(IdentifierType.ID.key to "test-id"),
                timestamp = 42
            )

            // when
            val predicate = InAppMessageFrequencyCapMatcher.IdentifierCapPredicate(identifierCap)
            val actual = predicate.matches(user, 0, impression)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `IdentifierCapPredicate - impression 식별자가 없으면 false`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.ID, "test-id").build()
            val identifierCap = InAppMessages.identifierCap(identifierType = IdentifierType.ID.key, count = 1)
            val impression = InAppMessages.impression(
                identifiers = mapOf(IdentifierType.DEVICE.key to "test-device"),
                timestamp = 42
            )

            // when
            val predicate = InAppMessageFrequencyCapMatcher.IdentifierCapPredicate(identifierCap)
            val actual = predicate.matches(user, 0, impression)

            // then
            expectThat(actual).isFalse()
        }

        @Test
        fun `DurationCapPredicate - 기간 내 impression 이면 true`() {
            // given
            val user = HackleUser.builder().build()
            val durationCap = InAppMessages.durationCap(durationMillis = 1000, count = 1)
            val impression = InAppMessages.impression(identifiers = emptyMap(), timestamp = 1000)

            // when
            val predicate = InAppMessageFrequencyCapMatcher.DurationCapPredicate(durationCap)
            val actual = predicate.matches(user, 1500, impression)

            // then
            expectThat(actual).isTrue()
        }

        @Test
        fun `DurationCapPredicate - 기간을 벗어난 impression 이면 false`() {
            // given
            val user = HackleUser.builder().build()
            val durationCap = InAppMessages.durationCap(durationMillis = 1000, count = 1)
            val impression = InAppMessages.impression(identifiers = emptyMap(), timestamp = 1000)

            // when
            val predicate = InAppMessageFrequencyCapMatcher.DurationCapPredicate(durationCap)
            val actual = predicate.matches(user, 2500, impression)

            // then
            expectThat(actual).isFalse()
        }
    }
}
